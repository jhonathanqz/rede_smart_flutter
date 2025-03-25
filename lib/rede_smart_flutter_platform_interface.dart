import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'rede_smart_flutter_method_channel.dart';

abstract class RedeSmartFlutterPlatform extends PlatformInterface {
  /// Constructs a RedeSmartFlutterPlatform.
  RedeSmartFlutterPlatform() : super(token: _token);

  static final Object _token = Object();

  static RedeSmartFlutterPlatform _instance = MethodChannelRedeSmartFlutter();

  /// The default instance of [RedeSmartFlutterPlatform] to use.
  ///
  /// Defaults to [MethodChannelRedeSmartFlutter].
  static RedeSmartFlutterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [RedeSmartFlutterPlatform] when
  /// they register themselves.
  static set instance(RedeSmartFlutterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
