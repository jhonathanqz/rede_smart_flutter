package com.example.rede_smart_flutter.payments;

import io.flutter.plugin.common.MethodChannel;
import java.util.HashMap;
import java.util.Map;

public class PaymentsFragment implements PaymentsContract {
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
