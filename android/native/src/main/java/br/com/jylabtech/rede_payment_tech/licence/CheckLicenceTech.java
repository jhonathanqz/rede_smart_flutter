package br.com.jylabtech.rede_payment_tech.licence;

import android.content.Context;

import java.util.Date;

/**
 * Orquestra a validação de licença conforme o contrato de licenças do jylabtech_core:
 *
 * <ul>
 *   <li>A primeira validação de cada processo (abertura do app) sempre consulta o servidor.</li>
 *   <li>Depois disso, com cache válido, usa o cache e consulta o servidor de novo a cada
 *       {@link LicenceRules#REVALIDATE_INTERVAL_MS} (4h).</li>
 *   <li>Recusa definitiva do servidor (cliente bloqueado, licença vencida/cancelada/suspensa, não
 *       encontrada, aparelho desativado...) apaga o cache e bloqueia na hora.</li>
 *   <li>Falha de rede, timeout ou 5xx usa o cache válido (salvo há no máximo 72h); sem cache,
 *       bloqueia. Após uma falha, espera {@link LicenceRules#RETRY_AFTER_FAILURE_MS} para tentar
 *       de novo, para não travar cada operação offline no timeout.</li>
 * </ul>
 *
 * A lógica não usa APIs do Android diretamente (rede, cache e relógio são injetáveis), o que
 * permite testá-la em JVM. O construtor com {@link Context} monta as implementações reais.
 */
public class CheckLicenceTech {
    private final LicenceNetwork licenceNetwork;
    private final LicenceStorage licenceStorage;
    private final LicenceClock clock;

    // Estado deste processo (não persistido): reiniciar o app força nova consulta ao servidor.
    // volatile: validateExistsLicence lê sem lock (pode ser chamado na main thread).
    private volatile boolean checkedOnlineInThisProcess;
    private volatile Date lastOnlineFailureAt;

    public CheckLicenceTech(Context context) {
        this(new LicenceProvider(context), new EncryptedLicenceStorage(context), LicenceClock.SYSTEM);
    }

    public CheckLicenceTech(LicenceNetwork licenceNetwork, LicenceStorage licenceStorage, LicenceClock clock) {
        this.licenceNetwork = licenceNetwork;
        this.licenceStorage = licenceStorage;
        this.clock = clock;
    }

    /**
     * Valida a licença (servidor ou cache, conforme as regras acima).
     * Faz acesso à rede: não chame na main thread.
     */
    public synchronized LicenceResult validateLicence(String licenceKey, String licenceInternalKey) {
        try {
            CheckLicenceEntity cached = validCachedLicence(licenceKey, licenceInternalKey);
            Date now = clock.now();
            if (cached != null && !LicenceRules.shouldRevalidate(cached, checkedOnlineInThisProcess, now)) {
                return success(cached);
            }
            if (cached != null && recentlyFailedOnline(now)) {
                return success(cached);
            }

            String apiUrl = LicenceConfig.resolveLicenceUrl(licenceInternalKey);
            LicenceResult licence;
            try {
                licence = licenceNetwork.checkLicence(apiUrl, licenceKey);
            } catch (Exception e) {
                return onlineFailure(cached, now, "Erro ao validar licença: " + describe(e));
            }
            if (licence == null) {
                return onlineFailure(cached, now, "Erro ao validar licença.");
            }

            CheckLicenceEntity entity = licence.getLicenceEntity();
            if (licence.getResult() == LicenceRules.RESULT_SUCCESS && LicenceRules.isLicenceUsable(entity, now)) {
                // Salva a URL efetiva (sempre oficial), não o valor recebido do app.
                entity.setLicenceInternalKey(apiUrl);
                entity.setSavedLocalDatabaseAt(now);
                licenceStorage.save(entity);
                checkedOnlineInThisProcess = true;
                lastOnlineFailureAt = null;
                return success(entity);
            }

            String message = licence.getResult() == LicenceRules.RESULT_SUCCESS
                    ? LicenceRules.unusableReason(entity, now)
                    : licence.getErrorMessage();
            if (message == null) {
                message = "Erro ao validar licença.";
            }
            if (licence.getResult() == LicenceRules.RESULT_SUCCESS || licence.isRejectedByServer()) {
                // Recusa definitiva: o cache não pode mais liberar o uso.
                licenceStorage.delete();
                checkedOnlineInThisProcess = true;
                lastOnlineFailureAt = null;
                LicenceResult rejected = error(message);
                rejected.setRejectedByServer(true);
                return rejected;
            }
            return onlineFailure(cached, now, message);
        } catch (Exception e) {
            return error("Erro ao validar licença: " + describe(e));
        }
    }

    /**
     * Verifica somente o cache local (sem rede): true só quando {@link #validateLicence} também
     * liberaria sem consultar o servidor agora (cache válido, já validado online neste processo há
     * menos de 4h, ou servidor fora do ar há menos de 5 min). Cache inválido é apagado.
     * Não usa lock: pode ser chamado na main thread como atalho antes de {@link #validateLicence}.
     */
    public boolean validateExistsLicence(String licenceKey, String licenceInternalKey) {
        try {
            CheckLicenceEntity cached = validCachedLicence(licenceKey, licenceInternalKey);
            if (cached == null) {
                return false;
            }
            Date now = clock.now();
            return !LicenceRules.shouldRevalidate(cached, checkedOnlineInThisProcess, now)
                    || recentlyFailedOnline(now);
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

    private boolean recentlyFailedOnline(Date now) {
        Date failedAt = lastOnlineFailureAt;
        if (failedAt == null) {
            return false;
        }
        long elapsed = now.getTime() - failedAt.getTime();
        return elapsed >= 0 && elapsed < LicenceRules.RETRY_AFTER_FAILURE_MS;
    }

    /** Servidor inacessível: usa o cache válido (até 72h) ou bloqueia se não houver. */
    private LicenceResult onlineFailure(CheckLicenceEntity cached, Date now, String message) {
        lastOnlineFailureAt = now;
        if (cached != null) {
            return success(cached);
        }
        return error(message);
    }

    private static LicenceResult success(CheckLicenceEntity entity) {
        LicenceResult result = new LicenceResult();
        result.setMethod("licence");
        result.setResult(LicenceRules.RESULT_SUCCESS);
        result.setLicenceEntity(entity);
        return result;
    }

    private static LicenceResult error(String message) {
        LicenceResult result = new LicenceResult();
        result.setMethod("licence");
        result.setResult(LicenceRules.RESULT_ERROR);
        result.setErrorMessage(message);
        return result;
    }

    private static String describe(Exception e) {
        String message = e.getMessage();
        return message != null ? message : e.getClass().getSimpleName();
    }
}
