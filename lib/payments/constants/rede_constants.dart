/// URL oficial de validação de licença da JY Labtech, em base64
/// (`https://portal-api.jylabtech.com.br/pos-payments/licence/check`).
///
/// É o valor padrão de `licenceInternalKey`. O nativo só aceita URLs do
/// servidor oficial; qualquer outro valor é ignorado e esta URL é usada.
const String licenceCheck =
    'aHR0cHM6Ly9wb3J0YWwtYXBpLmp5bGFidGVjaC5jb20uYnIvcG9zLXBheW1lbnRzL2xpY2VuY2UvY2hlY2s=';

abstract class RedeConstants {
  static const String channel = 'rede_payment_tech';
}
