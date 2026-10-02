import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:rede_payment_tech/payments/constants/rede_constants.dart';
import 'package:rede_payment_tech/payments/enum/rede_payment_type.dart';
import 'package:rede_payment_tech/rede_payment_tech.dart';

/// Chave fictícia (nenhuma chave real nos testes).
const _testLicenceKey = 'JYL-TEST-0000-0000';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const channel = MethodChannel(RedeConstants.channel);
  final calls = <MethodCall>[];
  final messenger =
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger;

  PlatformException licenceError(String message) => PlatformException(
        code: 'LICENCE_ERROR',
        message: 'Sua licença não foi ativada. $message',
      );

  /// Nativo fake: RedeConfigureLicence responde conforme [licenceValid]; as
  /// demais operações respondem LICENCE_ERROR se [operationLicenceValid] for
  /// false, ou LICENCE_NOT_CONFIGURED se a licença não foi configurada.
  void mockNative({
    bool licenceValid = true,
    bool operationLicenceValid = true,
  }) {
    var configured = false;
    messenger.setMockMethodCallHandler(channel, (call) async {
      calls.add(call);
      if (call.method == 'RedeConfigureLicence') {
        if (!licenceValid) {
          throw licenceError('Licença vencida.');
        }
        configured = true;
        return true;
      }
      if (!configured) {
        throw PlatformException(
          code: 'LICENCE_NOT_CONFIGURED',
          message: 'Configure a licença chamando configureLicence.',
        );
      }
      if (!operationLicenceValid) {
        throw licenceError('Licença cancelada.');
      }
      return '{}';
    });
  }

  final isLicenceError = throwsA(
    isA<PlatformException>().having((e) => e.code, 'code', 'LICENCE_ERROR'),
  );

  setUp(calls.clear);

  tearDown(() {
    messenger.setMockMethodCallHandler(channel, null);
  });

  test('licenceCheck aponta para o servidor oficial (core)', () {
    final url = utf8.decode(base64.decode(licenceCheck));
    expect(
      url,
      'https://portal-api.jylabtech.com.br/pos-payments/licence/check',
    );
    final uri = Uri.parse(url);
    expect(uri.scheme, 'https');
    expect(uri.host, 'portal-api.jylabtech.com.br');
    expect(uri.path, startsWith('/pos-payments/'));
  });

  test('configureLicence envia licenceKey e licenceInternalKey padrão do core',
      () async {
    mockNative();
    final redeTech = RedeTech();
    await redeTech.configureLicence(licenceKey: _testLicenceKey);

    final call = calls.singleWhere((c) => c.method == 'RedeConfigureLicence');
    expect(call.arguments['licenceKey'], _testLicenceKey);
    expect(call.arguments['licenceInternalKey'], licenceCheck);
    expect(redeTech.isLicenceConfigured, isTrue);
  });

  test('configureLicence repassa licenceInternalKey informado', () async {
    mockNative();
    const custom =
        'https://portal-api.jylabtech.com.br/pos-payments/licence/check/invoice';
    await RedeTech().configureLicence(
      licenceKey: _testLicenceKey,
      licenceInternalKey: custom,
    );

    final call = calls.singleWhere((c) => c.method == 'RedeConfigureLicence');
    expect(call.arguments['licenceInternalKey'], custom);
  });

  test('configureLicence lança LICENCE_ERROR quando a licença é inválida',
      () async {
    mockNative(licenceValid: false);
    final redeTech = RedeTech();
    await expectLater(
      redeTech.configureLicence(licenceKey: _testLicenceKey),
      isLicenceError,
    );
    expect(redeTech.isLicenceConfigured, isFalse);
  });

  test('operações antes de configurar a licença falham', () async {
    mockNative();
    await expectLater(
      RedeTech().payment.paymentTransaction(
            paymentType: RedePaymentType.credit,
            amount: 100,
          ),
      throwsA(
        isA<PlatformException>()
            .having((e) => e.code, 'code', 'LICENCE_NOT_CONFIGURED'),
      ),
    );
  });

  test('todas as operações propagam LICENCE_ERROR do nativo', () async {
    mockNative(operationLicenceValid: false);
    final redeTech = RedeTech();
    await redeTech.configureLicence(licenceKey: _testLicenceKey);

    for (final type in RedePaymentType.values) {
      await expectLater(
        redeTech.payment.paymentTransaction(
          paymentType: type,
          amount: 100,
          installment: 2,
        ),
        isLicenceError,
        reason: type.method,
      );
    }
    await expectLater(redeTech.payment.reversalTransaction(), isLicenceError);
    await expectLater(redeTech.payment.reprintTransaction(), isLicenceError);
    await expectLater(
      redeTech.auth.authWithOnlyPin(description: 'd', amount: 1),
      isLicenceError,
    );
    await expectLater(
      redeTech.auth.authWithChangePin(description: 'd', amount: 1),
      isLicenceError,
    );
    await expectLater(
      redeTech.auth.authWithValueAndPin(description: 'd', amount: 1),
      isLicenceError,
    );
    await expectLater(redeTech.pos.getIMEI(), isLicenceError);
    await expectLater(redeTech.pos.getSerialNumber(), isLicenceError);
    await expectLater(redeTech.pos.getSimCardSerialNumber(), isLicenceError);

    // Nenhuma operação de negócio recebeu sucesso: só o configure foi aceito.
    expect(calls.first.method, 'RedeConfigureLicence');
    expect(calls.length, 1 + RedePaymentType.values.length + 8);
  });
}
