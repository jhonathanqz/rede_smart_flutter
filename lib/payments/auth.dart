import 'package:flutter/services.dart';

import 'constants/rede_constants.dart';
import 'model/rede_auth_response.dart';

/// Autenticação do SDK da Rede (`RedeAuthentication`).
///
/// O SDK recebe o **valor da transação em centavos** (`amount`) junto com a
/// descrição; o PIN é digitado pelo cliente na tela do próprio SDK.
///
/// Use o parâmetro nomeado `amount`. O antigo `pin` continua aceito por
/// compatibilidade (ele sempre foi enviado ao nativo como `amount`), mas está
/// obsoleto e será removido em uma versão futura.
class Auth {
  final _channel = const MethodChannel(RedeConstants.channel);

  Future<RedeAuthResponse> authWithOnlyPin({
    required String description,
    int? amount,
    @Deprecated('Use amount (valor da transação em centavos). '
        'pin sempre foi enviado ao SDK como valor.')
    int? pin,
  }) async =>
      _auth('AuthOnlyPin', description, _resolveAmount(amount, pin));

  Future<RedeAuthResponse> authWithChangePin({
    required String description,
    int? amount,
    @Deprecated('Use amount (valor da transação em centavos). '
        'pin sempre foi enviado ao SDK como valor.')
    int? pin,
  }) async =>
      _auth('AuthChangePin', description, _resolveAmount(amount, pin));

  Future<RedeAuthResponse> authWithValueAndPin({
    required String description,
    int? amount,
    @Deprecated('Use amount (valor da transação em centavos). '
        'pin sempre foi enviado ao SDK como valor.')
    int? pin,
  }) async =>
      _auth('AuthValueAndPin', description, _resolveAmount(amount, pin));

  /// `amount` tem prioridade; `pin` (obsoleto) é o mesmo valor com outro nome.
  /// Lança [ArgumentError] se nenhum for informado ou se vierem diferentes.
  static int _resolveAmount(int? amount, int? pin) {
    if (amount != null && pin != null && amount != pin) {
      throw ArgumentError(
        'Informe apenas amount (pin está obsoleto e representa o mesmo valor).',
      );
    }
    final value = amount ?? pin;
    if (value == null) {
      throw ArgumentError.notNull('amount');
    }
    return value;
  }

  Future<RedeAuthResponse> _auth(
    String method,
    String description,
    int amount,
  ) async {
    try {
      final response = await _channel.invokeMethod(method, {
        'description': description,
        'amount': amount,
      });
      final model = RedeAuthResponse.fromJson(response);
      return model;
    } catch (e) {
      rethrow;
    }
  }
}
