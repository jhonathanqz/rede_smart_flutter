package br.com.jylabtech.rede_payment_tech.licence;

import static br.com.jylabtech.rede_payment_tech.licence.LicenceTestData.OLD_CLOUD_RUN_URL;
import static br.com.jylabtech.rede_payment_tech.licence.LicenceTestData.b64;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LicenceConfigTest {
    private static final String CORE = LicenceConfig.LICENCE_CHECK_URL;

    @Test
    public void constantPointsToCore() {
        assertEquals("https://portal-api.jylabtech.com.br/pos-payments/licence/check", CORE);
    }

    @Test
    public void acceptsCorePlainText() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(CORE));
    }

    @Test
    public void acceptsCoreBase64() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "aHR0cHM6Ly9wb3J0YWwtYXBpLmp5bGFidGVjaC5jb20uYnIvcG9zLXBheW1lbnRzL2xpY2VuY2UvY2hlY2s="));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(b64(CORE)));
    }

    @Test
    public void acceptsInvoiceAndSubscriptionVariants() {
        String invoice = CORE + "/invoice";
        String subscription = CORE + "/subscription";
        assertEquals(invoice, LicenceConfig.resolveLicenceUrl(invoice));
        assertEquals(subscription, LicenceConfig.resolveLicenceUrl(subscription));
        assertEquals(invoice, LicenceConfig.resolveLicenceUrl(b64(invoice)));
        assertEquals(subscription, LicenceConfig.resolveLicenceUrl(b64(subscription)));
    }

    @Test
    public void oldCloudRunUrlFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(OLD_CLOUD_RUN_URL));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(b64(OLD_CLOUD_RUN_URL)));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "aHR0cHM6Ly9wb3MtcGF5bWVudHMtYXBpLTU3NzQ2NDIzNTQwOC5zb3V0aGFtZXJpY2EtZWFzdDEucnVuLmFwcC9wb3MtcGF5bWVudHMvbGljZW5jZS9jaGVjaw=="));
    }

    @Test
    public void otherHostFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl("https://evil.com/pos-payments/licence/check"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(b64("https://evil.com/pos-payments/licence/check")));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl("https://api.jylabtech.com.br/pos-payments/licence/check"));
    }

    @Test
    public void maliciousSuffixOrPathFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://portal-api.jylabtech.com.br.evil.com/pos-payments/licence/check"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://evil.com/portal-api.jylabtech.com.br/pos-payments/licence/check"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://evil.com/?u=https://portal-api.jylabtech.com.br/pos-payments/licence/check"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://portal-api.jylabtech.com.br/other/pos-payments/licence/check"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://portal-api.jylabtech.com.br/pos-payments/licence/checkout"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://portal-api.jylabtech.com.br:8443/pos-payments/licence/check"));
    }

    @Test
    public void userInfoFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://portal-api.jylabtech.com.br@evil.com/pos-payments/licence/check"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "https://user:pass@portal-api.jylabtech.com.br/pos-payments/licence/check"));
    }

    @Test
    public void plainHttpFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                "http://portal-api.jylabtech.com.br/pos-payments/licence/check"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(
                b64("http://portal-api.jylabtech.com.br/pos-payments/licence/check")));
    }

    @Test
    public void emptyOrNullFallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(null));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(""));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl("   "));
    }

    @Test
    public void garbageBase64FallsBackToCore() {
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl("!!!not-base64###"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl("abcde"));
        assertEquals(CORE, LicenceConfig.resolveLicenceUrl(b64("qualquer coisa")));
    }

    @Test
    public void isOfficialLicenceInternalKey() {
        org.junit.Assert.assertTrue(LicenceConfig.isOfficialLicenceInternalKey(CORE));
        org.junit.Assert.assertTrue(LicenceConfig.isOfficialLicenceInternalKey(b64(CORE)));
        org.junit.Assert.assertTrue(LicenceConfig.isOfficialLicenceInternalKey(b64(CORE + "/invoice")));
        org.junit.Assert.assertFalse(LicenceConfig.isOfficialLicenceInternalKey(OLD_CLOUD_RUN_URL));
        org.junit.Assert.assertFalse(LicenceConfig.isOfficialLicenceInternalKey(b64(OLD_CLOUD_RUN_URL)));
        org.junit.Assert.assertFalse(LicenceConfig.isOfficialLicenceInternalKey(
                "https://portal-api.jylabtech.com.br.evil.com/pos-payments/licence/check"));
        org.junit.Assert.assertFalse(LicenceConfig.isOfficialLicenceInternalKey(""));
        org.junit.Assert.assertFalse(LicenceConfig.isOfficialLicenceInternalKey(null));
        org.junit.Assert.assertFalse(LicenceConfig.isOfficialLicenceInternalKey("!!lixo"));
    }
}
