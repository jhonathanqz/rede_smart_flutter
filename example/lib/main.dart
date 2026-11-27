import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:rede_payment_tech/rede_payment_tech.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _redeTech = RedeTech();
  String _lastResult = 'Nenhuma operação executada';

  /// Executa a operação e mostra o resultado (ou o erro) na tela.
  Future<void> _run(String label, Future<Object?> Function() operation) async {
    setState(() => _lastResult = '$label: aguardando...');
    try {
      final result = await operation();
      setState(() => _lastResult = '$label: $result');
    } on PlatformException catch (e) {
      setState(() => _lastResult = '$label: erro ${e.code} - ${e.message}');
    } catch (e) {
      setState(() => _lastResult = '$label: erro $e');
    }
  }

  Widget _tile(String label, Future<Object?> Function() operation) {
    return ListTile(
      title: Text(label),
      onTap: () => _run(label, operation),
    );
  }

  @override
  Widget build(BuildContext context) {
    final payment = _redeTech.payment;
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Rede Payment Tech'),
        ),
        body: Column(
          children: [
            Padding(
              padding: const EdgeInsets.all(16),
              child: Text(_lastResult, key: const Key('lastResult')),
            ),
            const Divider(),
            Expanded(
              child: ListView(
                children: [
                  _tile(
                    'Crédito',
                    () => payment.paymentTransaction(
                      paymentType: RedePaymentType.credit,
                      amount: 1540,
                    ),
                  ),
                  _tile(
                    'Débito',
                    () => payment.paymentTransaction(
                      paymentType: RedePaymentType.debit,
                      amount: 1540,
                    ),
                  ),
                  _tile(
                    'Voucher',
                    () => payment.paymentTransaction(
                      paymentType: RedePaymentType.voucher,
                      amount: 1540,
                    ),
                  ),
                  _tile(
                    'PIX',
                    () => payment.paymentTransaction(
                      paymentType: RedePaymentType.pix,
                      amount: 1540,
                    ),
                  ),
                  _tile(
                    'Crédito parcelado consumidor',
                    () => payment.paymentTransaction(
                      paymentType: RedePaymentType.creditParcCustomer,
                      amount: 1540,
                      installment: 5,
                    ),
                  ),
                  _tile(
                    'Crédito parcelado estabelecimento',
                    () => payment.paymentTransaction(
                      paymentType: RedePaymentType.creditParcIssue,
                      amount: 1540,
                      installment: 6,
                    ),
                  ),
                  _tile('Reversão de pagamento', payment.reversalTransaction),
                  _tile('Reimpressão', payment.reprintTransaction),
                  _tile(
                    'Autenticação (só PIN)',
                    () => _redeTech.auth.authWithOnlyPin(
                      description: 'Autenticação',
                      amount: 1000,
                    ),
                  ),
                  _tile('IMEI', _redeTech.pos.getIMEI),
                  _tile('Número de série', _redeTech.pos.getSerialNumber),
                  _tile('SIM card', _redeTech.pos.getSimCardSerialNumber),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
