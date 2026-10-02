package br.com.jylabtech.rede_payment_tech.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class RedeOperationExceptionTest {

    @Test
    public void from_keepsCodeOfRedeOperationException() {
        RedeOperationException original = new RedeOperationException(
                RedeOperationException.INVALID_PAYMENT, "valor inválido");

        RedeOperationException mapped = RedeOperationException.from(original);

        assertSame(original, mapped);
        assertEquals("INVALID_PAYMENT", mapped.getCode());
    }

    @Test
    public void from_unexpectedExceptionBecomesRedeErrorWithDetail() {
        RedeOperationException mapped = RedeOperationException.from(new IllegalStateException("serviço caiu"));

        assertEquals("REDE_ERROR", mapped.getCode());
        assertEquals("Erro ao executar a operação na Rede: serviço caiu", mapped.getMessage());
    }

    @Test
    public void from_exceptionWithoutMessageStillHasClearMessage() {
        RedeOperationException mapped = RedeOperationException.from(new NullPointerException());

        assertEquals("REDE_ERROR", mapped.getCode());
        assertEquals("Erro ao executar a operação na Rede.", mapped.getMessage());
    }

    @Test
    public void codesMatchTheDocumentedPlatformExceptionCodes() {
        assertEquals("REDE_APP_NOT_FOUND", RedeOperationException.REDE_APP_NOT_FOUND);
        assertEquals("INVALID_PAYMENT", RedeOperationException.INVALID_PAYMENT);
        assertEquals("INVALID_AUTH", RedeOperationException.INVALID_AUTH);
        assertEquals("REDE_SDK_ERROR", RedeOperationException.REDE_SDK_ERROR);
        assertEquals("UNEXPECTED_RESULT", RedeOperationException.UNEXPECTED_RESULT);
        assertEquals("OPERATION_REPLACED", RedeOperationException.OPERATION_REPLACED);
    }
}
