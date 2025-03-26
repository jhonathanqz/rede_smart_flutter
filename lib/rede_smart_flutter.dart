import 'package:rede_smart_flutter/payments/auth.dart';
import 'package:rede_smart_flutter/payments/payment.dart';
import 'package:rede_smart_flutter/payments/pos.dart';

class RedeSmartFlutter {
  Payment? _payment;
  Pos? _pos;
  Auth? _auth;

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
