package br.com.jylabtech.rede_payment_tech.core;

/**
 * Falha de uma operação da Rede com o código que o Dart recebe em {@code PlatformException.code}.
 * Os usecases convertem as exceções do SDK nela; o {@link RedeTech} responde o Result com o erro.
 */
public final class RedeOperationException extends RuntimeException {

    /** App de pagamentos da Rede não instalado/não encontrado no terminal. */
    public static final String REDE_APP_NOT_FOUND = "REDE_APP_NOT_FOUND";
    /** O SDK recusou os dados do pagamento (valor, parcelas etc.). */
    public static final String INVALID_PAYMENT = "INVALID_PAYMENT";
    /** O SDK recusou os dados da autenticação (descrição, valor etc.). */
    public static final String INVALID_AUTH = "INVALID_AUTH";
    /** O SDK da Rede está em estado inválido (ex.: serviço do terminal indisponível). */
    public static final String REDE_SDK_ERROR = "REDE_SDK_ERROR";
    /** Falha inesperada ao chamar o SDK. */
    public static final String REDE_ERROR = "REDE_ERROR";
    /** O app da Rede devolveu um resultado que o plugin não sabe interpretar. */
    public static final String UNEXPECTED_RESULT = "UNEXPECTED_RESULT";
    /** Uma nova operação com tela da Rede começou antes de a anterior responder. */
    public static final String OPERATION_REPLACED = "OPERATION_REPLACED";
    public static final String ACTIVITY_NOT_ATTACHED = "ACTIVITY_NOT_ATTACHED";
    /** Pagamento/estorno cancelado e o app da Rede não devolveu dados da transação. */
    public static final String CANCELED = "CANCELED";

    private final String code;

    public RedeOperationException(String code, String message) {
        this(code, message, null);
    }

    public RedeOperationException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /** Código e mensagem para responder o Dart a partir de qualquer falha. */
    public static RedeOperationException from(Throwable t) {
        if (t instanceof RedeOperationException) {
            return (RedeOperationException) t;
        }
        return new RedeOperationException(REDE_ERROR, withDetail("Erro ao executar a operação na Rede", t), t);
    }

    /** "prefixo: mensagem da exceção" (ou só o prefixo se a exceção não tiver mensagem). */
    public static String withDetail(String prefix, Throwable t) {
        String detail = t != null ? t.getMessage() : null;
        if (detail == null || detail.trim().isEmpty()) {
            return prefix + ".";
        }
        return prefix + ": " + detail;
    }
}
