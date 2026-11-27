# Rede Payment Tech

> Flutter plugin for Rede Android smart POS terminals (Rede Smart SDK): card, PIX and voucher payments, installments, reversal, reprint, PIN authentication and terminal data.

[![pub package](https://img.shields.io/pub/v/rede_payment_tech.svg)](https://pub.dev/packages/rede_payment_tech)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Plugin Flutter para integrar apps às maquininhas smart Android da **Rede**, usando o SDK oficial
Rede Smart (`smartrede_sdk`). Ele abre as telas do aplicativo de pagamentos da Rede no terminal e
devolve o resultado da transação ao Dart.

> **Plugin não oficial.** Não é mantido nem endossado pela Rede.

Open source sob a licença [MIT](#licença). Não há licença paga, chave de ativação nem cobrança.

## Sumário

- [Funcionalidades](#funcionalidades)
- [Compatibilidade e requisitos](#compatibilidade-e-requisitos)
- [Instalação](#instalação)
- [Configuração Android](#configuração-android)
- [Uso rápido](#uso-rápido)
- [Referência da API](#referência-da-api)
- [Exemplos por operação](#exemplos-por-operação)
- [Tratamento de erros](#tratamento-de-erros)
- [App de exemplo](#app-de-exemplo)
- [Testes](#testes)
- [Migração da 1.x para a 2.0](#migração-da-1x-para-a-20)
- [Contribuindo](#contribuindo)
- [Licença](#licença)
- [Créditos](#créditos)

## Funcionalidades

| Operação | Método |
| --- | --- |
| Crédito à vista | `payment.paymentTransaction(paymentType: RedePaymentType.credit, ...)` |
| Crédito parcelado emissor (`CREDITO_PARCELADO_EMISSOR` no SDK) | `payment.paymentTransaction(paymentType: RedePaymentType.creditParcIssue, installment: n, ...)` |
| Crédito parcelado (`CREDITO_PARCELADO` no SDK) | `payment.paymentTransaction(paymentType: RedePaymentType.creditParcCustomer, installment: n, ...)` |
| Débito | `payment.paymentTransaction(paymentType: RedePaymentType.debit, ...)` |
| Voucher (alimentação/refeição) | `payment.paymentTransaction(paymentType: RedePaymentType.voucher, ...)` |
| PIX | `payment.paymentTransaction(paymentType: RedePaymentType.pix, ...)` |
| Estorno (reversão) | `payment.reversalTransaction()` |
| Reimpressão do comprovante | `payment.reprintTransaction()` |
| Autenticação só com PIN | `auth.authWithOnlyPin(...)` |
| Autenticação com troca de PIN | `auth.authWithChangePin(...)` |
| Autenticação com valor e PIN | `auth.authWithValueAndPin(...)` |
| IMEI do terminal | `pos.getIMEI()` |
| Número de série do terminal | `pos.getSerialNumber()` |
| Número de série do SIM card | `pos.getSimCardSerialNumber()` |

A impressão de texto/imagem livre e a leitura NFC não são expostas por este plugin.

## Compatibilidade e requisitos

- **Plataforma:** somente Android. O plugin iOS é um stub e responde `MissingPluginException`
  (`notImplemented`) para qualquer método.
- **Terminais:** maquininhas smart Android da Rede com o aplicativo de pagamentos da Rede
  instalado (é ele que exibe as telas de pagamento, estorno, reimpressão e autenticação).
  Em celulares comuns ou emuladores as operações falham com `REDE_APP_NOT_FOUND` ou
  `REDE_SDK_ERROR`.
- **Android:** `minSdk` **28** (exigido pelo `smartrede_sdk.aar`), `compileSdk` 36.
- **Dart** `>=3.5.4 <4.0.0` e **Flutter** `>=3.3.0`.
- **Permissões:** o plugin não declara nenhuma permissão no `AndroidManifest.xml`.
- **SDK da Rede:** o `smartrede_sdk.aar` já vem dentro do pacote (`android/libs`); não é preciso
  baixá-lo nem configurar repositório Maven ou credenciais.

## Instalação

```bash
flutter pub add rede_payment_tech
```

ou, no `pubspec.yaml`:

```yaml
dependencies:
  rede_payment_tech: ^2.0.0
```

## Configuração Android

### minSdk

No `android/app/build.gradle` (ou `build.gradle.kts`) do seu app, use `minSdk` 28 ou maior:

```groovy
android {
    defaultConfig {
        minSdk = 28 // exigido pelo smartrede_sdk.aar
        targetSdk = flutter.targetSdkVersion
    }
}
```

### Repositório do SDK (`flatDir`)

O `android/build.gradle` do plugin adiciona `google()`, `mavenCentral()` e um `flatDir` apontando
para o seu próprio `android/libs` em `rootProject.allprojects`, para o app conseguir resolver o
`smartrede_sdk.aar`. Em projetos Flutter padrão (com `allprojects { repositories { ... } }` no
`android/build.gradle`) nada mais é necessário.

Se o seu `settings.gradle` usar `dependencyResolutionManagement` com
`repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)`, troque para `PREFER_SETTINGS`
(ou remova essa linha), senão o Gradle recusa o repositório adicionado pelo plugin.

### Activity

As operações abrem telas do app da Rede com `startActivityForResult` e respondem no
`onActivityResult` da sua `FlutterActivity`. Não é preciso alterar a `MainActivity`; basta chamar
as operações com o app em primeiro plano (antes de a Activity existir, a chamada falha com
`ACTIVITY_NOT_ATTACHED`).

Nenhum intent filter, flavor ou entrada extra no `AndroidManifest.xml` é necessário.

## Uso rápido

```dart
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:rede_payment_tech/rede_payment_tech.dart';

void main() => runApp(const MaterialApp(home: PaymentPage()));

class PaymentPage extends StatefulWidget {
  const PaymentPage({super.key});

  @override
  State<PaymentPage> createState() => _PaymentPageState();
}

class _PaymentPageState extends State<PaymentPage> {
  final _redeTech = RedeTech();
  String _status = 'Toque para pagar R\$ 15,40 no crédito';

  Future<void> _pay() async {
    try {
      final RedeTransactionResponse tx = await _redeTech.payment.paymentTransaction(
        paymentType: RedePaymentType.credit,
        amount: 1540, // centavos
      );
      setState(() => _status = 'Status ${tx.status} | NSU ${tx.nsu} | AUT ${tx.auto}');
    } on PlatformException catch (e) {
      // e.code == 'CANCELED' -> cancelado sem dados da transação.
      setState(() => _status = 'Erro ${e.code}: ${e.message}');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Rede')),
      body: Center(child: Text(_status)),
      floatingActionButton: FloatingActionButton(
        onPressed: _pay,
        child: const Icon(Icons.payment),
      ),
    );
  }
}
```

Não há inicialização: crie `RedeTech()` e chame as operações.

## Referência da API

Todos os tipos públicos são exportados por `package:rede_payment_tech/rede_payment_tech.dart`.
Todas as operações usam o `MethodChannel` `rede_payment_tech` e respondem **exatamente uma vez**
(sucesso ou `PlatformException`).

### `RedeTech`

| Membro | Tipo | Descrição |
| --- | --- | --- |
| `payment` | `Payment` | Pagamentos, estorno e reimpressão. |
| `pos` | `Pos` | Dados do terminal. |
| `auth` | `Auth` | Autenticação com PIN. |

Os getters criam a instância na primeira chamada e devolvem sempre a mesma.

```dart
final redeTech = RedeTech();
final payment = redeTech.payment;
```

### `Payment`

#### `paymentTransaction`

```dart
Future<RedeTransactionResponse> paymentTransaction({
  required RedePaymentType paymentType,
  required int amount,
  int? installment,
})
```

| Parâmetro | Descrição |
| --- | --- |
| `paymentType` | Tipo do pagamento (veja [`RedePaymentType`](#enum-redepaymenttype)). |
| `amount` | Valor em **centavos** (`1540` = R$ 15,40). Repassado ao SDK como `long`. |
| `installment` | Número de parcelas. **Obrigatório** em `creditParcIssue` e `creditParcCustomer` (sem ele, `INVALID_ARGUMENT`); ignorado nos demais tipos. |

Retorna um [`RedeTransactionResponse`](#redetransactionresponse) quando o app da Rede devolve os
dados da transação (aprovada ou cancelada). Se o pagamento for cancelado e o app da Rede não
devolver dados, o `Future` falha com `PlatformException` código `CANCELED`.

```dart
final tx = await RedeTech().payment.paymentTransaction(
  paymentType: RedePaymentType.debit,
  amount: 2590,
);
```

#### `reversalTransaction`

```dart
Future<RedeTransactionResponse> reversalTransaction()
```

Abre a tela de estorno do app da Rede (o operador escolhe a transação lá). Retorna um
`RedeTransactionResponse` com `method == 'Reversal'`.

```dart
final reversal = await RedeTech().payment.reversalTransaction();
```

#### `reprintTransaction`

```dart
Future<RedePrintResponse> reprintTransaction()
```

Abre a reimpressão de comprovante do app da Rede. Retorna `RedePrintResponse` com
`status` `'Ok'` ou `'Canceled'`.

```dart
final print = await RedeTech().payment.reprintTransaction();
```

### `Pos`

| Método | Retorno | Descrição |
| --- | --- | --- |
| `getIMEI()` | `Future<String>` | IMEI do terminal. |
| `getSerialNumber()` | `Future<String>` | Número de série do terminal. |
| `getSimCardSerialNumber()` | `Future<String>` | Número de série do SIM card. |

Respondem na hora (sem tela). Falham com `REDE_SDK_ERROR` se o serviço do terminal não estiver
disponível.

```dart
final serial = await RedeTech().pos.getSerialNumber();
```

### `Auth`

```dart
Future<RedeAuthResponse> authWithOnlyPin({required String description, required int amount});
Future<RedeAuthResponse> authWithChangePin({required String description, required int amount});
Future<RedeAuthResponse> authWithValueAndPin({required String description, required int amount});
```

| Parâmetro | Descrição |
| --- | --- |
| `description` | Texto exibido na tela de autenticação da Rede. |
| `amount` | Valor da operação em **centavos** (obrigatório). |

O PIN é digitado pelo cliente na tela do SDK. Retorna `RedeAuthResponse` com `status` `'Ok'` ou
`'Canceled'`.

```dart
final auth = await RedeTech().auth.authWithValueAndPin(
  description: 'Confirmar operação',
  amount: 1000,
);
```

### Enum `RedePaymentType`

| Valor | Método nativo | Tipo no SDK da Rede (`FlexTipoPagamento`) |
| --- | --- | --- |
| `credit` | `PaymentCredit` | `CREDITO_A_VISTA` |
| `debit` | `PaymentDebit` | `DEBITO` |
| `voucher` | `PaymentVoucher` | `VOUCHER` |
| `pix` | `PaymentPix` | `PIX` |
| `creditParcIssue` | `PaymentCreditParcIssuer` | `CREDITO_PARCELADO_EMISSOR` |
| `creditParcCustomer` | `PaymentCreditParcCustomer` | `CREDITO_PARCELADO` |

`RedePaymentType.method` devolve o nome do método nativo. Para saber quem assume os juros em cada
modalidade parcelada, consulte a documentação do SDK da Rede para o tipo `FlexTipoPagamento`
correspondente.

### Modelos de resposta

Todos têm `fromJson`/`fromMap`, `toJson`/`toMap`, `copyWith`, `==` e `hashCode`.

#### `RedeTransactionResponse`

Campos ausentes viram `''` (texto) ou `0` (números).

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `method` | `String` | `'Payment'`, `'PaymentCancelled'` ou `'Reversal'`. |
| `status` | `String` | Status da transação no SDK da Rede. |
| `paymentType` | `String` | Tipo de pagamento informado pelo SDK. |
| `amount` | `double` | Valor do comprovante. |
| `installments` / `installmentValue` | `int` / `double` | Parcelas e valor da parcela. |
| `nsu` | `String` | NSU da transação. |
| `auto` | `String` | Código de autorização. |
| `transactionNumber` / `referenceId` | `String` | Identificadores da transação no SDK. |
| `issuerName` / `cardHolderName` / `maskedPan` | `String` | Bandeira/emissor, portador e cartão mascarado. |
| `aid` / `arqc` / `cv` | `String` | Dados EMV do comprovante. |
| `storeName` / `document` / `terminalNumber` | `String` | Estabelecimento, CNPJ e número do terminal. |
| `operationType` | `String` | Tipo da operação no comprovante. |
| `pinTransactionId` | `String` | Identificador da transação PIX. |
| `date` / `hour` | `String` | Data/hora do comprovante (serializadas pelo nativo). |

Em pagamentos e estornos **cancelados** com dados, só `method`, `status`, `paymentType`,
`referenceId` e `transactionNumber` vêm preenchidos.

#### `RedePrintResponse` e `RedeAuthResponse`

| Campo | Tipo | Valores |
| --- | --- | --- |
| `status` | `String` | `'Ok'` ou `'Canceled'` |
| `method` | `String` | `'Print'` (reimpressão) ou `'Auth'` (autenticação) |

## Exemplos por operação

```dart
final redeTech = RedeTech();
```

**Crédito à vista**

```dart
await redeTech.payment.paymentTransaction(
  paymentType: RedePaymentType.credit,
  amount: 1540,
);
```

**Crédito parcelado emissor (6x)**

```dart
await redeTech.payment.paymentTransaction(
  paymentType: RedePaymentType.creditParcIssue,
  amount: 60000,
  installment: 6,
);
```

**Crédito parcelado (5x)**

```dart
await redeTech.payment.paymentTransaction(
  paymentType: RedePaymentType.creditParcCustomer,
  amount: 50000,
  installment: 5,
);
```

**Débito**

```dart
await redeTech.payment.paymentTransaction(
  paymentType: RedePaymentType.debit,
  amount: 1540,
);
```

**PIX**

```dart
final tx = await redeTech.payment.paymentTransaction(
  paymentType: RedePaymentType.pix,
  amount: 1540,
);
print(tx.pinTransactionId);
```

**Voucher**

```dart
await redeTech.payment.paymentTransaction(
  paymentType: RedePaymentType.voucher,
  amount: 3200,
);
```

**Estorno**

```dart
final reversal = await redeTech.payment.reversalTransaction();
print('${reversal.status} ${reversal.transactionNumber}');
```

**Reimpressão**

```dart
final reprint = await redeTech.payment.reprintTransaction();
if (reprint.status == 'Canceled') print('Reimpressão cancelada');
```

**Autenticação**

```dart
await redeTech.auth.authWithOnlyPin(description: 'Acesso ao caixa', amount: 1000);
await redeTech.auth.authWithChangePin(description: 'Trocar PIN', amount: 1000);
await redeTech.auth.authWithValueAndPin(description: 'Confirmar', amount: 1000);
```

**Dados do terminal**

```dart
final imei = await redeTech.pos.getIMEI();
final serial = await redeTech.pos.getSerialNumber();
final sim = await redeTech.pos.getSimCardSerialNumber();
```

## Tratamento de erros

Falhas do nativo chegam como `PlatformException` (`e.code`):

| Código | Quando ocorre |
| --- | --- |
| `ACTIVITY_NOT_ATTACHED` | O plugin ainda não está anexado a uma Activity (chame com o app em primeiro plano). |
| `INVALID_ARGUMENT` | `amount`/`installment` ausente, não numérico, fracionado ou (parcelas) fora do intervalo de `int`. |
| `REDE_APP_NOT_FOUND` | O aplicativo de pagamentos/autenticação da Rede não foi encontrado no terminal. |
| `INVALID_PAYMENT` | O SDK da Rede recusou os dados do pagamento (ex.: valor ou parcelas inválidos). |
| `INVALID_AUTH` | O SDK da Rede recusou os dados da autenticação (ex.: descrição ou valor inválidos). |
| `REDE_SDK_ERROR` | O SDK da Rede está indisponível ao ler dados do POS (IMEI, número de série, SIM). |
| `UNEXPECTED_RESULT` | O app da Rede voltou sem os dados da transação ou com um `resultCode` desconhecido. |
| `CANCELED` | Pagamento ou estorno cancelado e o app da Rede não devolveu dados da transação (`message`: "Operação de pagamento cancelada" / "Operação de reversão cancelada"). Cancelamentos **com** dados retornam normalmente um `RedeTransactionResponse` com `method == 'PaymentCancelled'`. |
| `OPERATION_REPLACED` | Uma nova operação com tela da Rede foi iniciada antes de a anterior responder; a anterior recebe este erro. |
| `REDE_ERROR` | Falha inesperada ao chamar o SDK ou ao ler o retorno do app da Rede (a `message` traz o detalhe). |

Outros casos:

- **`MissingPluginException`**: plataforma sem implementação (iOS) ou método desconhecido.

```dart
try {
  await RedeTech().payment.paymentTransaction(
    paymentType: RedePaymentType.creditParcIssue,
    amount: 10000,
    installment: 3,
  );
} on PlatformException catch (e) {
  switch (e.code) {
    case 'REDE_APP_NOT_FOUND':
      // terminal sem o app da Rede
      break;
    case 'CANCELED':
      // cancelado pelo operador/cliente, sem dados da transação
      break;
    case 'INVALID_PAYMENT':
      // valor/parcelas recusados pelo SDK: e.message traz o motivo
      break;
    default:
      debugPrint('${e.code}: ${e.message}');
  }
}
```

## App de exemplo

O diretório [`example/`](example) tem um app com um botão para cada operação e o resultado
(ou o erro) na tela. Rode em uma maquininha da Rede:

```bash
cd example
flutter run
```

Não é preciso nenhuma chave ou `--dart-define`.

## Testes

```bash
# Testes Dart (MethodChannel mockado): todos os métodos, modelos e erros
flutter test

# Testes JVM do código nativo (android/native), com JDK 17
cd example/android
./gradlew -p ../../android/native testDebugUnitTest --no-daemon -Dorg.gradle.jvmargs=-Xmx2g

# Teste JVM da classe ponte do plugin (android/src/test)
./gradlew :rede_payment_tech:testDebugUnitTest --no-daemon -Dorg.gradle.jvmargs=-Xmx2g
```

O `-p ../../android/native` precisa do Flutter SDK com o `flutter.jar` em cache
(`flutter precache --android`) e de `flutter.sdk` em `android/native/local.properties` ou da
variável `FLUTTER_SDK`.

### Binário nativo

O build do plugin usa o binário pré-compilado `android/libs/rede_payment_tech-native.jar`
(e o `.aar`), gerado a partir do código-fonte em `android/native`. Depois de alterar o código
nativo, regenere:

```bash
JAVA_HOME=/caminho/do/jdk17 JAVA_TOOL_OPTIONS=-Xmx2g FLUTTER_SDK=/caminho/do/flutter \
  scripts/build_android_aar.sh
```

## Migração da 1.x para a 2.0

A 2.0 remove toda a licença paga. O que mudou:

- `RedeTech.configureLicence({licenceKey, licenceInternalKey})` foi **removido**: não há mais
  configuração antes das operações.
- `RedeTech.isLicenceConfigured` e a constante `licenceCheck` foram **removidos**.
- Os erros `LICENCE_ERROR` e `LICENCE_NOT_CONFIGURED` não existem mais.
- O plugin não faz nenhuma chamada HTTP e não guarda cache de licença no aparelho.
- O example não precisa mais de `--dart-define=LICENCE_KEY=...`.
- O parâmetro obsoleto `pin` de `authWithOnlyPin`, `authWithChangePin` e `authWithValueAndPin`
  foi **removido**: use `amount` (agora `required int`).
- Pagamento/estorno cancelado **sem dados** agora falha com `PlatformException` código
  `CANCELED`. Antes o nativo devolvia um texto puro e o Dart lançava `FormatException`.

Antes (1.x):

```dart
final redeTech = RedeTech();

try {
  await redeTech.configureLicence(licenceKey: 'SUA_LICENCE_KEY');
} on PlatformException catch (e) {
  if (e.code == 'LICENCE_ERROR') {
    // licença inválida
  }
}

await redeTech.payment.paymentTransaction(
  paymentType: RedePaymentType.credit,
  amount: 1540,
);
```

Depois (2.0):

```dart
final redeTech = RedeTech();

await redeTech.payment.paymentTransaction(
  paymentType: RedePaymentType.credit,
  amount: 1540,
);
```

Autenticação e cancelamento, antes (1.x):

```dart
await redeTech.auth.authWithOnlyPin(description: 'Acesso', pin: 1000);

try {
  await redeTech.payment.reversalTransaction();
} on FormatException {
  // cancelado sem dados
}
```

Depois (2.0):

```dart
await redeTech.auth.authWithOnlyPin(description: 'Acesso', amount: 1000);

try {
  await redeTech.payment.reversalTransaction();
} on PlatformException catch (e) {
  if (e.code == 'CANCELED') {
    // cancelado sem dados
  }
}
```

Remova também qualquer tratamento de `LICENCE_ERROR`/`LICENCE_NOT_CONFIGURED`. Os imports
diretos (`package:rede_payment_tech/payments/enum/rede_payment_type.dart` etc.) continuam
funcionando, mas agora basta `package:rede_payment_tech/rede_payment_tech.dart`.

## Contribuindo

Issues e pull requests são bem-vindos em
[github.com/jhonathanqz/rede_smart_flutter](https://github.com/jhonathanqz/rede_smart_flutter/issues).

1. Faça um fork e crie um branch.
2. Rode `flutter analyze` e `flutter test` (e os testes nativos, se mexer em `android/`).
3. Se alterar `android/native`, regenere os binários com `scripts/build_android_aar.sh`.
4. Descreva a mudança e atualize o `CHANGELOG.md`.

## Licença

O código deste plugin é distribuído sob a licença [MIT](LICENSE)
(Copyright (c) 2023 Gabuldev, Copyright (c) 2026 JY Labtech).

**O SDK da Rede não é coberto pela MIT.** O arquivo `android/libs/smartrede_sdk.aar` é propriedade
da Rede e é redistribuído aqui apenas para viabilizar o build; seu uso segue os termos da Rede.
"Rede" é marca de seus respectivos titulares.

## Créditos

Desenvolvido e mantido pela **JY Labtech**: [jylabtech.com.br](https://jylabtech.com.br).

Autores: [Jhonathan Queiroz](https://github.com/jhonathanqz) e
[Gabul Dev](https://github.com/gabuldev) (estrutura original, derivada do
`pagseguro_smart_flutter`).
