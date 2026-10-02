import 'package:flutter/services.dart';
import 'package:rede_payment_tech/payments/constants/rede_constants.dart';
import 'package:rede_payment_tech/payments/enum/rede_payment_type.dart';

import 'model/rede_print_response.dart';
import 'model/rede_transaction_response.dart';

class Payment {
  final _channel = const MethodChannel(RedeConstants.channel);

  Future<RedeTransactionResponse> paymentTransaction({
    required RedePaymentType paymentType,
    required int amount,
    int? installment,
  }) async {
    try {
      Map<String, dynamic> params = {
        'amount': amount,
      };

      if (installment != null) {
        params['installment'] = installment;
      }

      final response = await _channel.invokeMethod(paymentType.method, params);

      final model = RedeTransactionResponse.fromJson(response);
      return model;
    } catch (e) {
      rethrow;
    }
  }

  Future<RedeTransactionResponse> reversalTransaction() async {
    try {
      final response = await _channel.invokeMethod('RedeReversal');

      final model = RedeTransactionResponse.fromJson(response);
      return model;
    } catch (e) {
      rethrow;
    }
  }

  Future<RedePrintResponse> reprintTransaction() async {
    try {
      final response = await _channel.invokeMethod('RedeReprint');

      final model = RedePrintResponse.fromJson(response);
      return model;
    } catch (e) {
      rethrow;
    }
  }
}
