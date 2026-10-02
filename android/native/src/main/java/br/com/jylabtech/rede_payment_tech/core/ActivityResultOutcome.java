package br.com.jylabtech.rede_payment_tech.core;

import static br.com.jylabtech.rede_payment_tech.constants.RedeConstants.AUTH_REQUEST_CODE;
import static br.com.jylabtech.rede_payment_tech.constants.RedeConstants.PAYMENT_REQUEST_CODE;
import static br.com.jylabtech.rede_payment_tech.constants.RedeConstants.REPRINT_REQUEST_CODE;
import static br.com.jylabtech.rede_payment_tech.constants.RedeConstants.REVERSAL_REQUEST_CODE;

/**
 * Decide (sem dependências Android) como responder o retorno de uma tela da Rede, garantindo que
 * todo retorno com request code do plugin gere exatamente uma resposta ao Dart.
 */
public enum ActivityResultOutcome {
    /** Request code de outra parte do app: o plugin não consome. */
    NOT_HANDLED,
    /** Pagamento/estorno concluído com dados da transação. */
    TRANSACTION_RESULT,
    /** Pagamento/estorno cancelado com dados da transação. */
    TRANSACTION_CANCELLED,
    /** Pagamento/estorno cancelado sem dados (resposta em texto, como antes). */
    TRANSACTION_CANCELLED_NO_DATA,
    /** Pagamento/estorno com RESULT_OK mas sem dados: antes ficava sem resposta. */
    TRANSACTION_NO_DATA,
    AUTH_OK,
    AUTH_CANCELLED,
    REPRINT_OK,
    REPRINT_CANCELLED,
    /** resultCode diferente de OK/CANCELED: antes ficava sem resposta (ou JSON vazio). */
    UNEXPECTED_RESULT;

    /** Mesmos valores de android.app.Activity.RESULT_OK / RESULT_CANCELED. */
    public static final int RESULT_OK = -1;
    public static final int RESULT_CANCELED = 0;

    public static boolean isPluginRequestCode(int requestCode) {
        return requestCode == PAYMENT_REQUEST_CODE
                || requestCode == REVERSAL_REQUEST_CODE
                || requestCode == AUTH_REQUEST_CODE
                || requestCode == REPRINT_REQUEST_CODE;
    }

    public static ActivityResultOutcome classify(int requestCode, int resultCode, boolean hasData) {
        if (!isPluginRequestCode(requestCode)) {
            return NOT_HANDLED;
        }
        if (resultCode != RESULT_OK && resultCode != RESULT_CANCELED) {
            return UNEXPECTED_RESULT;
        }
        boolean ok = resultCode == RESULT_OK;
        if (requestCode == PAYMENT_REQUEST_CODE || requestCode == REVERSAL_REQUEST_CODE) {
            if (ok) {
                return hasData ? TRANSACTION_RESULT : TRANSACTION_NO_DATA;
            }
            return hasData ? TRANSACTION_CANCELLED : TRANSACTION_CANCELLED_NO_DATA;
        }
        if (requestCode == AUTH_REQUEST_CODE) {
            return ok ? AUTH_OK : AUTH_CANCELLED;
        }
        return ok ? REPRINT_OK : REPRINT_CANCELLED;
    }
}
