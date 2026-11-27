import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:rede_payment_tech/payments/auth.dart';
import 'package:rede_payment_tech/payments/constants/rede_constants.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const channel = MethodChannel(RedeConstants.channel);
  final calls = <MethodCall>[];
  final messenger =
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger;

  setUp(() {
    calls.clear();
    messenger.setMockMethodCallHandler(channel, (call) async {
      calls.add(call);
      return '{"status":"Ok","method":"Auth"}';
    });
  });

  tearDown(() => messenger.setMockMethodCallHandler(channel, null));

  final auth = Auth();

  final methods = <String, Future<void> Function(int amount)>{
    'AuthOnlyPin': (amount) =>
        auth.authWithOnlyPin(description: 'd', amount: amount),
    'AuthChangePin': (amount) =>
        auth.authWithChangePin(description: 'd', amount: amount),
    'AuthValueAndPin': (amount) =>
        auth.authWithValueAndPin(description: 'd', amount: amount),
  };

  methods.forEach((method, invoke) {
    test('$method envia description e amount ao nativo', () async {
      await invoke(1500);

      expect(calls.single.method, method);
      expect(calls.single.arguments, {'description': 'd', 'amount': 1500});
    });
  });

  test('resposta do nativo é convertida em RedeAuthResponse', () async {
    final response = await auth.authWithOnlyPin(description: 'd', amount: 100);

    expect(response.status, 'Ok');
    expect(response.method, 'Auth');
  });

  test('cancelamento da autenticação chega como status Canceled', () async {
    messenger.setMockMethodCallHandler(
      channel,
      (_) async => '{"status":"Canceled","method":"Auth"}',
    );

    final response =
        await auth.authWithChangePin(description: 'd', amount: 100);

    expect(response.status, 'Canceled');
  });

  test('INVALID_AUTH do nativo é repassado', () async {
    messenger.setMockMethodCallHandler(
      channel,
      (_) async => throw PlatformException(code: 'INVALID_AUTH'),
    );

    await expectLater(
      auth.authWithValueAndPin(description: 'd', amount: 100),
      throwsA(isA<PlatformException>()
          .having((e) => e.code, 'code', 'INVALID_AUTH')),
    );
  });
}
