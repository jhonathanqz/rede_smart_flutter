import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:rede_payment_tech/payments/constants/rede_constants.dart';
import 'package:rede_payment_tech/rede_payment_tech.dart';

const _transactionJson = '{"storeName":"Loja","date":"Nov 27, 2026",'
    '"hour":"Nov 27, 2026","amount":15.4,"auto":"123456","nsu":"987",'
    '"issuerName":"VISA","cardHolderName":"FULANO","terminalNumber":"T1",'
    '"cv":"cv","aid":"A000","arqc":"ARQC","installments":5,'
    '"installmentValue":3.08,"document":"00.000.000/0001-00",'
    '"operationType":"VENDA","maskedPan":"************1234",'
    '"pinTransactionId":"","paymentType":"CREDITO_PARCELADO",'
    '"status":"AUTHORIZED","referenceId":"ref","transactionNumber":"42",'
    '"method":"Payment"}';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const channel = MethodChannel(RedeConstants.channel);
  final calls = <MethodCall>[];
  final messenger =
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger;

  /// Nativo fake: responde cada método como o plugin Android responde.
  void mockNative({Object? Function(MethodCall call)? handler}) {
    messenger.setMockMethodCallHandler(channel, (call) async {
      calls.add(call);
      if (handler != null) return handler(call);
      if (call.method.startsWith('Payment') || call.method == 'RedeReversal') {
        return _transactionJson;
      }
      if (call.method == 'RedeReprint') {
        return '{"status":"Ok","method":"Print"}';
      }
      if (call.method.startsWith('Auth')) {
        return '{"status":"Ok","method":"Auth"}';
      }
      return 'valor-${call.method}';
    });
  }

  setUp(calls.clear);
  tearDown(() => messenger.setMockMethodCallHandler(channel, null));

  group('Sem configuração prévia', () {
    test('a primeira chamada já vai direto ao nativo', () async {
      mockNative();
      await RedeTech().payment.paymentTransaction(
            paymentType: RedePaymentType.credit,
            amount: 100,
          );

      expect(calls.single.method, 'PaymentCredit');
    });

    test('getters devolvem sempre a mesma instância', () {
      final redeTech = RedeTech();
      expect(identical(redeTech.payment, redeTech.payment), isTrue);
      expect(identical(redeTech.pos, redeTech.pos), isTrue);
      expect(identical(redeTech.auth, redeTech.auth), isTrue);
    });
  });

  group('Payment.paymentTransaction', () {
    const expectedMethods = {
      RedePaymentType.credit: 'PaymentCredit',
      RedePaymentType.debit: 'PaymentDebit',
      RedePaymentType.voucher: 'PaymentVoucher',
      RedePaymentType.pix: 'PaymentPix',
      RedePaymentType.creditParcIssue: 'PaymentCreditParcIssuer',
      RedePaymentType.creditParcCustomer: 'PaymentCreditParcCustomer',
    };

    test('todos os tipos têm o método nativo correto', () {
      for (final type in RedePaymentType.values) {
        expect(type.method, expectedMethods[type], reason: type.name);
      }
    });

    for (final entry in expectedMethods.entries) {
      test('${entry.key.name} envia só amount quando não há parcelas',
          () async {
        mockNative();
        await RedeTech()
            .payment
            .paymentTransaction(paymentType: entry.key, amount: 1540);

        expect(calls.single.method, entry.value);
        expect(calls.single.arguments, {'amount': 1540});
      });
    }

    test('parcelado envia amount e installment', () async {
      mockNative();
      await RedeTech().payment.paymentTransaction(
            paymentType: RedePaymentType.creditParcCustomer,
            amount: 1540,
            installment: 5,
          );

      expect(calls.single.method, 'PaymentCreditParcCustomer');
      expect(calls.single.arguments, {'amount': 1540, 'installment': 5});
    });

    test('resposta é convertida em RedeTransactionResponse', () async {
      mockNative();
      final response = await RedeTech().payment.paymentTransaction(
            paymentType: RedePaymentType.creditParcIssue,
            amount: 1540,
            installment: 5,
          );

      expect(response.amount, 15.4);
      expect(response.installments, 5);
      expect(response.installmentValue, 3.08);
      expect(response.nsu, '987');
      expect(response.status, 'AUTHORIZED');
      expect(response.method, 'Payment');
    });

    test('valores grandes (acima de int32) chegam intactos', () async {
      mockNative();
      await RedeTech().payment.paymentTransaction(
            paymentType: RedePaymentType.debit,
            amount: 3000000000,
          );

      expect(calls.single.arguments, {'amount': 3000000000});
    });

    test('PlatformException do nativo é repassada', () async {
      mockNative(
        handler: (_) => throw PlatformException(
          code: 'REDE_APP_NOT_FOUND',
          message: 'Aplicativo de pagamentos da Rede não encontrado.',
        ),
      );

      await expectLater(
        RedeTech().payment.paymentTransaction(
              paymentType: RedePaymentType.pix,
              amount: 100,
            ),
        throwsA(isA<PlatformException>()
            .having((e) => e.code, 'code', 'REDE_APP_NOT_FOUND')),
      );
    });
  });

  group('Cancelamento sem dados', () {
    final isCanceled = throwsA(
      isA<PlatformException>().having((e) => e.code, 'code', 'CANCELED'),
    );

    test('pagamento cancelado chega como PlatformException CANCELED', () async {
      mockNative(
        handler: (_) => throw PlatformException(
          code: 'CANCELED',
          message: 'Operação de pagamento cancelada',
        ),
      );

      await expectLater(
        RedeTech().payment.paymentTransaction(
              paymentType: RedePaymentType.credit,
              amount: 100,
            ),
        isCanceled,
      );
    });

    test('estorno cancelado chega como PlatformException CANCELED', () async {
      mockNative(
        handler: (_) => throw PlatformException(
          code: 'CANCELED',
          message: 'Operação de reversão cancelada',
        ),
      );

      await expectLater(RedeTech().payment.reversalTransaction(), isCanceled);
    });

    test('pagamento cancelado com dados continua retornando a resposta',
        () async {
      mockNative(
        handler: (_) => '{"method":"PaymentCancelled","status":"CANCELED",'
            '"transactionNumber":"7"}',
      );

      final response = await RedeTech().payment.paymentTransaction(
            paymentType: RedePaymentType.debit,
            amount: 100,
          );

      expect(response.method, 'PaymentCancelled');
      expect(response.transactionNumber, '7');
    });
  });

  group('Estorno e reimpressão', () {
    test('reversalTransaction chama RedeReversal sem argumentos', () async {
      mockNative();
      final response = await RedeTech().payment.reversalTransaction();

      expect(calls.single.method, 'RedeReversal');
      expect(calls.single.arguments, isNull);
      expect(response.transactionNumber, '42');
    });

    test('reprintTransaction chama RedeReprint e devolve RedePrintResponse',
        () async {
      mockNative();
      final response = await RedeTech().payment.reprintTransaction();

      expect(calls.single.method, 'RedeReprint');
      expect(response.status, 'Ok');
      expect(response.method, 'Print');
    });
  });

  group('Pos', () {
    test('getIMEI', () async {
      mockNative();
      expect(await RedeTech().pos.getIMEI(), 'valor-POSIMEI');
      expect(calls.single.method, 'POSIMEI');
    });

    test('getSerialNumber', () async {
      mockNative();
      expect(await RedeTech().pos.getSerialNumber(), 'valor-POSSerialNumber');
      expect(calls.single.method, 'POSSerialNumber');
    });

    test('getSimCardSerialNumber', () async {
      mockNative();
      expect(
        await RedeTech().pos.getSimCardSerialNumber(),
        'valor-POSSimSerialNumber',
      );
      expect(calls.single.method, 'POSSimSerialNumber');
    });

    test('REDE_SDK_ERROR é repassado', () async {
      mockNative(
        handler: (_) => throw PlatformException(code: 'REDE_SDK_ERROR'),
      );

      await expectLater(
        RedeTech().pos.getIMEI(),
        throwsA(isA<PlatformException>()
            .having((e) => e.code, 'code', 'REDE_SDK_ERROR')),
      );
    });
  });

  group('Modelos', () {
    test('RedeTransactionResponse tolera campos ausentes', () {
      final response = RedeTransactionResponse.fromJson('{}');

      expect(response.amount, 0.0);
      expect(response.installments, 0);
      expect(response.nsu, '');
    });

    test('RedeTransactionResponse ida e volta em JSON', () {
      final response = RedeTransactionResponse.fromJson(_transactionJson);

      expect(RedeTransactionResponse.fromJson(response.toJson()), response);
    });

    test('RedeAuthResponse e RedePrintResponse', () {
      expect(
        RedeAuthResponse.fromJson('{"status":"Canceled","method":"Auth"}')
            .status,
        'Canceled',
      );
      expect(
        RedePrintResponse.fromJson('{"status":"Ok","method":"Print"}').method,
        'Print',
      );
    });
  });
}
