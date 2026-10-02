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

  final methods =
      <String, Future<void> Function({int? amount, int? pin})>{
    'AuthOnlyPin': ({amount, pin}) =>
        // ignore: deprecated_member_use_from_same_package
        auth.authWithOnlyPin(description: 'd', amount: amount, pin: pin),
    'AuthChangePin': ({amount, pin}) =>
        // ignore: deprecated_member_use_from_same_package
        auth.authWithChangePin(description: 'd', amount: amount, pin: pin),
    'AuthValueAndPin': ({amount, pin}) =>
        // ignore: deprecated_member_use_from_same_package
        auth.authWithValueAndPin(description: 'd', amount: amount, pin: pin),
  };

  methods.forEach((method, invoke) {
    group(method, () {
      test('envia amount (valor da transação) ao nativo', () async {
        await invoke(amount: 1500);

        expect(calls.single.method, method);
        expect(calls.single.arguments, {'description': 'd', 'amount': 1500});
      });

      test('pin obsoleto continua funcionando como amount', () async {
        await invoke(pin: 700);

        expect(calls.single.arguments, {'description': 'd', 'amount': 700});
      });

      test('amount e pin iguais são aceitos', () async {
        await invoke(amount: 300, pin: 300);

        expect(calls.single.arguments['amount'], 300);
      });

      test('amount e pin diferentes lançam ArgumentError', () async {
        await expectLater(invoke(amount: 1, pin: 2), throwsArgumentError);
        expect(calls, isEmpty);
      });

      test('sem amount nem pin lança ArgumentError sem chamar o nativo',
          () async {
        await expectLater(invoke(), throwsArgumentError);
        expect(calls, isEmpty);
      });
    });
  });

  test('resposta do nativo é convertida em RedeAuthResponse', () async {
    final response =
        await auth.authWithOnlyPin(description: 'd', amount: 100);

    expect(response.status, 'Ok');
    expect(response.method, 'Auth');
  });
}
