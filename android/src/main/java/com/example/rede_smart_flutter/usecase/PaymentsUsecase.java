package com.example.rede_smart_flutter.usecase;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import static com.example.rede_smart_flutter.constants.RedeConstants.PAYMENT_REQUEST_CODE;
import static com.example.rede_smart_flutter.constants.RedeConstants.REPRINT_REQUEST_CODE;
import static com.example.rede_smart_flutter.constants.RedeConstants.REVERSAL_REQUEST_CODE;

import rede.smartrede.sdk.FlexTipoPagamento;
import rede.smartrede.sdk.RedePaymentValidationError;
import rede.smartrede.sdk.RedePayments;
import rede.smartrede.sdk.api.IRedeSdk;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.util.Log;

import com.example.rede_smart_flutter.constants.RedePaymentType;

public class PaymentsUsecase {
    private IRedeSdk iRedeSdk;
    private RedePayments redePayments;


    public PaymentsUsecase(IRedeSdk sdk, Context context) {
        this.iRedeSdk = sdk;
        this.redePayments = iRedeSdk.getRedePayments(context);
    }

    public void paymentTransaction(Activity activity, int amount, RedePaymentType paymentType) {
        try {
            Intent collectPaymentIntent = redePayments
                    .intentForPaymentBuilder(getRedeType(paymentType), amount)
                    .build();

            activity.startActivityForResult(collectPaymentIntent, PAYMENT_REQUEST_CODE);
        } catch (ActivityNotFoundException ex) {
            Log.e("Pagamentos", "Application Pagamentos not found");
        } catch (RedePaymentValidationError ex) {
            Log.e("Pagamentos", "Intent validation error");
        }
    }

    public void creditInstallmentIssuer(Activity activity, int amount, int installment) {
        try {
            Intent collectPaymentIntent = redePayments
                    .intentForPaymentBuilder(FlexTipoPagamento.CREDITO_PARCELADO_EMISSOR, amount).setInstallments(installment)
                    .build();

            activity.startActivityForResult(collectPaymentIntent, PAYMENT_REQUEST_CODE);
        } catch (ActivityNotFoundException ex) {
            Log.e("Pagamentos", "Application Pagamentos not found");
        } catch (RedePaymentValidationError ex) {
            Log.e("Pagamentos", "Intent validation error");
        }
    }

    public void creditInstallmentCustomer(Activity activity, int amount, int installment) {
        try {
            Intent collectPaymentIntent = redePayments
                    .intentForPaymentBuilder(FlexTipoPagamento.CREDITO_PARCELADO, amount).setInstallments(installment)
                    .build();

            activity.startActivityForResult(collectPaymentIntent, PAYMENT_REQUEST_CODE);
        } catch (ActivityNotFoundException ex) {
            Log.e("Pagamentos", "Application Pagamentos not found");
        } catch (RedePaymentValidationError ex) {
            Log.e("Pagamentos", "Intent validation error");
        }
    }

    public void reversalTransaction(Activity activity) {
        try {
            Intent reversal = redePayments.intentForReversal();
            activity.startActivityForResult(reversal, REVERSAL_REQUEST_CODE);
        }catch (ActivityNotFoundException e) {
            Log.e("Pagamentos", "Application Reversal not found");
        }
    }

    public void reprintTransaction(Activity activity) {
        try {
            Intent reversal = redePayments.intentForReprint();
            activity.startActivityForResult(reversal, REPRINT_REQUEST_CODE);
        }catch (ActivityNotFoundException e) {
            Log.e("Pagamentos", "Application Reprint not found");
        }
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
