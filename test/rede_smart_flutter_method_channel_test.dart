import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:rede_smart_flutter/rede_smart_flutter_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  MethodChannelRedeSmartFlutter platform = MethodChannelRedeSmartFlutter();
  const MethodChannel channel = MethodChannel('rede_smart_flutter');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        return '42';
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
