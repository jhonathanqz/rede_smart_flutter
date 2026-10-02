package br.com.jylabtech.rede_payment_tech.licence;

import org.json.JSONObject;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Regras puras de validação de licença (sem dependências do Android, testáveis em JVM).
 */
public final class LicenceRules {
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_ERROR = 999999;

    /** Validade máxima do cache offline sem nova consulta ao servidor. */
    public static final long CACHE_MAX_AGE_MS = TimeUnit.DAYS.toMillis(7);
    /** Tolerância para relógio do aparelho atrasado em relação ao momento em que o cache foi salvo. */
    public static final long CLOCK_SKEW_TOLERANCE_MS = TimeUnit.MINUTES.toMillis(5);

    private LicenceRules() {}

    /** Licença só é utilizável se existir, tiver validade futura e não estiver vencida, inativa ou cancelada. */
    public static boolean isLicenceUsable(CheckLicenceEntity entity, Date now) {
        if (entity == null || now == null) {
            return false;
        }
        Date expiredAt = entity.getExpiredAt();
        if (expiredAt == null || !expiredAt.after(now)) {
            return false;
        }
        return !entity.isExpired() && !entity.isInactive() && !entity.isCanceled();
    }

    /** Motivo legível pelo qual a licença não é utilizável (null se for utilizável). */
    public static String unusableReason(CheckLicenceEntity entity, Date now) {
        if (entity == null) return "Resposta inválida do servidor de licença.";
        if (entity.getExpiredAt() == null) return "Resposta do servidor de licença sem data de validade.";
        if (entity.isCanceled()) return "Licença cancelada.";
        if (entity.isInactive()) return "Licença inativa.";
        if (entity.isExpired() || !entity.getExpiredAt().after(now)) return "Licença vencida.";
        return null;
    }

    /**
     * O cache offline só vale se a licença for utilizável, tiver sido salva há no máximo 7 dias
     * (e não no futuro, além da tolerância), pertencer à mesma licenceKey e ter sido salvo com a
     * URL oficial, igual à URL resolvida atual.
     */
    public static boolean isCacheValid(CheckLicenceEntity cache, String licenceKey, String licenceInternalKey, Date now) {
        if (!isLicenceUsable(cache, now)) {
            return false;
        }
        Date savedAt = cache.getSavedLocalDatabaseAt();
        if (savedAt == null) {
            return false;
        }
        long age = now.getTime() - savedAt.getTime();
        if (age > CACHE_MAX_AGE_MS) {
            return false;
        }
        if (age < -CLOCK_SKEW_TOLERANCE_MS) {
            return false;
        }
        if (licenceKey == null || !Objects.equals(cache.getLicenceEncrypt(), licenceKey)) {
            return false;
        }
        // Cache salvo com valor fora do servidor oficial (ex.: URL antiga do Cloud Run) não vale:
        // precisa ser revalidado online no core.
        if (!LicenceConfig.isOfficialLicenceInternalKey(cache.getLicenceInternalKey())) {
            return false;
        }
        String savedUrl = LicenceConfig.resolveLicenceUrl(cache.getLicenceInternalKey());
        String currentUrl = LicenceConfig.resolveLicenceUrl(licenceInternalKey);
        return Objects.equals(savedUrl, currentUrl);
    }

    /** Converte a resposta HTTP do servidor de licença em um {@link LicenceResult}. */
    public static LicenceResult decideHttpResult(int statusCode, String body, Date now) {
        LicenceResult result = new LicenceResult();
        result.setMethod("licence");

        if (statusCode == 200) {
            CheckLicenceEntity entity = body == null ? null : CheckLicenceEntity.fromJson(body);
            String reason = unusableReason(entity, now);
            if (reason != null) {
                result.setResult(RESULT_ERROR);
                result.setErrorMessage(reason);
                return result;
            }
            result.setResult(RESULT_SUCCESS);
            result.setLicenceEntity(entity);
            return result;
        }

        result.setResult(RESULT_ERROR);
        result.setErrorMessage(extractErrorMessage(statusCode, body));
        return result;
    }

    static String extractErrorMessage(int statusCode, String body) {
        String fallback = "Erro ao validar licença (HTTP " + statusCode + ")";
        if (body == null || body.trim().isEmpty()) {
            return fallback;
        }
        try {
            JSONObject json = new JSONObject(body);
            if (json.has("message") && !json.isNull("message")) {
                String message = json.optString("message", "").trim();
                if (!message.isEmpty()) {
                    return message;
                }
            }
        } catch (Exception ignored) {
            // corpo não é JSON
        }
        return fallback;
    }
}
