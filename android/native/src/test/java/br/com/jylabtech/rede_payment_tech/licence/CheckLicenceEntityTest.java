package br.com.jylabtech.rede_payment_tech.licence;

import static br.com.jylabtech.rede_payment_tech.licence.LicenceTestData.LICENCE_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CheckLicenceEntityTest {

    @Test
    public void parsesRealCoreResponse() {
        CheckLicenceEntity entity = CheckLicenceEntity.fromJson(
                LicenceTestData.coreJson("2099-12-31T23:59:59", false, false, false));

        assertNotNull(entity);
        assertEquals(LICENCE_KEY, entity.getLicenceKey());
        assertEquals(LICENCE_KEY, entity.getLicenceEncrypt());
        assertFalse(entity.isExpired());
        assertFalse(entity.isInactive());
        assertFalse(entity.isCanceled());
        assertEquals("one_time", entity.getCheckoutType());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        cal.setTime(entity.getExpiredAt());
        assertEquals(2099, cal.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
        assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(23, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, cal.get(Calendar.MINUTE));

        assertTrue(LicenceRules.isLicenceUsable(entity, new Date()));
    }

    @Test
    public void parsesCanceledFlag() {
        CheckLicenceEntity entity = CheckLicenceEntity.fromJson(
                LicenceTestData.coreJson("2099-12-31T23:59:59", false, false, true));
        assertNotNull(entity);
        assertTrue(entity.isCanceled());
    }

    @Test
    public void nullExpiredAtIsKeptNull() {
        CheckLicenceEntity entity = CheckLicenceEntity.fromJson(
                LicenceTestData.coreJson(null, false, false, false));
        assertNotNull(entity);
        assertNull(entity.getExpiredAt());
    }

    @Test
    public void invalidJsonReturnsNull() {
        assertNull(CheckLicenceEntity.fromJson("not json"));
        assertNull(CheckLicenceEntity.fromJson(null));
    }

    @Test
    public void roundTripKeepsCacheFields() {
        Date now = new Date((new Date().getTime() / 1000) * 1000);
        CheckLicenceEntity entity = LicenceTestData.validCache(now);
        entity.setCanceled(false);
        entity.setSavedLocalDatabaseAt(now);

        CheckLicenceEntity parsed = CheckLicenceEntity.fromJson(entity.toJson());
        assertNotNull(parsed);
        assertEquals(entity.getLicenceEncrypt(), parsed.getLicenceEncrypt());
        assertEquals(entity.getLicenceInternalKey(), parsed.getLicenceInternalKey());
        assertEquals(entity.getExpiredAt().getTime() / 1000, parsed.getExpiredAt().getTime() / 1000);
        assertEquals(now.getTime(), parsed.getSavedLocalDatabaseAt().getTime());
        assertTrue(LicenceRules.isCacheValid(parsed, LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL, now));
    }
}
