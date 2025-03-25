package com.example.rede_smart_flutter.core;

import android.content.Context;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import android.util.Log;
import android.app.Activity;

import com.example.rede_smart_flutter.presentation.AuthPresenter;
import com.example.rede_smart_flutter.presentation.RedePresenter;
import com.example.rede_smart_flutter.presentation.TerminalPresenter;

import rede.smartrede.sdk.api.IRedeSdk;

public class RedeSmart {
    private Context context;
    final MethodChannel mChannel;
    private Activity mActivity;
    private RedePresenter redePresenter;
    private TerminalPresenter terminalPresenter;
    private AuthPresenter authPresenter;

    private IRedeSdk redeSdk;

    public RedeSmart(Context context, MethodChannel channel, Activity activity) {
        this.mChannel = channel;
        this.context = context;
        this.mActivity = activity;

        redeSdk = IRedeSdk.newInstance(context);
        this.redePresenter = new RedePresenter(this.context, this.redeSdk);
    }

    public void initPayment(MethodCall call, MethodChannel.Result result) {
        if (call.method.startsWith("POS")) {
            if(this.terminalPresenter == null) {
                this.terminalPresenter = new TerminalPresenter(this.redeSdk);
            }

            if(call.method.startsWith("POSSimSerialNumber")) {
                String response = this.terminalPresenter.getSimCardSerialNumber();
                result.success(response);
            }else if(call.method.startsWith("POSSerialNumber")) {
                String response = this.terminalPresenter.getSerialNumber();
                result.success(response);
            }else if(call.method.startsWith("POSIMEI")) {
                String response = this.terminalPresenter.getIMEI();
                result.success(response);
            } else {
                result.notImplemented();
            }
            return;
        }

        if (call.method.startsWith("Auth")) {
            if(this.authPresenter == null) {
                this.authPresenter = new AuthPresenter(this.context, this.mActivity, this.redeSdk);
            }
            int amount = call.argument("amount");
            String description = call.argument("description");

            if(call.method.startsWith("Auth")) {
                this.authPresenter.authWithOnlyPin(description, amount);
            }else if(call.method.startsWith("POSSerialNumber")) {
                this.authPresenter.authWithChangePin(description, amount);
            }else if(call.method.startsWith("POSIMEI")) {
                this.authPresenter.authWithValueAndPin(description, amount);
            } else {
                result.notImplemented();
            }
            return;
        }

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
            }else if(call.method.startsWith("PaymentCreditParc")) {
                int installment = call.argument("installment");
                if(call.method.startsWith("PaymentCreditParcIssuer")) {
                    this.redePresenter.creditInstallmentIssuer(mActivity, amount, installment);
                } else if(call.method.startsWith("PaymentCreditParcCustomer")) {
                    this.redePresenter.creditInstallmentCustomer(mActivity, amount, installment);
                }
                return;
            } else {
                result.notImplemented();
                return;
            }
        }

        if(call.method.startsWith("RedeReprint")) {
            this.redePresenter.reprintTransaction(mActivity);
        } else if(call.method.startsWith("RedeReversal")) {
            this.redePresenter.reversalTransaction(mActivity);
        }else {
            result.notImplemented();
        }
    }
}
