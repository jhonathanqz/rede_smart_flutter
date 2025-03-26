enum RedePaymentType {
  credit,
  debit,
  voucher,
  pix,
  creditParcIssue,
  creditParcCustomer;

  String get method {
    switch (this) {
      case RedePaymentType.credit:
        return 'PaymentCredit';
      case RedePaymentType.debit:
        return 'PaymentDebit';
      case RedePaymentType.voucher:
        return 'PaymentVoucher';
      case RedePaymentType.pix:
        return 'PaymentPix';
      case RedePaymentType.creditParcIssue:
        return 'PaymentCreditParcIssuer';
      case RedePaymentType.creditParcCustomer:
        return 'PaymentCreditParcCustomer';
    }
  }
}
