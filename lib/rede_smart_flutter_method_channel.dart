import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'rede_smart_flutter_platform_interface.dart';

/// An implementation of [RedeSmartFlutterPlatform] that uses method channels.
class MethodChannelRedeSmartFlutter extends RedeSmartFlutterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('rede_smart_flutter');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
