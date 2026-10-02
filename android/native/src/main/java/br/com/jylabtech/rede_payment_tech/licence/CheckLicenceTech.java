package br.com.jylabtech.rede_payment_tech.licence;

import android.content.Context;

import java.util.Date;

/**
 * Orquestra a validação de licença: primeiro o cache local (se válido por todas as regras de
 * {@link LicenceRules#isCacheValid}), depois o servidor oficial da JY Labtech.
 *
 * A lógica não usa APIs do Android diretamente (rede, cache e relógio são injetáveis), o que
 * permite testá-la em JVM. O construtor com {@link Context} monta as implementações reais.
 */
public class CheckLicenceTech {
    private final LicenceNetwork licenceNetwork;
    private final LicenceStorage licenceStorage;
    private final LicenceClock clock;

    public CheckLicenceTech(Context context) {
        this(new LicenceProvider(context), new EncryptedLicenceStorage(context), LicenceClock.SYSTEM);
    }

    public CheckLicenceTech(LicenceNetwork licenceNetwork, LicenceStorage licenceStorage, LicenceClock clock) {
        this.licenceNetwork = licenceNetwork;
        this.licenceStorage = licenceStorage;
        this.clock = clock;
    }

    /**
     * Valida a licença usando o cache válido (sem rede) ou, se não houver, o servidor.
     * Faz acesso à rede: não chame na main thread.
     */
    public LicenceResult validateLicence(String licenceKey, String licenceInternalKey) {
        LicenceResult licenceResult = new LicenceResult();
        licenceResult.setMethod("licence");
        try {
            CheckLicenceEntity cached = validCachedLicence(licenceKey, licenceInternalKey);
            if (cached != null) {
                licenceResult.setResult(LicenceRules.RESULT_SUCCESS);
                licenceResult.setLicenceEntity(cached);
                return licenceResult;
            }

            String apiUrl = LicenceConfig.resolveLicenceUrl(licenceInternalKey);
            LicenceResult licence = licenceNetwork.checkLicence(apiUrl, licenceKey);
            if (licence == null) {
                licenceResult.setResult(LicenceRules.RESULT_ERROR);
                licenceResult.setErrorMessage("Erro ao validar licença.");
                return licenceResult;
            }

            Date now = clock.now();
            CheckLicenceEntity entity = licence.getLicenceEntity();
            if (licence.getResult() == LicenceRules.RESULT_SUCCESS && LicenceRules.isLicenceUsable(entity, now)) {
                // Salva a URL efetiva (sempre oficial), não o valor recebido do app.
                entity.setLicenceInternalKey(apiUrl);
                entity.setSavedLocalDatabaseAt(now);
                licenceStorage.save(entity);
                licenceResult.setResult(LicenceRules.RESULT_SUCCESS);
                licenceResult.setLicenceEntity(entity);
                return licenceResult;
            }

            licenceResult.setResult(LicenceRules.RESULT_ERROR);
            String message = licence.getResult() == LicenceRules.RESULT_SUCCESS
                    ? LicenceRules.unusableReason(entity, now)
                    : licence.getErrorMessage();
            licenceResult.setErrorMessage(message != null ? message : "Erro ao validar licença.");
            return licenceResult;
        } catch (Exception e) {
            licenceResult.setResult(LicenceRules.RESULT_ERROR);
            String message = e.getMessage();
            licenceResult.setErrorMessage("Erro ao validar licença: "
                    + (message != null ? message : e.getClass().getSimpleName()));
        }
        return licenceResult;
    }

    /**
     * Verifica somente o cache local (sem rede). Cache inválido é apagado.
     */
    public boolean validateExistsLicence(String licenceKey, String licenceInternalKey) {
        try {
            return validCachedLicence(licenceKey, licenceInternalKey) != null;
        } catch (Exception e) {
            return false;
        }
    }

    private CheckLicenceEntity validCachedLicence(String licenceKey, String licenceInternalKey) {
        CheckLicenceEntity cached = licenceStorage.load();
        if (cached == null) {
            return null;
        }
        if (LicenceRules.isCacheValid(cached, licenceKey, licenceInternalKey, clock.now())) {
            return cached;
        }
        licenceStorage.delete();
        return null;
    }
}
