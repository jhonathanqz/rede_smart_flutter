import 'package:flutter/services.dart';

import 'constants/rede_constants.dart';

class Pos {
  final _channel = const MethodChannel(RedeConstants.channel);

  Future<String> getIMEI() async {
    try {
      final response = await _channel.invokeMethod('POSIMEI');
      return response;
    } catch (e) {
      rethrow;
    }
  }

  Future<String> getSerialNumber() async {
    try {
      final response = await _channel.invokeMethod('POSSerialNumber');
      return response;
    } catch (e) {
      rethrow;
    }
  }

  Future<String> getSimCardSerialNumber() async {
    try {
      final response = await _channel.invokeMethod('POSSimSerialNumber');
      return response;
    } catch (e) {
      rethrow;
    }
  }
}
