import 'package:rede_payment_tech/payments/auth.dart';
import 'package:rede_payment_tech/payments/payment.dart';
import 'package:rede_payment_tech/payments/pos.dart';

export 'package:rede_payment_tech/payments/enum/rede_payment_type.dart';
export 'package:rede_payment_tech/payments/model/rede_auth_response.dart';
export 'package:rede_payment_tech/payments/model/rede_print_response.dart';
export 'package:rede_payment_tech/payments/model/rede_transaction_response.dart';

/// Ponto de entrada do plugin: integra o Flutter ao SDK Rede Smart
/// (`smartrede_sdk`) nas maquininhas Android da Rede.
///
/// Não há configuração prévia: crie a instância e chame as operações em
/// [payment], [pos] e [auth].
class RedeTech {
  Payment? _payment;
  Pos? _pos;
  Auth? _auth;

  /// Pagamentos (crédito, débito, voucher, PIX, parcelado), estorno e
  /// reimpressão.
  Payment get payment => _payment ??= Payment();

  /// Dados do terminal (IMEI, número de série, SIM card).
  Pos get pos => _pos ??= Pos();

  /// Autenticação com PIN do SDK da Rede.
  Auth get auth => _auth ??= Auth();
}
