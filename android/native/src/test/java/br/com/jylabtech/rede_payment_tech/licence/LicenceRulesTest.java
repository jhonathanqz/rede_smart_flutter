package br.com.jylabtech.rede_payment_tech.licence;

import static br.com.jylabtech.rede_payment_tech.licence.LicenceTestData.LICENCE_KEY;
import static br.com.jylabtech.rede_payment_tech.licence.LicenceTestData.OLD_CLOUD_RUN_URL;
import static br.com.jylabtech.rede_payment_tech.licence.LicenceTestData.b64;
import static br.com.jylabtech.rede_payment_tech.licence.LicenceTestData.daysFrom;
import static br.com.jylabtech.rede_payment_tech.licence.LicenceTestData.usableEntity;
import static br.com.jylabtech.rede_payment_tech.licence.LicenceTestData.validCache;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class LicenceRulesTest {
    private final Date now = new Date();

    // ---- isLicenceUsable ----

    @Test
    public void usableLicence() {
        assertTrue(LicenceRules.isLicenceUsable(usableEntity(now), now));
    }

    @Test
    public void expiredByDate() {
        CheckLicenceEntity entity = usableEntity(now);
        entity.setExpiredAt(daysFrom(now, -1));
        assertFalse(LicenceRules.isLicenceUsable(entity, now));
        entity.setExpiredAt(now);
        assertFalse(LicenceRules.isLicenceUsable(entity, now));
    }

    @Test
    public void flaggedExpired() {
        CheckLicenceEntity entity = usableEntity(now);
        entity.setExpired(true);
        assertFalse(LicenceRules.isLicenceUsable(entity, now));
    }

    @Test
    public void flaggedInactive() {
        CheckLicenceEntity entity = usableEntity(now);
        entity.setInactive(true);
        assertFalse(LicenceRules.isLicenceUsable(entity, now));
    }

    @Test
    public void flaggedCanceled() {
        CheckLicenceEntity entity = usableEntity(now);
        entity.setCanceled(true);
        assertFalse(LicenceRules.isLicenceUsable(entity, now));
    }

    @Test
    public void nullExpiredAt() {
        CheckLicenceEntity entity = usableEntity(now);
        entity.setExpiredAt(null);
        assertFalse(LicenceRules.isLicenceUsable(entity, now));
    }

    @Test
    public void nullEntity() {
        assertFalse(LicenceRules.isLicenceUsable(null, now));
    }

    @Test
    public void masterKey2099() {
        CheckLicenceEntity entity = CheckLicenceEntity.fromJson(
                LicenceTestData.coreJson("2099-12-31T23:59:59", false, false, false));
        assertTrue(LicenceRules.isLicenceUsable(entity, now));
    }

    // ---- isCacheValid ----

    @Test
    public void cacheValid() {
        assertTrue(LicenceRules.isCacheValid(validCache(now), LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL, now));
    }

    @Test
    public void cacheSavedWithCoreBase64IsValid() {
        CheckLicenceEntity cache = validCache(now);
        cache.setLicenceInternalKey(b64(LicenceConfig.LICENCE_CHECK_URL));
        assertTrue(LicenceRules.isCacheValid(cache, LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL, now));
        assertTrue(LicenceRules.isCacheValid(cache, LICENCE_KEY, b64(LicenceConfig.LICENCE_CHECK_URL), now));
    }

    @Test
    public void cacheSavedWithOldCloudRunUrlIsInvalid() {
        CheckLicenceEntity cache = validCache(now);
        cache.setLicenceInternalKey(b64(OLD_CLOUD_RUN_URL));
        assertFalse(LicenceRules.isCacheValid(cache, LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL, now));
        assertFalse(LicenceRules.isCacheValid(cache, LICENCE_KEY, b64(OLD_CLOUD_RUN_URL), now));
        cache.setLicenceInternalKey(OLD_CLOUD_RUN_URL);
        assertFalse(LicenceRules.isCacheValid(cache, LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL, now));
    }

    @Test
    public void cacheSavedWithForeignOrEmptyValueIsInvalid() {
        String[] invalid = {"https://evil.com/pos-payments/licence/check",
                "http://licence-server.invalid/pos-payments/licence/check", "", null, "lixo!!"};
        for (String saved : invalid) {
            CheckLicenceEntity cache = validCache(now);
            cache.setLicenceInternalKey(saved);
            assertFalse(String.valueOf(saved),
                    LicenceRules.isCacheValid(cache, LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL, now));
        }
    }

    @Test
    public void cacheValidWhenCurrentValueIsIgnored() {
        // valor atual antigo/estranho é ignorado (resolve para o core); cache oficial continua valendo
        assertTrue(LicenceRules.isCacheValid(validCache(now), LICENCE_KEY, b64(OLD_CLOUD_RUN_URL), now));
    }

    @Test
    public void cacheOlderThan7Days() {
        CheckLicenceEntity cache = validCache(now);
        cache.setSavedLocalDatabaseAt(new Date(now.getTime() - TimeUnit.DAYS.toMillis(7) - 1000));
        assertFalse(LicenceRules.isCacheValid(cache, LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL, now));
    }

    @Test
    public void cacheExactly7DaysIsStillValid() {
        CheckLicenceEntity cache = validCache(now);
        cache.setSavedLocalDatabaseAt(new Date(now.getTime() - TimeUnit.DAYS.toMillis(7)));
        assertTrue(LicenceRules.isCacheValid(cache, LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL, now));
    }

    @Test
    public void cacheSavedInTheFuture() {
        CheckLicenceEntity cache = validCache(now);
        cache.setSavedLocalDatabaseAt(new Date(now.getTime() + TimeUnit.MINUTES.toMillis(10)));
        assertFalse(LicenceRules.isCacheValid(cache, LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL, now));
    }

    @Test
    public void cacheSavedSlightlyInTheFutureWithinTolerance() {
        CheckLicenceEntity cache = validCache(now);
        cache.setSavedLocalDatabaseAt(new Date(now.getTime() + TimeUnit.MINUTES.toMillis(2)));
        assertTrue(LicenceRules.isCacheValid(cache, LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL, now));
    }

    @Test
    public void cacheWithoutSavedAt() {
        CheckLicenceEntity cache = validCache(now);
        cache.setSavedLocalDatabaseAt(null);
        assertFalse(LicenceRules.isCacheValid(cache, LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL, now));
    }

    @Test
    public void cacheWithDifferentKey() {
        assertFalse(LicenceRules.isCacheValid(validCache(now), "JYL-OTHER-KEY", LicenceConfig.LICENCE_CHECK_URL, now));
        assertFalse(LicenceRules.isCacheValid(validCache(now), null, LicenceConfig.LICENCE_CHECK_URL, now));
    }

    @Test
    public void cacheWithDifferentUrl() {
        CheckLicenceEntity cache = validCache(now);
        cache.setLicenceInternalKey(LicenceConfig.LICENCE_CHECK_URL + "/invoice");
        assertFalse(LicenceRules.isCacheValid(cache, LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL, now));
    }

    @Test
    public void cacheWithExpiredLicence() {
        CheckLicenceEntity cache = validCache(now);
        cache.setExpiredAt(daysFrom(now, -1));
        assertFalse(LicenceRules.isCacheValid(cache, LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL, now));

        CheckLicenceEntity canceled = validCache(now);
        canceled.setCanceled(true);
        assertFalse(LicenceRules.isCacheValid(canceled, LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL, now));
    }
}
