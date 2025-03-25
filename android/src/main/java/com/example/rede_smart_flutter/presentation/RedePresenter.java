package com.example.rede_smart_flutter.presentation;

import android.app.Activity;
import android.content.Context;

import com.example.rede_smart_flutter.constants.RedePaymentType;
import com.example.rede_smart_flutter.usecase.PaymentsUsecase;
import com.example.rede_smart_flutter.usecase.TerminalUsecase;

import rede.smartrede.sdk.api.IRedeSdk;

public class RedePresenter {
    private PaymentsUsecase paymentsUsecase;
    private TerminalUsecase terminalUsecase;

    public RedePresenter(Context context, IRedeSdk sdk) {
        this.paymentsUsecase = new PaymentsUsecase(sdk, context);
        this.terminalUsecase = new TerminalUsecase(sdk);
    }

    public void creditPayment(Activity activity, int amount) {
        paymentsUsecase.paymentTransaction(activity, amount, RedePaymentType.CREDIT);
    }

    public void debitPayment(Activity activity, int amount) {
        paymentsUsecase.paymentTransaction(activity, amount, RedePaymentType.DEBIT);
    }

    public void voucherPayment(Activity activity, int amount) {
        paymentsUsecase.paymentTransaction(activity, amount, RedePaymentType.VOUCHER);
    }

    public void pixPayment(Activity activity, int amount) {
        paymentsUsecase.paymentTransaction(activity, amount, RedePaymentType.PIX);
    }

    public void creditInstallmentIssuer(Activity activity, int amount, int installments) {
        paymentsUsecase.creditInstallmentIssuer(activity, amount, installments);
    }

    public void creditInstallmentCustomer(Activity activity, int amount, int installment){
        paymentsUsecase.creditInstallmentCustomer(activity, amount, installment);
    }

    public void reversalTransaction(Activity activity) {
        paymentsUsecase.reversalTransaction(activity);
    }

    public void reprintTransaction(Activity activity) {
        paymentsUsecase.reprintTransaction(activity);
    }

    public String getSimCardSerialNumber() {
        return terminalUsecase.getSimCardSerialNumber();
    }

    public String getIMEI() {
        return terminalUsecase.getIMEI();
    }

    public String getSerialNumber() {
        return terminalUsecase.getSerialNumber();
    }
}
