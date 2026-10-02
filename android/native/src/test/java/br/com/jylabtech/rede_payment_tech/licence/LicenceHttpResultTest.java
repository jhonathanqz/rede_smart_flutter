package br.com.jylabtech.rede_payment_tech.licence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.Date;

public class LicenceHttpResultTest {
    private final Date now = new Date();

    private static String json(String expiredAt, boolean expired, boolean inactive, boolean canceled) {
        return LicenceTestData.coreJson(expiredAt, expired, inactive, canceled);
    }

    @Test
    public void ok200ValidLicenceIsSuccess() {
        LicenceResult result = LicenceRules.decideHttpResult(200, json("2099-12-31T23:59:59", false, false, false), now);
        assertEquals(LicenceRules.RESULT_SUCCESS, result.getResult());
        assertNotNull(result.getLicenceEntity());
    }

    @Test
    public void ok200ExpiredByDateIsError() {
        LicenceResult result = LicenceRules.decideHttpResult(200, json("2020-01-01T00:00:00", false, false, false), now);
        assertEquals(LicenceRules.RESULT_ERROR, result.getResult());
        assertNull(result.getLicenceEntity());
        assertEquals("Licença vencida.", result.getErrorMessage());
    }

    @Test
    public void ok200FlaggedExpiredIsError() {
        LicenceResult result = LicenceRules.decideHttpResult(200, json("2099-12-31T23:59:59", true, false, false), now);
        assertEquals(LicenceRules.RESULT_ERROR, result.getResult());
    }

    @Test
    public void ok200InactiveIsError() {
        LicenceResult result = LicenceRules.decideHttpResult(200, json("2099-12-31T23:59:59", false, true, false), now);
        assertEquals(LicenceRules.RESULT_ERROR, result.getResult());
        assertEquals("Licença inativa.", result.getErrorMessage());
    }

    @Test
    public void ok200CanceledIsError() {
        LicenceResult result = LicenceRules.decideHttpResult(200, json("2099-12-31T23:59:59", false, false, true), now);
        assertEquals(LicenceRules.RESULT_ERROR, result.getResult());
        assertEquals("Licença cancelada.", result.getErrorMessage());
    }

    @Test
    public void ok200InvalidJsonIsError() {
        assertEquals(LicenceRules.RESULT_ERROR, LicenceRules.decideHttpResult(200, "<html>oops</html>", now).getResult());
        assertEquals(LicenceRules.RESULT_ERROR, LicenceRules.decideHttpResult(200, "", now).getResult());
        assertEquals(LicenceRules.RESULT_ERROR, LicenceRules.decideHttpResult(200, null, now).getResult());
    }

    @Test
    public void ok200WithoutExpiredAtIsError() {
        LicenceResult result = LicenceRules.decideHttpResult(200, json(null, false, false, false), now);
        assertEquals(LicenceRules.RESULT_ERROR, result.getResult());
        assertEquals(LicenceRules.RESULT_ERROR, LicenceRules.decideHttpResult(200, "{}", now).getResult());
    }

    @Test
    public void errorStatusesUseServerMessage() {
        int[] codes = {404, 412, 428, 403, 400, 429, 500};
        for (int code : codes) {
            String message = "mensagem do servidor " + code;
            LicenceResult result = LicenceRules.decideHttpResult(code, "{\"message\":\"" + message + "\"}", now);
            assertEquals("HTTP " + code, LicenceRules.RESULT_ERROR, result.getResult());
            assertEquals(message, result.getErrorMessage());
            assertNull(result.getLicenceEntity());
        }
    }

    @Test
    public void errorWithEmptyOrNonJsonBodyUsesFallbackMessage() {
        assertEquals("Erro ao validar licença (HTTP 500)",
                LicenceRules.decideHttpResult(500, null, now).getErrorMessage());
        assertEquals("Erro ao validar licença (HTTP 502)",
                LicenceRules.decideHttpResult(502, "", now).getErrorMessage());
        assertEquals("Erro ao validar licença (HTTP 503)",
                LicenceRules.decideHttpResult(503, "<html>Bad gateway</html>", now).getErrorMessage());
        assertEquals("Erro ao validar licença (HTTP 404)",
                LicenceRules.decideHttpResult(404, "{\"message\":null}", now).getErrorMessage());
    }

    @Test
    public void resultStartsAsError() {
        assertEquals(LicenceRules.RESULT_ERROR, new LicenceResult().getResult());
    }
}
