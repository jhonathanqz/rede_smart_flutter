package br.com.jylabtech.rede_payment_tech.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MethodArgumentsTest {

    @Test
    public void requireLong_acceptsIntegerFromCodec() {
        assertEquals(1500L, MethodArguments.requireLong(1500, "amount"));
    }

    @Test
    public void requireLong_acceptsLongAboveIntRange() {
        // O codec do Flutter entrega Long para inteiros Dart > 2^31-1; o cast direto para int quebrava.
        long big = 3_000_000_000L;
        assertEquals(big, MethodArguments.requireLong(big, "amount"));
    }

    @Test
    public void requireLong_acceptsIntegralDouble() {
        assertEquals(100L, MethodArguments.requireLong(100.0, "amount"));
    }

    @Test
    public void requireLong_nullThrowsIllegalArgumentWithName() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> MethodArguments.requireLong(null, "amount"));
        assertTrue(e.getMessage().contains("amount"));
    }

    @Test
    public void requireLong_rejectsNonNumber() {
        assertThrows(IllegalArgumentException.class, () -> MethodArguments.requireLong("100", "amount"));
    }

    @Test
    public void requireLong_rejectsFractionalOrNonFiniteDouble() {
        assertThrows(IllegalArgumentException.class, () -> MethodArguments.requireLong(10.5, "amount"));
        assertThrows(IllegalArgumentException.class, () -> MethodArguments.requireLong(Double.NaN, "amount"));
        assertThrows(IllegalArgumentException.class,
                () -> MethodArguments.requireLong(Double.POSITIVE_INFINITY, "amount"));
    }

    @Test
    public void requireInt_acceptsIntAndSmallLong() {
        assertEquals(3, MethodArguments.requireInt(3, "installment"));
        assertEquals(12, MethodArguments.requireInt(12L, "installment"));
    }

    @Test
    public void requireInt_rejectsValueOutsideIntRange() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> MethodArguments.requireInt(3_000_000_000L, "installment"));
        assertTrue(e.getMessage().contains("installment"));
    }

    @Test
    public void requireInt_nullThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> MethodArguments.requireInt(null, "installment"));
    }
}
