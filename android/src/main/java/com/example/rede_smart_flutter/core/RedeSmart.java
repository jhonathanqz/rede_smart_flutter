package com.example.rede_smart_flutter.core;

import android.content.Context;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import android.util.Log;
import android.app.Activity;

import com.example.rede_smart_flutter.presentation.RedePresenter;

import rede.smartrede.sdk.api.IRedeSdk;

public class RedeSmart {
    private Context context;
    final MethodChannel mChannel;
    private Activity mActivity;
    private RedePresenter redePresenter;

    private IRedeSdk redeSdk;

    public RedeSmart(Context context, MethodChannel channel, Activity activity) {
        this.mChannel = channel;
        this.context = context;
        this.mActivity = activity;

        redeSdk = IRedeSdk.newInstance(context);
        this.redePresenter = new RedePresenter(this.context, this.redeSdk);
    }

    public void initPayment(MethodCall call, MethodChannel.Result result) {
        if (call.method.startsWith("Payment")) {
            int amount = call.argument("amount");


            if(call.method.startsWith("PaymentCredit")) {
                this.redePresenter.creditPayment(mActivity, amount);
                return;
            } else if(call.method.startsWith("PaymentDebit")) {
                this.redePresenter.debitPayment(mActivity, amount);
                return;
            } else if(call.method.startsWith("PaymentVoucher")) {
                this.redePresenter.voucherPayment(mActivity, amount);
                return;
            } else if(call.method.startsWith("PaymentPix")) {
                this.redePresenter.pixPayment(mActivity, amount);
                return;
            } else {
                result.notImplemented();
                return;
            }
        }

        if(call.method.startsWith("RedeReprint")) {
            this.redePresenter.reprintTransaction(mActivity);
        } else if(call.method.startsWith("RedeReversal")) {
            this.redePresenter.reprintTransaction(mActivity);
        }else if(call.method.startsWith("RedeSimSerialNumber")) {
            String response = this.redePresenter.getSimCardSerialNumber();
            result.success(response);
        }else if(call.method.startsWith("RedePosSerialNumber")) {
            String response = this.redePresenter.getSerialNumber();
            result.success(response);
        }else if(call.method.startsWith("RedeIMEI")) {
            String response = this.redePresenter.getIMEI();
            result.success(response);
        } else {
            result.notImplemented();
        }
    }
}
