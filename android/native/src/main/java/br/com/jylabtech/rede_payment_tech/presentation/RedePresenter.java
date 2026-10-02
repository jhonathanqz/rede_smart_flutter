package br.com.jylabtech.rede_payment_tech.presentation;

import android.app.Activity;
import android.content.Context;

import br.com.jylabtech.rede_payment_tech.constants.RedePaymentType;
import br.com.jylabtech.rede_payment_tech.usecase.PaymentsUsecase;

import rede.smartrede.sdk.api.IRedeSdk;

public class RedePresenter {
    private PaymentsUsecase paymentsUsecase;

    public RedePresenter(Context context, IRedeSdk sdk) {
        this.paymentsUsecase = new PaymentsUsecase(sdk, context);
    }

    public void creditPayment(Activity activity, long amount) {
        paymentsUsecase.paymentTransaction(activity, amount, RedePaymentType.CREDIT);
    }

    public void debitPayment(Activity activity, long amount) {
        paymentsUsecase.paymentTransaction(activity, amount, RedePaymentType.DEBIT);
    }

    public void voucherPayment(Activity activity, long amount) {
        paymentsUsecase.paymentTransaction(activity, amount, RedePaymentType.VOUCHER);
    }

    public void pixPayment(Activity activity, long amount) {
        paymentsUsecase.paymentTransaction(activity, amount, RedePaymentType.PIX);
    }

    public void creditInstallmentIssuer(Activity activity, long amount, int installments) {
        paymentsUsecase.creditInstallmentIssuer(activity, amount, installments);
    }

    public void creditInstallmentCustomer(Activity activity, long amount, int installment){
        paymentsUsecase.creditInstallmentCustomer(activity, amount, installment);
    }

    public void reversalTransaction(Activity activity) {
        paymentsUsecase.reversalTransaction(activity);
    }

    public void reprintTransaction(Activity activity) {
        paymentsUsecase.reprintTransaction(activity);
    }
}
