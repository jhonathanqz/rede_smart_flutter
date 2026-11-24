<h1 align="center">Rede Payment Tech</h1>

<div align="center" id="top"> 
  <img src="https://logodownload.org/wp-content/uploads/2014/07/rede-logo-1-1.png" alt="Fiserv" height=100 />
</div>
<br>

<p align="center">
  <a href="#dart-sobre">Sobre</a> &#xa0; | &#xa0; 
  <a href="#rocket-tecnologias">Tecnologias</a> &#xa0; | &#xa0;
  <a href="#checkered_flag-configuração">Configuração</a> &#xa0; | &#xa0;
  <a href="#key-licença">Licença</a> &#xa0; | &#xa0;
  <a href="#memo-autores">Autores</a> &#xa0; | &#xa0;
</p>

<br>

## \*\*\*\* Plugin não oficial!!! \*\*\*\*

## :dart: Sobre

Projeto destinado a facilitar a integração com o SDK da REDE no Flutter.

## :rocket: Tecnologias

As seguintes ferramentas foram usadas na construção do projeto:

- [Flutter](https://flutter.dev/) v3.24.5

## :checkered_flag: Configuração

Para adicionar esse plugin no seu projeto Flutter, basta inserir a depêncida no seu arquivo `pubspec.yaml`.

### # Pubspec.yaml

Para usar este plugin, adicione `rede_payment_tech` como [dependência](https://flutter.io/using-packages/) ao seu arquivo `pubspec.yaml`.

```yaml
dependencies:
  rede_payment_tech: ^1.1.1
```

### # Build.gradle

Em seu build.gradle a nivel do app, a propriedade `minSdkVersion` precisa ser no mínimo 28 (exigido pelo SDK da Rede, `smartrede_sdk`).

```xml
...
defaultConfig {
        applicationId "com.example.rede_example"
        minSdkVersion 28
        targetSdkVersion flutter.targetSdkVersion
        versionCode flutterVersionCode.toInteger()
        versionName flutterVersionName
    }
...
```

### # Implementação

Para começar é necessário criar uma instancia da classe `RedeTech` e configurar a licença
(veja a seção [Licença](#key-licença)) **antes** de chamar qualquer outra função.

```dart
final redeTech = RedeTech();

try {
  // licenceInternalKey é opcional: o padrão já é o servidor oficial da JY Labtech.
  await redeTech.configureLicence(licenceKey: 'SUA_LICENCE_KEY');
} on PlatformException catch (e) {
  // e.code == 'LICENCE_ERROR' -> licença inválida, vencida, suspensa ou cancelada
}
```

> As chaves ficam armazenadas na instância do plugin nativo (são compartilhadas pelo canal
> `rede_payment_tech`), então basta configurar uma vez por execução do app.

Função de crédito

```dart
 await RedeTech().payment.paymentTransaction(
                  paymentType: RedePaymentType.credit,
                  amount: 1540,
                );
```

Função de débito

```dart
 await RedeTech().payment.paymentTransaction(
                  paymentType: RedePaymentType.debit,
                  amount: 1540,
                );
```

Função de voucher

```dart
 await RedeTech().payment.paymentTransaction(
                  paymentType: RedePaymentType.voucher,
                  amount: 1540,
                );
```

Função de pix

```dart
 await RedeTech().payment.paymentTransaction(
                  paymentType: RedePaymentType.pix,
                  amount: 1540,
                );
```

Função de crédito parcelado pelo consumidor

```dart
 await RedeTech().payment.paymentTransaction(
                  paymentType: RedePaymentType.creditParcCustomer,
                  amount: 1540,
                  installment: 5,
                );
```

Função de crédito parcelado pelo estabelecimento

```dart
    await RedeTech().payment.paymentTransaction(
                  paymentType: RedePaymentType.creditParcIssue,
                  amount: 1540,
                  installment: 6,
                );
```

Função para reversão de pagamento

```dart
 await RedeTech().payment.reversalTransaction();
```

Função para reimprimir

```dart
 await RedeTech().payment.reprintTransaction();
```

#### POS

Função para obter IMEI do device

```dart
 await RedeTech().pos.getIMEI();
```

Função para obter o número serial do device

```dart
 await RedeTech().pos.getSerialNumber();
```

Função para obter número do SIM Card

```dart
 await RedeTech().pos.getSimCardSerialNumber();
```

#### Autenticação

Funções de autenticação do SDK da Rede. `amount` é o **valor da transação em centavos**
(o SDK da Rede recebe `description` + valor; o PIN é digitado pelo cliente na tela do SDK).

```dart
 await RedeTech().auth.authWithOnlyPin(description: 'Descrição', amount: 1000);
 await RedeTech().auth.authWithChangePin(description: 'Descrição', amount: 1000);
 await RedeTech().auth.authWithValueAndPin(description: 'Descrição', amount: 1000);
```

> O parâmetro `pin` ainda é aceito por compatibilidade, mas está **obsoleto** (`@Deprecated`):
> ele sempre foi enviado ao SDK como o valor (`amount`). Migre para `amount`. Informar `amount` e
> `pin` com valores diferentes, ou nenhum dos dois, lança `ArgumentError`.

## :key: Licença

O uso do plugin exige uma licença JY Labtech.

### Onde adquirir

Adquira e gerencie suas licenças no portal **[portal.jylabtech.com.br](https://portal.jylabtech.com.br)**
(o login é o mesmo e-mail/senha do portal anterior). O produto é único — **"Licença plugin de
pagamento"** — e vale para qualquer plugin de pagamento da JY Labtech.

- **Chaves existentes continuam valendo**: não é preciso gerar uma nova `licenceKey`.
- **Plano `debug`**: concedido automaticamente como cortesia no autocadastro (2 aparelhos, 365 dias).
- **Plano `mensal`**: cobrado por dispositivo. Aparelhos podem ser desativados pelo portal.

### Parâmetros

| Parâmetro            | Descrição                                                                                                  |
| -------------------- | ---------------------------------------------------------------------------------------------------------- |
| `licenceKey`         | Chave da licença obtida no portal (obrigatória).                                                            |
| `licenceInternalKey` | Opcional. Padrão: constante `licenceCheck` (base64 de `https://licence-server.invalid/pos-payments/licence/check`). |

O plugin **sempre valida a licença no servidor oficial da JY Labtech**
(`https://licence-server.invalid/pos-payments/licence/check`). Um `licenceInternalKey` que não
aponte para esse servidor (inclusive o valor antigo, começando com `***REMOVIDO***...`)
é ignorado e a URL oficial é usada. Não é preciso informá-lo.

### Como a validação funciona

- `configureLicence` registra as chaves e valida a licença online (`POST` com
  `Authorization: Bearer licence:<licenceKey>` e o identificador do aparelho). Se a licença não for
  válida, lança `PlatformException` com código `LICENCE_ERROR`.
- **Toda operação** (pagamentos, estorno, reimpressão, autenticação e dados do POS) verifica a
  licença antes de chamar o SDK da Rede. Se a licença for inválida, vencida, suspensa ou cancelada,
  a operação **não é executada** e o `Future` falha com `LICENCE_ERROR`.
- A licença só é aceita se o servidor responder com sucesso **e** ela estiver ativa: com data de
  validade futura e não vencida, inativa ou cancelada.
- **Revalidação**: a primeira operação depois de abrir o app sempre consulta o servidor; depois,
  a licença é consultada de novo a cada **4 horas** de uso.
- **Bloqueio imediato**: se o servidor recusar a licença (cliente bloqueado no portal, licença
  vencida, cancelada ou suspensa, não encontrada, aparelho desativado ou limite de aparelhos), o
  cache é apagado e as operações passam a falhar com `LICENCE_ERROR` na hora.
- **Cache offline**: após uma validação bem-sucedida, o resultado fica salvo de forma criptografada
  (EncryptedSharedPreferences). Ele só é usado quando o servidor não responde (sem internet,
  timeout ou erro 5xx) e por no máximo **72 horas** desde a última validação online, desde que a
  licença continue dentro da validade e a `licenceKey` seja a mesma. Depois de uma falha de rede o
  plugin espera 5 minutos antes de tentar o servidor de novo. Cache vencido, de outra chave, salvo
  com data no futuro (relógio adulterado) ou salvo com uma URL fora do servidor oficial é apagado.
  Sem internet e sem cache válido, as operações falham com `LICENCE_ERROR`.
- A consulta ao servidor roda fora da thread principal, com timeout de 15 s para conexão e 15 s
  para leitura.

### Erros

Erros que podem ocorrer (`PlatformException.code`):

| Código                   | Quando ocorre                                                                                 |
| ------------------------ | --------------------------------------------------------------------------------------------- |
| `LICENCE_ERROR`          | Licença inválida ou não foi possível validá-la. A `message` traz o motivo (veja abaixo).      |
| `INVALID_ARGUMENT`       | `configureLicence` chamado com `licenceKey` vazio, ou operação com `amount`/`installment` ausente, não numérico ou fracionado. |
| `LICENCE_NOT_CONFIGURED` | Alguma função de pagamento/POS/autenticação foi chamada antes de `configureLicence`.          |
| `ACTIVITY_NOT_ATTACHED`  | O plugin ainda não está anexado a uma Activity (chame após o app estar em primeiro plano).    |
| `REDE_APP_NOT_FOUND`     | O aplicativo de pagamentos/autenticação da Rede não foi encontrado no terminal.               |
| `INVALID_PAYMENT`        | O SDK da Rede recusou os dados do pagamento (ex.: valor ou parcelas inválidos).               |
| `INVALID_AUTH`           | O SDK da Rede recusou os dados da autenticação (ex.: descrição ou valor inválidos).           |
| `REDE_SDK_ERROR`         | O SDK da Rede está indisponível ao ler dados do POS (IMEI, número de série, SIM).             |
| `UNEXPECTED_RESULT`      | O app da Rede voltou sem os dados da transação ou com um resultado desconhecido.              |
| `OPERATION_REPLACED`     | Uma nova operação com tela da Rede foi iniciada antes de a anterior responder.                |
| `REDE_ERROR`             | Falha inesperada ao chamar o SDK da Rede (a `message` traz o detalhe).                        |

Toda chamada responde **exatamente uma vez**: com sucesso ou com um dos erros acima. Antes,
falhas ao abrir o app da Rede eram só registradas em log e o `Future` nunca completava, e
exceções do SDK na autenticação ou nos dados do POS podiam derrubar o app.

Mensagens de `LICENCE_ERROR` (vêm do servidor quando ele responde com erro):

| Situação                                      | Origem                                                       |
| --------------------------------------------- | ------------------------------------------------------------ |
| Licença não encontrada                        | HTTP 404                                                     |
| Limite de aparelhos atingido / aparelho desativado no portal | HTTP 412                                      |
| Licença vencida, suspensa ou cancelada        | HTTP 428, ou resposta com licença vencida/inativa/cancelada  |
| Muitas tentativas                             | HTTP 429                                                     |
| Aparelho sem identificador estável            | HTTP 400                                                     |
| Erro interno do servidor                      | HTTP 5xx (`Erro ao validar licença (HTTP <código>)` se não houver mensagem) |
| Sem conexão / tempo esgotado                  | Falha de rede                                                |

### Solução de problemas

- **`LICENCE_ERROR`**: leia a `message`. Confira no portal
  [portal.jylabtech.com.br](https://portal.jylabtech.com.br) se a licença está ativa, dentro da
  validade e se o aparelho não foi desativado ou excedeu o limite. Sem internet, a operação só
  funciona se houver uma validação bem-sucedida nas últimas 72 horas.
- **`LICENCE_NOT_CONFIGURED`**: chame `configureLicence` (com `await`) antes de qualquer outra função.
  Se o app for reiniciado, a configuração precisa ser feita novamente.
- **`INVALID_ARGUMENT`**: confira se `licenceKey` não está vazio e se `amount`/`installment` foram informados como inteiros (centavos / número de parcelas).

## :test_tube: Testes

```bash
# Testes Dart (MethodChannel mockado)
flutter test

# Testes JVM da validação de licença (nativo)
cd example/android
./gradlew -p ../../android/native testDebugUnitTest
```

Para regenerar o binário nativo (`android/libs/rede_payment_tech-native.jar`) após alterar
`android/native`, execute `scripts/build_android_aar.sh`.

## :memo: Autores

Este projeto foi desenvolvido por:

<div> 
<a href="https://github.com/gabuldev">
  <img src="https://avatars.githubusercontent.com/u/32063378?v=4" height=90 />
</a>
<a href="https://github.com/jhonathanqz">
  <img src="https://avatars.githubusercontent.com/u/74057391?s=96&v=4" height=90 />
</a>

<br>
<a href="https://github.com/gabuldev" target="_blank">Gabul Dev</a>, 
<a href="https://github.com/jhonathanqz" target="_blank">Jhonathan Queiroz</a>

</div>

&#xa0;

<a href="#top">Voltar para o topo</a>
