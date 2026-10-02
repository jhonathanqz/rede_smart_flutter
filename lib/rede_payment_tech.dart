import 'package:flutter/services.dart';
import 'package:rede_payment_tech/payments/auth.dart';
import 'package:rede_payment_tech/payments/payment.dart';
import 'package:rede_payment_tech/payments/pos.dart';
import 'package:rede_payment_tech/payments/constants/rede_constants.dart';

export 'package:rede_payment_tech/payments/constants/rede_constants.dart'
    show licenceCheck;

class RedeTech {
  final MethodChannel _channel = const MethodChannel(RedeConstants.channel);
  Payment? _payment;
  Pos? _pos;
  Auth? _auth;
  bool _licenceConfigured = false;

  /// Configura e valida a licença no servidor oficial da JY Labtech.
  ///
  /// [licenceInternalKey] é opcional (padrão: [licenceCheck]); valores que não
  /// apontem para o servidor oficial são ignorados pelo nativo.
  ///
  /// Lança [PlatformException] com código `LICENCE_ERROR` se a licença for
  /// inválida, vencida, suspensa ou cancelada (ou se não for possível validá-la).
  Future<void> configureLicence({
    required String licenceKey,
    String licenceInternalKey = licenceCheck,
  }) async {
    await _channel.invokeMethod('RedeConfigureLicence', {
      'licenceKey': licenceKey,
      'licenceInternalKey': licenceInternalKey,
    });
    _licenceConfigured = true;
  }

  bool get isLicenceConfigured => _licenceConfigured;

  Payment _initPayment() {
    _payment ??= Payment();
    return _payment!;
  }

  Pos _initPos() {
    _pos ??= Pos();
    return _pos!;
  }

  Auth _initAuth() {
    _auth ??= Auth();
    return _auth!;
  }

  Payment get payment => _payment ?? _initPayment();
  Pos get pos => _pos ?? _initPos();
  Auth get auth => _auth ?? _initAuth();
}
