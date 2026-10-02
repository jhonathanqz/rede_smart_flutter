## 1.0.0

- Pacote renomeado para `rede_payment_tech` (classe principal `RedeTech`, pacote Android `br.com.jylabtech.rede_payment_tech`).
- Adicionado `configureLicence(licenceKey, licenceInternalKey)`: as chaves devem ser configuradas antes de usar o SDK (caso contrário, erro `LICENCE_NOT_CONFIGURED`).
- Validação de licença online no servidor oficial da JY Labtech (`https://portal-api.jylabtech.com.br/pos-payments/licence/check`): `configureLicence` lança `LICENCE_ERROR` se a licença for inválida, vencida, suspensa ou cancelada, com a mensagem do servidor (404, 412, 428, 429, 5xx etc.).
- **Toda operação** (pagamentos, estorno, reimpressão, autenticação e dados do POS) verifica a licença antes de chamar o SDK da Rede e falha com `LICENCE_ERROR` se ela não for válida.
- `licenceInternalKey` agora é opcional (padrão: constante `licenceCheck`). Valores que não apontem para o servidor oficial (inclusive a URL antiga) são ignorados.
- Uma resposta de sucesso só libera o uso se a licença estiver ativa (validade futura, não vencida, inativa nem cancelada).
- Cache offline criptografado (EncryptedSharedPreferences) válido por até 7 dias, invalidado se a chave mudar, se a licença vencer, se a data salva estiver no futuro ou se tiver sido salvo com URL fora do servidor oficial.
- Consulta de licença fora da thread principal, com timeouts de 15 s.
- Corrigido: crédito parcelado (`creditParcIssue`/`creditParcCustomer`) executava crédito à vista no nativo.
- Corrigido o build Android: binário nativo `android/libs/rede_payment_tech-native.jar` gerado por `scripts/build_android_aar.sh`, SDK da Rede referenciado via `flatDir` e dependência `androidx.security:security-crypto`.
- `minSdk` do plugin e do README corrigido para 28 (exigido pelo SDK da Rede).
- Testes Dart (`flutter test`) e testes JVM da validação de licença no módulo nativo.
- Documentação de licença apontando para o novo portal (https://portal.jylabtech.com.br) e o novo `licenceInternalKey` (`https://portal-api.jylabtech.com.br/pos-payments/licence/check`).
- Corrigido: funções de autenticação (`authWithOnlyPin`, `authWithChangePin`, `authWithValueAndPin`) não enviavam `description`/valor ao nativo.
- Autenticação: novo parâmetro nomeado `amount` (valor da transação em centavos, o que o SDK da Rede espera) em `authWithOnlyPin`, `authWithChangePin` e `authWithValueAndPin`. O parâmetro `pin` continua aceito, mas está obsoleto (`@Deprecated`): ele sempre foi enviado ao nativo como `amount`. `amount` e `pin` diferentes, ou nenhum dos dois, lançam `ArgumentError` (antes `pin` era obrigatório em tempo de compilação).
- Corrigido: argumentos numéricos (`amount`, `installment`) eram lidos no nativo com cast direto para `int`, o que quebrava com `ClassCastException` para valores acima de 2.147.483.647 (o Flutter os entrega como `Long`) e com `NullPointerException` quando faltavam. Agora são lidos como `Number` (`amount` repassado ao SDK da Rede como `long`, como ele espera) e argumento ausente, não numérico ou fracionado devolve `PlatformException` com código `INVALID_ARGUMENT` em vez de derrubar o app ou deixar o Future sem resposta. Binários `android/libs/rede_payment_tech-native.{aar,jar}` regenerados.
- Corrigido: toda chamada ao nativo agora responde exatamente uma vez. Antes, se o app da Rede não fosse encontrado ou o SDK recusasse os dados do pagamento, o erro só ia para o log e o `Future` nunca completava; exceções do SDK na autenticação e na leitura de dados do POS subiam sem tratamento; um retorno de pagamento/estorno com sucesso mas sem dados, ou com `resultCode` desconhecido, ficava sem resposta; e uma resposta pendente antiga podia ser respondida de novo. Novos códigos de `PlatformException`: `REDE_APP_NOT_FOUND`, `INVALID_PAYMENT`, `INVALID_AUTH`, `REDE_SDK_ERROR`, `UNEXPECTED_RESULT`, `OPERATION_REPLACED` e `REDE_ERROR` (veja a tabela de erros no README). **Mudança visível:** esses `Future`s, que antes ficavam pendentes para sempre, agora completam com erro.
- Teste JVM da classe ponte Android (`RedeTechFlutterPluginTest`) reescrito: o anterior usava Mockito (não declarado) e testava `getPlatformVersion`, que não existe. Agora cobre a delegação ao nativo (licença sem chave, operação sem Activity, `onActivityResult` sem operação pendente) sem dependências extras.
- Example atualizado para chamar `configureLicence` (chave via `--dart-define=LICENCE_KEY=...`).

## 0.0.1

- Initial release
