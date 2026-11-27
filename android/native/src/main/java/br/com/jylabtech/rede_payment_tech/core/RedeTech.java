package br.com.jylabtech.rede_payment_tech.core;

import android.content.Context;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import android.util.Log;
import android.app.Activity;

import br.com.jylabtech.rede_payment_tech.presentation.AuthPresenter;
import br.com.jylabtech.rede_payment_tech.presentation.RedePresenter;
import br.com.jylabtech.rede_payment_tech.presentation.TerminalPresenter;

import rede.smartrede.sdk.api.IRedeSdk;

public class RedeTech {
    private Context context;
    final MethodChannel mChannel;
    private Activity mActivity;
    private RedePresenter redePresenter;
    private TerminalPresenter terminalPresenter;
    private AuthPresenter authPresenter;

    private IRedeSdk redeSdk;

    public RedeTech(Context context, MethodChannel channel, Activity activity) {
        this.mChannel = channel;
        this.context = context;
        this.mActivity = activity;

        redeSdk = IRedeSdk.newInstance(context);
        this.redePresenter = new RedePresenter(this.context, this.redeSdk);
    }

    /**
     * Executa a operação. Falhas do SDK (app da Rede ausente, dados recusados, SDK indisponível
     * ou erro inesperado) respondem o {@code result} com erro em vez de serem engolidas ou
     * derrubarem o app. Operações com tela da Rede (pagamento, estorno, reimpressão,
     * autenticação) respondem depois, no onActivityResult.
     */
    public void initPayment(MethodCall call, MethodChannel.Result result) {
        try {
            dispatch(call, result);
        } catch (RuntimeException e) {
            RedeOperationException error = RedeOperationException.from(e);
            Log.e("RedeTech", "Falha em " + call.method + ": " + error.getMessage());
            result.error(error.getCode(), error.getMessage(), null);
        }
    }

    private void dispatch(MethodCall call, MethodChannel.Result result) {
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
            final long amount;
            try {
                amount = MethodArguments.requireLong(call.argument("amount"), "amount");
            } catch (IllegalArgumentException e) {
                result.error(MethodArguments.INVALID_ARGUMENT, e.getMessage(), null);
                return;
            }
            String description = call.argument("description");
            if(this.authPresenter == null) {
                this.authPresenter = new AuthPresenter(this.context, this.mActivity, this.redeSdk);
            }

            if(call.method.startsWith("AuthOnlyPin")) {
                this.authPresenter.authWithOnlyPin(description, amount);
            }else if(call.method.startsWith("AuthChangePin")) {
                this.authPresenter.authWithChangePin(description, amount);
            }else if(call.method.startsWith("AuthValueAndPin")) {
                this.authPresenter.authWithValueAndPin(description, amount);
            } else {
                result.notImplemented();
            }
            return;
        }

        if (call.method.startsWith("Payment")) {
            final long amount;
            try {
                amount = MethodArguments.requireLong(call.argument("amount"), "amount");
            } catch (IllegalArgumentException e) {
                result.error(MethodArguments.INVALID_ARGUMENT, e.getMessage(), null);
                return;
            }

            if(call.method.startsWith("PaymentCreditParc")) {
                final int installment;
                try {
                    installment = MethodArguments.requireInt(call.argument("installment"), "installment");
                } catch (IllegalArgumentException e) {
                    result.error(MethodArguments.INVALID_ARGUMENT, e.getMessage(), null);
                    return;
                }
                if(call.method.startsWith("PaymentCreditParcIssuer")) {
                    this.redePresenter.creditInstallmentIssuer(mActivity, amount, installment);
                } else if(call.method.startsWith("PaymentCreditParcCustomer")) {
                    this.redePresenter.creditInstallmentCustomer(mActivity, amount, installment);
                } else {
                    result.notImplemented();
                }
                return;
            } else if(call.method.startsWith("PaymentCredit")) {
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
            this.redePresenter.reversalTransaction(mActivity);
        }else {
            result.notImplemented();
        }
    }
}
