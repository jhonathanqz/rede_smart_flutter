package br.com.jylabtech.rede_payment_tech.core;

/**
 * Leitura segura de argumentos numéricos vindos do MethodChannel (sem dependências Android).
 *
 * <p>O codec do Flutter entrega inteiros Dart como {@link Integer} ou {@link Long} conforme o
 * tamanho; um cast direto ({@code int x = call.argument("x")}) quebra com ClassCastException
 * para valores grandes e com NullPointerException quando o argumento falta. Aqui o valor é lido
 * como {@link Number} e argumentos ausentes ou inválidos viram {@link IllegalArgumentException},
 * que o plugin devolve ao Dart como {@code INVALID_ARGUMENT}.
 */
public final class MethodArguments {

    public static final String INVALID_ARGUMENT = "INVALID_ARGUMENT";

    private MethodArguments() {
    }

    /** Valor inteiro obrigatório (ex.: valor da transação em centavos, que o SDK recebe como long). */
    public static long requireLong(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " é obrigatório");
        }
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException(name + " deve ser um número inteiro");
        }
        Number number = (Number) value;
        if (value instanceof Double || value instanceof Float) {
            double d = number.doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d) || d != Math.rint(d)
                    || d > Long.MAX_VALUE || d < Long.MIN_VALUE) {
                throw new IllegalArgumentException(name + " deve ser um número inteiro");
            }
        }
        return number.longValue();
    }

    /** Valor inteiro obrigatório que precisa caber em int (ex.: número de parcelas). */
    public static int requireInt(Object value, String name) {
        long v = requireLong(value, name);
        if (v > Integer.MAX_VALUE || v < Integer.MIN_VALUE) {
            throw new IllegalArgumentException(name + " fora do intervalo permitido");
        }
        return (int) v;
    }
}
