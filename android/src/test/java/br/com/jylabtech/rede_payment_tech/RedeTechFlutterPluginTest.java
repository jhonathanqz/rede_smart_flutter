package br.com.jylabtech.rede_payment_tech;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Testes JVM da classe ponte {@link RedeTechFlutterPlugin}: ela precisa repassar tudo para o
 * {@code RedePaymentTechNativePlugin} (binário em android/libs) sem chamar o SDK da Rede quando
 * não há licença/Activity.
 *
 * <p>Rode com {@code cd example/android && ./gradlew :rede_payment_tech:testDebugUnitTest}.
 * Sem Mockito: o {@link MethodChannel.Result} é um fake que só grava a resposta.
 */
public class RedeTechFlutterPluginTest {

  /** Result fake que grava a última resposta recebida. */
  private static final class RecordingResult implements MethodChannel.Result {
    String kind;
    Object value;
    String errorCode;
    String errorMessage;

    @Override
    public void success(@Nullable Object result) {
      kind = "success";
      value = result;
    }

    @Override
    public void error(@NonNull String code, @Nullable String message, @Nullable Object details) {
      kind = "error";
      errorCode = code;
      errorMessage = message;
    }

    @Override
    public void notImplemented() {
      kind = "notImplemented";
    }
  }

  @Test
  public void implementsAllFlutterInterfacesNeededByTheNativeDelegate() {
    RedeTechFlutterPlugin plugin = new RedeTechFlutterPlugin();

    assertTrue(plugin instanceof FlutterPlugin);
    assertTrue(plugin instanceof MethodChannel.MethodCallHandler);
    assertTrue(plugin instanceof ActivityAware);
    assertTrue(plugin instanceof PluginRegistry.ActivityResultListener);
  }

  @Test
  public void configureLicence_withoutLicenceKey_isDelegatedAndReturnsInvalidArgument() {
    RedeTechFlutterPlugin plugin = new RedeTechFlutterPlugin();
    RecordingResult result = new RecordingResult();

    plugin.onMethodCall(new MethodCall("RedeConfigureLicence", new HashMap<String, Object>()), result);

    assertEquals("error", result.kind);
    assertEquals("INVALID_ARGUMENT", result.errorCode);
  }

  @Test
  public void configureLicence_withBlankLicenceKey_returnsInvalidArgument() {
    RedeTechFlutterPlugin plugin = new RedeTechFlutterPlugin();
    RecordingResult result = new RecordingResult();
    Map<String, Object> args = new HashMap<>();
    args.put("licenceKey", "   ");

    plugin.onMethodCall(new MethodCall("RedeConfigureLicence", args), result);

    assertEquals("error", result.kind);
    assertEquals("INVALID_ARGUMENT", result.errorCode);
  }

  @Test
  public void businessMethod_withoutActivity_isRejectedBeforeReachingTheSdk() {
    RedeTechFlutterPlugin plugin = new RedeTechFlutterPlugin();
    RecordingResult result = new RecordingResult();
    Map<String, Object> args = new HashMap<>();
    args.put("amount", 1000);

    plugin.onMethodCall(new MethodCall("PaymentCredit", args), result);

    assertEquals("error", result.kind);
    assertEquals("ACTIVITY_NOT_ATTACHED", result.errorCode);
  }

  @Test
  public void unknownMethod_withoutActivity_isRejectedInsteadOfHanging() {
    RedeTechFlutterPlugin plugin = new RedeTechFlutterPlugin();
    RecordingResult result = new RecordingResult();

    plugin.onMethodCall(new MethodCall("MetodoInexistente", null), result);

    assertEquals("error", result.kind);
    assertEquals("ACTIVITY_NOT_ATTACHED", result.errorCode);
    assertNull(result.value);
  }

  @Test
  public void onActivityResult_withoutPendingOperation_returnsFalse() {
    RedeTechFlutterPlugin plugin = new RedeTechFlutterPlugin();

    assertFalse(plugin.onActivityResult(1, 0, null));
  }
}
