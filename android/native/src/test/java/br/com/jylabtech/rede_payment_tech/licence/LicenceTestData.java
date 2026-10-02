package br.com.jylabtech.rede_payment_tech.licence;

import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/** Dados fictícios para os testes (nenhuma chave real). */
final class LicenceTestData {
    static final String LICENCE_KEY = "JYL-TEST-0000-0000";
    static final String OLD_CLOUD_RUN_URL =
            "https://pos-payments-api-577464235408.southamerica-east1.run.app/pos-payments/licence/check";

    private LicenceTestData() {}

    static String b64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    static Date daysFrom(Date base, long days) {
        return new Date(base.getTime() + TimeUnit.DAYS.toMillis(days));
    }

    static CheckLicenceEntity usableEntity(Date now) {
        CheckLicenceEntity entity = new CheckLicenceEntity();
        entity.setLicenceKey(LICENCE_KEY);
        entity.setLicenceEncrypt(LICENCE_KEY);
        entity.setExpiredAt(daysFrom(now, 30));
        return entity;
    }

    static CheckLicenceEntity validCache(Date now) {
        CheckLicenceEntity entity = usableEntity(now);
        entity.setSavedLocalDatabaseAt(daysFrom(now, -1));
        entity.setLicenceInternalKey(LicenceConfig.LICENCE_CHECK_URL);
        return entity;
    }

    /** JSON no formato do LegacyLicence do jylabtech_core (legacypos.go). */
    static String coreJson(String expiredAt, boolean isExpired, boolean isInactive, boolean isCanceled) {
        return "{"
                + "\"id\":\"64b000000000000000000001\","
                + "\"uid\":\"64b000000000000000000001\","
                + "\"deviceUid\":\"\","
                + "\"deviceSerialNumber\":\"\","
                + "\"userUid\":\"cus_test\","
                + "\"productId\":\"prod_test\","
                + "\"productName\":\"Licença plugin de pagamento\","
                + "\"productDescription\":\"\","
                + "\"integrationLibId\":\"\","
                + "\"integrationLibName\":\"pos_payments\","
                + "\"checkoutSessionId\":\"\","
                + "\"licenceKey\":\"" + LICENCE_KEY + "\","
                + "\"licenceEncrypt\":\"" + LICENCE_KEY + "\","
                + "\"expiredAt\":" + (expiredAt == null ? "null" : "\"" + expiredAt + "\"") + ","
                + "\"isExpired\":" + isExpired + ","
                + "\"isInactive\":" + isInactive + ","
                + "\"isTrial\":false,"
                + "\"isDebug\":true,"
                + "\"isCanceled\":" + isCanceled + ","
                + "\"canceledAt\":null,"
                + "\"licencesQuantity\":-1,"
                + "\"subscriptionId\":\"\","
                + "\"subscription\":null,"
                + "\"checkoutType\":\"one_time\","
                + "\"createdAt\":\"2026-01-10T08:00:00\","
                + "\"updatedAt\":\"2026-09-01T12:30:00\","
                + "\"coreToken\":\"header.payload.signature\""
                + "}";
    }
}
