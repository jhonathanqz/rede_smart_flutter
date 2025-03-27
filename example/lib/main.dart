import 'package:flutter/material.dart';
import 'package:rede_smart_flutter/payments/enum/rede_payment_type.dart';
import 'package:rede_smart_flutter/rede_smart_flutter.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _redeSmartFlutterPlugin = RedeSmartFlutter();

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: [
            ListTile(
              title: const Text('Crédito'),
              onTap: () async {
                await RedeSmartFlutter().payment.paymentTransaction(
                      paymentType: RedePaymentType.credit,
                      amount: 1540,
                    );
              },
            ),
            ListTile(
              title: const Text('Débito'),
              onTap: () async {
                await _redeSmartFlutterPlugin.payment.paymentTransaction(
                  paymentType: RedePaymentType.debit,
                  amount: 1540,
                );
              },
            ),
            ListTile(
              title: const Text('Voucher'),
              onTap: () async {
                await _redeSmartFlutterPlugin.payment.paymentTransaction(
                  paymentType: RedePaymentType.voucher,
                  amount: 1540,
                );
              },
            ),
            ListTile(
              title: const Text('PIX'),
              onTap: () async {
                await _redeSmartFlutterPlugin.payment.paymentTransaction(
                  paymentType: RedePaymentType.pix,
                  amount: 1540,
                );
              },
            ),
            ListTile(
              title: const Text('Crédito parcelado consumidor'),
              onTap: () async {
                await _redeSmartFlutterPlugin.payment.paymentTransaction(
                  paymentType: RedePaymentType.creditParcCustomer,
                  amount: 1540,
                  installment: 5,
                );
              },
            ),
            ListTile(
              title: const Text('Crédito parcelado estabelecimento'),
              onTap: () async {
                await _redeSmartFlutterPlugin.payment.paymentTransaction(
                  paymentType: RedePaymentType.creditParcIssue,
                  amount: 1540,
                  installment: 6,
                );
              },
            ),
            ListTile(
              title: const Text('Reversão de pagamento'),
              onTap: () async {
                await _redeSmartFlutterPlugin.payment.reversalTransaction();
              },
            ),
            ListTile(
              title: const Text('Reimpressao'),
              onTap: () async {
                await _redeSmartFlutterPlugin.payment.reprintTransaction();
              },
            ),
          ],
        ),
      ),
    );
  }
}
