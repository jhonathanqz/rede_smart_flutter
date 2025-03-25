import 'package:flutter_test/flutter_test.dart';
import 'package:rede_smart_flutter/rede_smart_flutter.dart';
import 'package:rede_smart_flutter/rede_smart_flutter_platform_interface.dart';
import 'package:rede_smart_flutter/rede_smart_flutter_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockRedeSmartFlutterPlatform
    with MockPlatformInterfaceMixin
    implements RedeSmartFlutterPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final RedeSmartFlutterPlatform initialPlatform = RedeSmartFlutterPlatform.instance;

  test('$MethodChannelRedeSmartFlutter is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelRedeSmartFlutter>());
  });

  test('getPlatformVersion', () async {
    RedeSmartFlutter redeSmartFlutterPlugin = RedeSmartFlutter();
    MockRedeSmartFlutterPlatform fakePlatform = MockRedeSmartFlutterPlatform();
    RedeSmartFlutterPlatform.instance = fakePlatform;

    expect(await redeSmartFlutterPlugin.getPlatformVersion(), '42');
  });
}
