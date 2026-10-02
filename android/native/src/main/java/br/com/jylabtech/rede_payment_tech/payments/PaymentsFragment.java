package br.com.jylabtech.rede_payment_tech.payments;

import io.flutter.plugin.common.MethodChannel;
import java.util.HashMap;
import java.util.Map;

public class PaymentsFragment implements PaymentsContract {
    // Nomes dos métodos enviados ao Dart (as constantes não existiam e impediam a compilação).
    private static final String ON_TRANSACTION_SUCCESS = "onTransactionSuccess";
    private static final String ON_ERROR = "onError";
    private static final String ON_MESSAGE = "onMessage";
    private static final String ON_FINISHED_RESPONSE = "onFinishedResponse";
    private static final String ON_LOADING = "onLoading";
    private static final String ON_ABORTED_SUCCESSFULLY = "onAbortedSuccessfully";

    final MethodChannel channel;

    public PaymentsFragment(MethodChannel channel) {
        this.channel = channel;
    }
    @Override
    public void onTransactionSuccess() {
        this.channel.invokeMethod(ON_TRANSACTION_SUCCESS, true);
    }

    @Override
    public void onError(String message) {
        this.channel.invokeMethod(ON_ERROR, message);
    }

    @Override
    public void onMessage(String message) {
        this.channel.invokeMethod(ON_MESSAGE, message);
    }

    @Override
    public void onFinishedResponse(String message) {
        this.channel.invokeMethod(ON_FINISHED_RESPONSE, message);
    }

    @Override
    public void onLoading(boolean show) {
        this.channel.invokeMethod(ON_LOADING, show);
    }
    @Override
    public void onAbortedSuccessfully() {
        this.channel.invokeMethod(ON_ABORTED_SUCCESSFULLY, true);
    }

}
