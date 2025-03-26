<h1 align="center">Rede Smart Flutter</h1>

<div align="center" id="top"> 
  <img src="https://logodownload.org/wp-content/uploads/2014/07/rede-logo-1-1.png" alt="Fiserv" height=100 />
</div>
<br>

<p align="center">
  <a href="#dart-sobre">Sobre</a> &#xa0; | &#xa0; 
  <a href="#rocket-tecnologias">Tecnologias</a> &#xa0; | &#xa0;
  <a href="#checkered_flag-configuração">Configuração</a> &#xa0; | &#xa0;
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

Para usar este plugin, adicione `rede_smart_flutter` como [dependência](https://flutter.io/using-packages/) ao seu arquivo `pubspec.yaml`.

```yaml
dependencies:
  rede_smart_flutter: any
```

### # Build.gradle

Em seu build.gradle a nivel do app, a propriedade `minSdkVersion` precisa ser level 23. Recurso este exigido pelo SDK.

```xml
...
defaultConfig {
        applicationId "com.example.rede_example"
        minSdkVersion 23
        targetSdkVersion flutter.targetSdkVersion
        versionCode flutterVersionCode.toInteger()
        versionName flutterVersionName
    }
...
```

### # Implementação

Para começar é necessário criar uma instancia da classe `RedeSmart`.

Função de crédito

```dart
 await RedeSmartFlutter().payment.paymentTransaction(
                  paymentType: RedePaymentType.credit,
                  amount: 1540,
                );
```

Função de débito

```dart
 await RedeSmartFlutter().payment.paymentTransaction(
                  paymentType: RedePaymentType.debit,
                  amount: 1540,
                );
```

Função de voucher

```dart
 await RedeSmartFlutter().payment.paymentTransaction(
                  paymentType: RedePaymentType.voucher,
                  amount: 1540,
                );
```

Função de pix

```dart
 await RedeSmartFlutter().payment.paymentTransaction(
                  paymentType: RedePaymentType.pix,
                  amount: 1540,
                );
```

Função de crédito parcelado pelo consumidor

```dart
 await RedeSmartFlutter().payment.paymentTransaction(
                  paymentType: RedePaymentType.creditParcCustomer,
                  amount: 1540,
                  installment: 5,
                );
```

Função de crédito parcelado pelo estabelecimento

```dart
    await RedeSmartFlutter().payment.paymentTransaction(
                  paymentType: RedePaymentType.creditParcIssue,
                  amount: 1540,
                  installment: 6,
                );
```

Função para reversão de pagamento

```dart
 await RedeSmartFlutter().payment.reversalTransaction();
```

Função para reimprimir

```dart
 await RedeSmartFlutter().payment.reprintTransaction();
```

#### POS

Função para obter IMEI do device

```dart
 await RedeSmartFlutter().pos.getIMEI();
```

Função para obter o número serial do device

```dart
 await RedeSmartFlutter().pos.getSerialNumber();
```

Função para obter número do SIM Card

```dart
 await RedeSmartFlutter().pos.getSimCardSerialNumber();
```

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
