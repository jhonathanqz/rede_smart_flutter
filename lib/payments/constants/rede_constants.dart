/// URL oficial de validação de licença da JY Labtech, em base64
/// (`https://licence-server.invalid/pos-payments/licence/check`).
///
/// É o valor padrão de `licenceInternalKey`. O nativo só aceita URLs do
/// servidor oficial; qualquer outro valor é ignorado e esta URL é usada.
const String licenceCheck =
    '***REMOVIDO***';

abstract class RedeConstants {
  static const String channel = 'rede_payment_tech';
}
