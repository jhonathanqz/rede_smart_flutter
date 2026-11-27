import 'package:flutter/services.dart';

import 'constants/rede_constants.dart';
import 'model/rede_auth_response.dart';

/// Autenticação do SDK da Rede (`RedeAuthentication`).
///
/// O SDK recebe o **valor da transação em centavos** ([amount]) junto com a
/// descrição; o PIN é digitado pelo cliente na tela do próprio SDK.
class Auth {
  final _channel = const MethodChannel(RedeConstants.channel);

  Future<RedeAuthResponse> authWithOnlyPin({
    required String description,
    required int amount,
  }) =>
      _auth('AuthOnlyPin', description, amount);

  Future<RedeAuthResponse> authWithChangePin({
    required String description,
    required int amount,
  }) =>
      _auth('AuthChangePin', description, amount);

  Future<RedeAuthResponse> authWithValueAndPin({
    required String description,
    required int amount,
  }) =>
      _auth('AuthValueAndPin', description, amount);

  Future<RedeAuthResponse> _auth(
    String method,
    String description,
    int amount,
  ) async {
    final response = await _channel.invokeMethod(method, {
      'description': description,
      'amount': amount,
    });
    return RedeAuthResponse.fromJson(response);
  }
}
