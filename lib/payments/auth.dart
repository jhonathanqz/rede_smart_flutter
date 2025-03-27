import 'package:flutter/services.dart';

import 'constants/rede_constants.dart';
import 'model/rede_auth_response.dart';

class Auth {
  final _channel = const MethodChannel(RedeConstants.channel);

  Future<RedeAuthResponse> authWithOnlyPin({
    required String description,
    required int pin,
  }) async {
    try {
      final response = await _channel.invokeMethod('AuthOnlyPin');
      final model = RedeAuthResponse.fromJson(response);
      return model;
    } catch (e) {
      rethrow;
    }
  }

  Future<RedeAuthResponse> authWithChangePin({
    required String description,
    required int pin,
  }) async {
    try {
      final response = await _channel.invokeMethod('AuthChangePin');
      final model = RedeAuthResponse.fromJson(response);
      return model;
    } catch (e) {
      rethrow;
    }
  }

  Future<RedeAuthResponse> authWithValueAndPin({
    required String description,
    required int pin,
  }) async {
    try {
      final response = await _channel.invokeMethod('AuthValueAndPin');
      final model = RedeAuthResponse.fromJson(response);
      return model;
    } catch (e) {
      rethrow;
    }
  }
}
