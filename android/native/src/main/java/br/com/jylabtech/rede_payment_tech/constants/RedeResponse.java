package br.com.jylabtech.rede_payment_tech.constants;

import java.util.Calendar;

import rede.smartrede.sdk.Payment;
import rede.smartrede.sdk.Receipt;

public class RedeResponse {
    private String storeName;
    private Calendar date;
    private Calendar hour;
    private double amount;
    private String auto;
    private String nsu;
    private String issuerName;
    private String cardHolderName;
    private String terminalNumber;
    private String cv;
    private String aid;
    private String arqc;
    private int installments;
    private double installmentValue;
    private String document;
    private String operationType;
    private String maskedPan;
    private String pinTransactionId;
    private String paymentType;
    private String status;
    private String referenceId;
    private String transactionNumber;
    private String method;

    public void buildPaymentResponse(String method, Payment payment) {
        Receipt receipt = payment.getReceipt();
        this.method = method;

       this.paymentType = payment.getPaymentType().toString();
       this.status = payment.getStatus().toString();
       this.referenceId = payment.getReferenceId();
       this.transactionNumber= payment.getTransactionNumber();

        this.storeName = receipt.getStoreName();
        this.date = receipt.getDate();
        this.hour = receipt.getDate();
        this.amount = receipt.getValue();
        this.auto = receipt.getAUTO();
        this.nsu = receipt.getNSU();
        this.issuerName = receipt.getIssuerName();
        this.cardHolderName = receipt.getCardHolderName();
        this.terminalNumber = receipt.getTerminalNumber();
        this.cv = receipt.getCV();
        this.aid = receipt.getAID();
        this.arqc = receipt.getARQC();
        this.installments = receipt.getInstallments();
        this.installmentValue = receipt.getInstallmentValue();
        this.document = receipt.getCNPJ();
        this.operationType = receipt.getOperationType();
        this.maskedPan = receipt.getMaskedPan();
        this.pinTransactionId = receipt.getPixTransactionId();
    }

    public void buildCancelledResponse(String method, Payment payment) {
        this.method = method;

        this.paymentType = payment.getPaymentType().toString();
        this.status = payment.getStatus().toString();
        this.referenceId = payment.getReferenceId();
        this.transactionNumber= payment.getTransactionNumber();
    }

    public void buildPrintResponse(String status) {
        this.method = "Print";
        this.status = status;
    }

    public void buildAuthResponse(String status) {
        this.method = "Auth";
        this.status = status;
    }
}
