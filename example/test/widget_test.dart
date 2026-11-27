import 'package:flutter_test/flutter_test.dart';

import 'package:rede_payment_tech_example/main.dart';

void main() {
  testWidgets('Mostra as operações sem pedir configuração', (tester) async {
    await tester.pumpWidget(const MyApp());

    expect(find.text('Rede Payment Tech'), findsOneWidget);
    expect(find.text('Nenhuma operação executada'), findsOneWidget);
    expect(find.text('Crédito'), findsOneWidget);
  });
}
