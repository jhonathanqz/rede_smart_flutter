package br.com.jylabtech.rede_payment_tech.usecase;

import static br.com.jylabtech.rede_payment_tech.constants.RedeConstants.PAYMENT_REQUEST_CODE;
import static br.com.jylabtech.rede_payment_tech.constants.RedeConstants.REPRINT_REQUEST_CODE;
import static br.com.jylabtech.rede_payment_tech.constants.RedeConstants.REVERSAL_REQUEST_CODE;

import rede.smartrede.sdk.FlexTipoPagamento;
import rede.smartrede.sdk.PaymentIntentBuilder;
import rede.smartrede.sdk.RedePaymentValidationError;
import rede.smartrede.sdk.RedePayments;
import rede.smartrede.sdk.api.IRedeSdk;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.util.Log;

import br.com.jylabtech.rede_payment_tech.constants.RedePaymentType;
import br.com.jylabtech.rede_payment_tech.core.RedeOperationException;

public class PaymentsUsecase {
    private IRedeSdk iRedeSdk;
    private RedePayments redePayments;


    public PaymentsUsecase(IRedeSdk sdk, Context context) {
        this.iRedeSdk = sdk;
        this.redePayments = iRedeSdk.getRedePayments(context);
    }

    public void paymentTransaction(Activity activity, long amount, RedePaymentType paymentType) {
        startPayment(activity, getRedeType(paymentType), amount, null);
    }

    public void creditInstallmentIssuer(Activity activity, long amount, int installment) {
        startPayment(activity, FlexTipoPagamento.CREDITO_PARCELADO_EMISSOR, amount, installment);
    }

    public void creditInstallmentCustomer(Activity activity, long amount, int installment) {
        startPayment(activity, FlexTipoPagamento.CREDITO_PARCELADO, amount, installment);
    }

    public void reversalTransaction(Activity activity) {
        requireActivity(activity);
        try {
            Intent reversal = redePayments.intentForReversal();
            activity.startActivityForResult(reversal, REVERSAL_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Log.e("Pagamentos", "Application Reversal not found");
            throw appNotFound(e);
        }
    }

    public void reprintTransaction(Activity activity) {
        requireActivity(activity);
        try {
            Intent reprint = redePayments.intentForReprint();
            activity.startActivityForResult(reprint, REPRINT_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Log.e("Pagamentos", "Application Reprint not found");
            throw appNotFound(e);
        }
    }

    /**
     * Abre a tela de pagamento da Rede. Falhas viram {@link RedeOperationException}, que o
     * RedeTech devolve ao Dart (antes eram só logadas e o Future nunca completava).
     */
    private void startPayment(Activity activity, FlexTipoPagamento type, long amount, Integer installments) {
        requireActivity(activity);
        try {
            PaymentIntentBuilder builder = redePayments.intentForPaymentBuilder(type, amount);
            if (installments != null) {
                builder.setInstallments(installments);
            }
            Intent collectPaymentIntent = builder.build();
            activity.startActivityForResult(collectPaymentIntent, PAYMENT_REQUEST_CODE);
        } catch (ActivityNotFoundException ex) {
            Log.e("Pagamentos", "Application Pagamentos not found");
            throw appNotFound(ex);
        } catch (RedePaymentValidationError ex) {
            Log.e("Pagamentos", "Intent validation error");
            String detail = ex.getMessage() != null ? ex.getMessage() : String.valueOf(ex.getValidationError());
            throw new RedeOperationException(RedeOperationException.INVALID_PAYMENT,
                    "Dados do pagamento recusados pelo SDK da Rede: " + detail, ex);
        }
    }

    private static void requireActivity(Activity activity) {
        if (activity == null) {
            throw new RedeOperationException(RedeOperationException.ACTIVITY_NOT_ATTACHED,
                    "Plugin não está anexado a uma Activity no momento.");
        }
    }

    private static RedeOperationException appNotFound(ActivityNotFoundException e) {
        return new RedeOperationException(RedeOperationException.REDE_APP_NOT_FOUND,
                "Aplicativo de pagamentos da Rede não encontrado no terminal.", e);
    }

    private FlexTipoPagamento getRedeType(RedePaymentType type) {
        if(type.equals(RedePaymentType.CREDIT)) {
            return FlexTipoPagamento.CREDITO_A_VISTA;
        }

        if(type.equals(RedePaymentType.DEBIT)) {
            return FlexTipoPagamento.DEBITO;
        }

        if(type.equals(RedePaymentType.PIX)) {
            return FlexTipoPagamento.PIX;
        }

        if(type.equals(RedePaymentType.VOUCHER)) {
            return FlexTipoPagamento.VOUCHER;
        }

        return FlexTipoPagamento.DEBITO;
    }
}
