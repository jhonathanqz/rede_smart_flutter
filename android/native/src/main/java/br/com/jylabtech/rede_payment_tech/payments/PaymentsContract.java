package br.com.jylabtech.rede_payment_tech.payments;

//Contract class
interface PaymentsContract {
    void onTransactionSuccess();

    void onError(String message);

    void onMessage(String message);

    void onFinishedResponse(String message);

    void onLoading(boolean show);

    void onAbortedSuccessfully();
}

