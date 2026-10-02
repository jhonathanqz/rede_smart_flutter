package br.com.jylabtech.rede_payment_tech.core;

import static br.com.jylabtech.rede_payment_tech.constants.RedeConstants.AUTH_REQUEST_CODE;
import static br.com.jylabtech.rede_payment_tech.constants.RedeConstants.PAYMENT_REQUEST_CODE;
import static br.com.jylabtech.rede_payment_tech.constants.RedeConstants.REPRINT_REQUEST_CODE;
import static br.com.jylabtech.rede_payment_tech.constants.RedeConstants.REVERSAL_REQUEST_CODE;
import static br.com.jylabtech.rede_payment_tech.core.ActivityResultOutcome.RESULT_CANCELED;
import static br.com.jylabtech.rede_payment_tech.core.ActivityResultOutcome.RESULT_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class ActivityResultOutcomeTest {

    private static final int[] PLUGIN_CODES = {
            PAYMENT_REQUEST_CODE, REVERSAL_REQUEST_CODE, AUTH_REQUEST_CODE, REPRINT_REQUEST_CODE
    };

    @Test
    public void foreignRequestCodeIsNotHandled() {
        assertEquals(ActivityResultOutcome.NOT_HANDLED, ActivityResultOutcome.classify(1, RESULT_OK, true));
    }

    @Test
    public void everyPluginRequestCodeAndResultCombinationProducesAReply() {
        int[] resultCodes = {RESULT_OK, RESULT_CANCELED, 1, 42, -2};
        for (int request : PLUGIN_CODES) {
            for (int resultCode : resultCodes) {
                for (boolean hasData : new boolean[]{true, false}) {
                    assertNotEquals(ActivityResultOutcome.NOT_HANDLED,
                            ActivityResultOutcome.classify(request, resultCode, hasData));
                }
            }
        }
    }

    @Test
    public void paymentOkWithoutDataIsAnErrorInsteadOfSilence() {
        assertEquals(ActivityResultOutcome.TRANSACTION_NO_DATA,
                ActivityResultOutcome.classify(PAYMENT_REQUEST_CODE, RESULT_OK, false));
        assertEquals(ActivityResultOutcome.TRANSACTION_NO_DATA,
                ActivityResultOutcome.classify(REVERSAL_REQUEST_CODE, RESULT_OK, false));
    }

    @Test
    public void paymentAndReversalOutcomes() {
        assertEquals(ActivityResultOutcome.TRANSACTION_RESULT,
                ActivityResultOutcome.classify(PAYMENT_REQUEST_CODE, RESULT_OK, true));
        assertEquals(ActivityResultOutcome.TRANSACTION_CANCELLED,
                ActivityResultOutcome.classify(REVERSAL_REQUEST_CODE, RESULT_CANCELED, true));
        assertEquals(ActivityResultOutcome.TRANSACTION_CANCELLED_NO_DATA,
                ActivityResultOutcome.classify(PAYMENT_REQUEST_CODE, RESULT_CANCELED, false));
    }

    @Test
    public void authAndReprintOutcomes() {
        assertEquals(ActivityResultOutcome.AUTH_OK,
                ActivityResultOutcome.classify(AUTH_REQUEST_CODE, RESULT_OK, false));
        assertEquals(ActivityResultOutcome.AUTH_CANCELLED,
                ActivityResultOutcome.classify(AUTH_REQUEST_CODE, RESULT_CANCELED, true));
        assertEquals(ActivityResultOutcome.REPRINT_OK,
                ActivityResultOutcome.classify(REPRINT_REQUEST_CODE, RESULT_OK, false));
        assertEquals(ActivityResultOutcome.REPRINT_CANCELLED,
                ActivityResultOutcome.classify(REPRINT_REQUEST_CODE, RESULT_CANCELED, false));
    }

    @Test
    public void unknownResultCodeIsAnError() {
        for (int request : PLUGIN_CODES) {
            assertEquals(ActivityResultOutcome.UNEXPECTED_RESULT,
                    ActivityResultOutcome.classify(request, 1, true));
        }
    }

    @Test
    public void resultConstantsMatchAndroid() {
        assertEquals(android.app.Activity.RESULT_OK, RESULT_OK);
        assertEquals(android.app.Activity.RESULT_CANCELED, RESULT_CANCELED);
    }
}
