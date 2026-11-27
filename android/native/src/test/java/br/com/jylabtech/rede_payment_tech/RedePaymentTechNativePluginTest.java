package br.com.jylabtech.rede_payment_tech;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import br.com.jylabtech.rede_payment_tech.constants.RedeConstants;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

import br.com.jylabtech.rede_payment_tech.core.ReplyOnce;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Testes JVM do {@link RedePaymentTechNativePlugin} sem Activity/SDK: toda chamada responde uma
 * única vez e nenhuma depende de configuração prévia.
 */
public class RedePaymentTechNativePluginTest {

  private static final class RecordingResult implements MethodChannel.Result {
    int replies;
    String kind;
    String errorCode;
    String errorMessage;

    @Override
    public void success(@Nullable Object result) {
      replies++;
      kind = "success";
    }

    @Override
    public void error(@NonNull String code, @Nullable String message, @Nullable Object details) {
      replies++;
      kind = "error";
      errorCode = code;
      errorMessage = message;
    }

    @Override
    public void notImplemented() {
      replies++;
      kind = "notImplemented";
    }
  }

  private static final String[] BUSINESS_METHODS = {
      "PaymentCredit", "PaymentDebit", "PaymentVoucher", "PaymentPix",
      "PaymentCreditParcIssuer", "PaymentCreditParcCustomer", "RedeReversal", "RedeReprint",
      "AuthOnlyPin", "AuthChangePin", "AuthValueAndPin",
      "POSIMEI", "POSSerialNumber", "POSSimSerialNumber",
  };

  @Test
  public void everyMethod_withoutActivity_repliesOnceWithActivityNotAttached() {
    for (String method : BUSINESS_METHODS) {
      RedePaymentTechNativePlugin plugin = new RedePaymentTechNativePlugin();
      RecordingResult result = new RecordingResult();
      Map<String, Object> args = new HashMap<>();
      args.put("amount", 1540);
      args.put("installment", 3);
      args.put("description", "d");

      plugin.onMethodCall(new MethodCall(method, args), result);

      assertEquals(method, 1, result.replies);
      assertEquals(method, "error", result.kind);
      assertEquals(method, "ACTIVITY_NOT_ATTACHED", result.errorCode);
    }
  }

  @Test
  public void onActivityResult_ignoresRequestCodesFromOtherParts() {
    RedePaymentTechNativePlugin plugin = new RedePaymentTechNativePlugin();

    assertFalse(plugin.onActivityResult(1, -1, null));
  }

  @Test
  public void onActivityResult_consumesPluginRequestCodesEvenWithoutPendingOperation() {
    RedePaymentTechNativePlugin plugin = new RedePaymentTechNativePlugin();

    assertTrue(plugin.onActivityResult(RedeConstants.PAYMENT_REQUEST_CODE, -1, null));
    assertTrue(plugin.onActivityResult(RedeConstants.REVERSAL_REQUEST_CODE, 0, null));
    assertTrue(plugin.onActivityResult(RedeConstants.AUTH_REQUEST_CODE, -1, null));
    assertTrue(plugin.onActivityResult(RedeConstants.REPRINT_REQUEST_CODE, 0, null));
  }

  private static void setPending(ReplyOnce reply) throws Exception {
    Field field = RedePaymentTechNativePlugin.class.getDeclaredField("pendingResult");
    field.setAccessible(true);
    field.set(null, reply);
  }

  @Test
  public void paymentCancelledWithoutData_repliesStructuredCanceledError() throws Exception {
    RedePaymentTechNativePlugin plugin = new RedePaymentTechNativePlugin();
    RecordingResult result = new RecordingResult();
    setPending(new ReplyOnce(result));

    assertTrue(plugin.onActivityResult(RedeConstants.PAYMENT_REQUEST_CODE, 0, null));

    assertEquals(1, result.replies);
    assertEquals("error", result.kind);
    assertEquals("CANCELED", result.errorCode);
    assertEquals("Operação de pagamento cancelada", result.errorMessage);
  }

  @Test
  public void reversalCancelledWithoutData_repliesStructuredCanceledError() throws Exception {
    RedePaymentTechNativePlugin plugin = new RedePaymentTechNativePlugin();
    RecordingResult result = new RecordingResult();
    setPending(new ReplyOnce(result));

    assertTrue(plugin.onActivityResult(RedeConstants.REVERSAL_REQUEST_CODE, 0, null));
    // A pendência foi consumida: um segundo retorno não responde de novo.
    assertTrue(plugin.onActivityResult(RedeConstants.REVERSAL_REQUEST_CODE, 0, null));

    assertEquals(1, result.replies);
    assertEquals("CANCELED", result.errorCode);
    assertEquals("Operação de reversão cancelada", result.errorMessage);
  }

  @Test
  public void paymentOkWithoutData_repliesUnexpectedResult() throws Exception {
    RedePaymentTechNativePlugin plugin = new RedePaymentTechNativePlugin();
    RecordingResult result = new RecordingResult();
    setPending(new ReplyOnce(result));

    assertTrue(plugin.onActivityResult(RedeConstants.PAYMENT_REQUEST_CODE, -1, null));

    assertEquals(1, result.replies);
    assertEquals("UNEXPECTED_RESULT", result.errorCode);
  }
}
