
import 'rede_smart_flutter_platform_interface.dart';

class RedeSmartFlutter {
  Future<String?> getPlatformVersion() {
    return RedeSmartFlutterPlatform.instance.getPlatformVersion();
  }
}
