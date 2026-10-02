package br.com.jylabtech.rede_payment_tech.licence;

import static br.com.jylabtech.rede_payment_tech.licence.LicenceTestData.LICENCE_KEY;
import static br.com.jylabtech.rede_payment_tech.licence.LicenceTestData.OLD_CLOUD_RUN_URL;
import static br.com.jylabtech.rede_payment_tech.licence.LicenceTestData.b64;
import static br.com.jylabtech.rede_payment_tech.licence.LicenceTestData.daysFrom;
import static br.com.jylabtech.rede_payment_tech.licence.LicenceTestData.validCache;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CheckLicenceTechTest {

    static class FakeStorage implements LicenceStorage {
        CheckLicenceEntity stored;
        int saves;
        int deletes;

        @Override public CheckLicenceEntity load() { return stored; }
        @Override public void save(CheckLicenceEntity entity) { stored = entity; saves++; }
        @Override public void delete() { stored = null; deletes++; }
    }

    static class FakeNetwork implements LicenceNetwork {
        final List<String> urls = new ArrayList<>();
        final List<String> keys = new ArrayList<>();
        int status = 200;
        String body;
        Exception error;

        @Override
        public LicenceResult checkLicence(String url, String licenceKey) throws Exception {
            urls.add(url);
            keys.add(licenceKey);
            if (error != null) throw error;
            return LicenceRules.decideHttpResult(status, body, new Date());
        }
    }

    private final Date now = new Date();
    private FakeStorage storage;
    private FakeNetwork network;
    private CheckLicenceTech tech;

    @Before
    public void setUp() {
        storage = new FakeStorage();
        network = new FakeNetwork();
        tech = new CheckLicenceTech(network, storage, () -> now);
    }

    @Test
    public void validCacheSkipsNetwork() {
        storage.stored = validCache(now);
        LicenceResult result = tech.validateLicence(LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL);
        assertEquals(LicenceRules.RESULT_SUCCESS, result.getResult());
        assertTrue(network.urls.isEmpty());
        assertTrue(tech.validateExistsLicence(LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL));
    }

    @Test
    public void onlineSuccessSavesCacheWithNow() {
        network.body = LicenceTestData.coreJson("2099-12-31T23:59:59", false, false, false);
        LicenceResult result = tech.validateLicence(LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL);
        assertEquals(LicenceRules.RESULT_SUCCESS, result.getResult());
        assertEquals(1, storage.saves);
        assertEquals(now, storage.stored.getSavedLocalDatabaseAt());
        assertEquals(LicenceConfig.LICENCE_CHECK_URL, storage.stored.getLicenceInternalKey());
        assertEquals(LICENCE_KEY, network.keys.get(0));
    }

    @Test
    public void alwaysCallsCoreEvenWithForeignInternalKey() {
        network.body = LicenceTestData.coreJson("2099-12-31T23:59:59", false, false, false);
        tech.validateLicence(LICENCE_KEY, b64("https://evil.com/pos-payments/licence/check"));
        tech.validateLicence(LICENCE_KEY, b64(OLD_CLOUD_RUN_URL));
        tech.validateLicence(LICENCE_KEY, "");
        storage.stored = null;
        tech.validateLicence(LICENCE_KEY, null);
        for (String url : network.urls) {
            assertEquals(LicenceConfig.LICENCE_CHECK_URL, url);
        }
        assertFalse(network.urls.isEmpty());
    }

    @Test
    public void online200ExpiredIsErrorAndNotSaved() {
        network.body = LicenceTestData.coreJson("2020-01-01T00:00:00", false, false, false);
        LicenceResult result = tech.validateLicence(LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL);
        assertEquals(LicenceRules.RESULT_ERROR, result.getResult());
        assertEquals(0, storage.saves);
        assertNull(storage.stored);
    }

    @Test
    public void online200CanceledIsErrorAndNotSaved() {
        network.body = LicenceTestData.coreJson("2099-12-31T23:59:59", false, false, true);
        LicenceResult result = tech.validateLicence(LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL);
        assertEquals(LicenceRules.RESULT_ERROR, result.getResult());
        assertEquals(0, storage.saves);
    }

    @Test
    public void serverErrorPropagatesMessage() {
        network.status = 428;
        network.body = "{\"message\":\"Licença vencida\"}";
        LicenceResult result = tech.validateLicence(LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL);
        assertEquals(LicenceRules.RESULT_ERROR, result.getResult());
        assertEquals("Licença vencida", result.getErrorMessage());
        assertEquals(0, storage.saves);
    }

    @Test
    public void networkExceptionIsError() {
        network.error = new IOException("sem internet");
        LicenceResult result = tech.validateLicence(LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL);
        assertEquals(LicenceRules.RESULT_ERROR, result.getResult());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("sem internet"));
    }

    @Test
    public void staleCacheIsDeletedAndRevalidatedOnline() {
        CheckLicenceEntity stale = validCache(now);
        stale.setSavedLocalDatabaseAt(daysFrom(now, -8));
        storage.stored = stale;
        network.body = LicenceTestData.coreJson("2099-12-31T23:59:59", false, false, false);

        LicenceResult result = tech.validateLicence(LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL);
        assertEquals(LicenceRules.RESULT_SUCCESS, result.getResult());
        assertEquals(1, storage.deletes);
        assertEquals(1, network.urls.size());
        assertEquals(now, storage.stored.getSavedLocalDatabaseAt());
    }

    @Test
    public void staleCacheAndServerRefusalBlocks() {
        CheckLicenceEntity stale = validCache(now);
        stale.setSavedLocalDatabaseAt(daysFrom(now, -8));
        storage.stored = stale;
        network.status = 404;
        network.body = "{\"message\":\"Licença não encontrada\"}";

        LicenceResult result = tech.validateLicence(LICENCE_KEY, LicenceConfig.LICENCE_CHECK_URL);
        assertEquals(LicenceRules.RESULT_ERROR, result.getResult());
        assertNull(storage.stored);
    }

    @Test
    public void cacheWithOldCloudRunUrlIsDeletedAndRevalidatedOnCore() {
        CheckLicenceEntity old = validCache(now);
        old.setLicenceInternalKey(b64(OLD_CLOUD_RUN_URL));
        storage.stored = old;
        network.body = LicenceTestData.coreJson("2099-12-31T23:59:59", false, false, false);

        LicenceResult result = tech.validateLicence(LICENCE_KEY, b64(OLD_CLOUD_RUN_URL));
        assertEquals(LicenceRules.RESULT_SUCCESS, result.getResult());
        assertEquals(1, storage.deletes);
        assertEquals(1, network.urls.size());
        assertEquals(LicenceConfig.LICENCE_CHECK_URL, network.urls.get(0));
        // o cache novo guarda a URL oficial, então a próxima chamada não vai à rede
        assertEquals(LicenceConfig.LICENCE_CHECK_URL, storage.stored.getLicenceInternalKey());
        tech.validateLicence(LICENCE_KEY, b64(OLD_CLOUD_RUN_URL));
        assertEquals(1, network.urls.size());
    }

    @Test
    public void cacheWithCoreBase64IsAcceptedOffline() {
        CheckLicenceEntity cache = validCache(now);
        cache.setLicenceInternalKey(b64(LicenceConfig.LICENCE_CHECK_URL));
        storage.stored = cache;
        LicenceResult result = tech.validateLicence(LICENCE_KEY, b64(LicenceConfig.LICENCE_CHECK_URL));
        assertEquals(LicenceRules.RESULT_SUCCESS, result.getResult());
        assertTrue(network.urls.isEmpty());
        assertEquals(0, storage.deletes);
    }

    @Test
    public void cacheForAnotherKeyIsNotAccepted() {
        storage.stored = validCache(now);
        assertFalse(tech.validateExistsLicence("JYL-OTHER-KEY", LicenceConfig.LICENCE_CHECK_URL));
        assertNull(storage.stored);
    }
}
