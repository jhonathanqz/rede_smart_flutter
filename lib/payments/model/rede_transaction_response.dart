// ignore_for_file: public_member_api_docs, sort_constructors_first
import 'dart:convert';

class RedeTransactionResponse {
  final String storeName;
  final String date;
  final String hour;
  final double amount;
  final String auto;
  final String nsu;
  final String issuerName;
  final String cardHolderName;
  final String terminalNumber;
  final String cv;
  final String aid;
  final String arqc;
  final int installments;
  final double installmentValue;
  final String document;
  final String operationType;
  final String maskedPan;
  final String pinTransactionId;
  final String paymentType;
  final String status;
  final String referenceId;
  final String transactionNumber;
  final String method;

  RedeTransactionResponse({
    required this.storeName,
    required this.date,
    required this.hour,
    required this.amount,
    required this.auto,
    required this.nsu,
    required this.issuerName,
    required this.cardHolderName,
    required this.terminalNumber,
    required this.cv,
    required this.aid,
    required this.arqc,
    required this.installments,
    required this.installmentValue,
    required this.document,
    required this.operationType,
    required this.maskedPan,
    required this.pinTransactionId,
    required this.paymentType,
    required this.status,
    required this.referenceId,
    required this.transactionNumber,
    required this.method,
  });

  RedeTransactionResponse copyWith({
    String? storeName,
    String? date,
    String? hour,
    double? amount,
    String? auto,
    String? nsu,
    String? issuerName,
    String? cardHolderName,
    String? terminalNumber,
    String? cv,
    String? aid,
    String? arqc,
    int? installments,
    double? installmentValue,
    String? document,
    String? operationType,
    String? maskedPan,
    String? pinTransactionId,
    String? paymentType,
    String? status,
    String? referenceId,
    String? transactionNumber,
    String? method,
  }) {
    return RedeTransactionResponse(
      storeName: storeName ?? this.storeName,
      date: date ?? this.date,
      hour: hour ?? this.hour,
      amount: amount ?? this.amount,
      auto: auto ?? this.auto,
      nsu: nsu ?? this.nsu,
      issuerName: issuerName ?? this.issuerName,
      cardHolderName: cardHolderName ?? this.cardHolderName,
      terminalNumber: terminalNumber ?? this.terminalNumber,
      cv: cv ?? this.cv,
      aid: aid ?? this.aid,
      arqc: arqc ?? this.arqc,
      installments: installments ?? this.installments,
      installmentValue: installmentValue ?? this.installmentValue,
      document: document ?? this.document,
      operationType: operationType ?? this.operationType,
      maskedPan: maskedPan ?? this.maskedPan,
      pinTransactionId: pinTransactionId ?? this.pinTransactionId,
      paymentType: paymentType ?? this.paymentType,
      status: status ?? this.status,
      referenceId: referenceId ?? this.referenceId,
      transactionNumber: transactionNumber ?? this.transactionNumber,
      method: method ?? this.method,
    );
  }

  Map<String, dynamic> toMap() {
    return <String, dynamic>{
      'storeName': storeName,
      'date': date,
      'hour': hour,
      'amount': amount,
      'auto': auto,
      'nsu': nsu,
      'issuerName': issuerName,
      'cardHolderName': cardHolderName,
      'terminalNumber': terminalNumber,
      'cv': cv,
      'aid': aid,
      'arqc': arqc,
      'installments': installments,
      'installmentValue': installmentValue,
      'document': document,
      'operationType': operationType,
      'maskedPan': maskedPan,
      'pinTransactionId': pinTransactionId,
      'paymentType': paymentType,
      'status': status,
      'referenceId': referenceId,
      'transactionNumber': transactionNumber,
      'method': method,
    };
  }

  factory RedeTransactionResponse.fromMap(Map<String, dynamic> map) {
    return RedeTransactionResponse(
      storeName: map['storeName'] as String? ?? '',
      date: map['date'] as String? ?? '',
      hour: map['hour'] as String? ?? '',
      amount: _convertDoubleValue(map['amount']),
      auto: map['auto'] as String? ?? '',
      nsu: map['nsu'] as String? ?? '',
      issuerName: map['issuerName'] as String? ?? '',
      cardHolderName: map['cardHolderName'] as String? ?? '',
      terminalNumber: map['terminalNumber'] as String? ?? '',
      cv: map['cv'] as String? ?? '',
      aid: map['aid'] as String? ?? '',
      arqc: map['arqc'] as String? ?? '',
      installments: map['installments'] as int? ?? 0,
      installmentValue: _convertDoubleValue(map['installmentValue']),
      document: map['document'] as String? ?? '',
      operationType: map['operationType'] as String? ?? '',
      maskedPan: map['maskedPan'] as String? ?? '',
      pinTransactionId: map['pinTransactionId'] as String? ?? '',
      paymentType: map['paymentType'] as String? ?? '',
      status: map['status'] as String? ?? '',
      referenceId: map['referenceId'] as String? ?? '',
      transactionNumber: map['transactionNumber'] as String? ?? '',
      method: map['method'] as String? ?? '',
    );
  }

  static double _convertDoubleValue(dynamic value) {
    return double.tryParse(value.toString()) ?? 0.0;
  }

  String toJson() => json.encode(toMap());

  factory RedeTransactionResponse.fromJson(String source) => RedeTransactionResponse.fromMap(json.decode(source) as Map<String, dynamic>);

  @override
  String toString() {
    return 'RedeTransactionResponse(storeName: $storeName, date: $date, hour: $hour, amount: $amount, auto: $auto, nsu: $nsu, issuerName: $issuerName, cardHolderName: $cardHolderName, terminalNumber: $terminalNumber, cv: $cv, aid: $aid, arqc: $arqc, installments: $installments, installmentValue: $installmentValue, document: $document, operationType: $operationType, maskedPan: $maskedPan, pinTransactionId: $pinTransactionId, paymentType: $paymentType, status: $status, referenceId: $referenceId, transactionNumber: $transactionNumber, method: $method)';
  }

  @override
  bool operator ==(covariant RedeTransactionResponse other) {
    if (identical(this, other)) return true;

    return other.storeName == storeName && other.date == date && other.hour == hour && other.amount == amount && other.auto == auto && other.nsu == nsu && other.issuerName == issuerName && other.cardHolderName == cardHolderName && other.terminalNumber == terminalNumber && other.cv == cv && other.aid == aid && other.arqc == arqc && other.installments == installments && other.installmentValue == installmentValue && other.document == document && other.operationType == operationType && other.maskedPan == maskedPan && other.pinTransactionId == pinTransactionId && other.paymentType == paymentType && other.status == status && other.referenceId == referenceId && other.transactionNumber == transactionNumber && other.method == method;
  }

  @override
  int get hashCode {
    return storeName.hashCode ^ date.hashCode ^ hour.hashCode ^ amount.hashCode ^ auto.hashCode ^ nsu.hashCode ^ issuerName.hashCode ^ cardHolderName.hashCode ^ terminalNumber.hashCode ^ cv.hashCode ^ aid.hashCode ^ arqc.hashCode ^ installments.hashCode ^ installmentValue.hashCode ^ document.hashCode ^ operationType.hashCode ^ maskedPan.hashCode ^ pinTransactionId.hashCode ^ paymentType.hashCode ^ status.hashCode ^ referenceId.hashCode ^ transactionNumber.hashCode ^ method.hashCode;
  }
}
