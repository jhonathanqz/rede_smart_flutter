package br.com.jylabtech.rede_payment_tech;

import static br.com.jylabtech.rede_payment_tech.constants.RedeConstants.REVERSAL_REQUEST_CODE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import br.com.jylabtech.rede_payment_tech.constants.RedeResponse;
import br.com.jylabtech.rede_payment_tech.core.ActivityResultOutcome;
import br.com.jylabtech.rede_payment_tech.core.RedeOperationException;
import br.com.jylabtech.rede_payment_tech.core.RedeTech;
import br.com.jylabtech.rede_payment_tech.core.ReplyOnce;
import br.com.jylabtech.rede_payment_tech.licence.CheckLicenceTech;
import br.com.jylabtech.rede_payment_tech.licence.LicenceResult;
import br.com.jylabtech.rede_payment_tech.licence.LicenceRules;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import rede.smartrede.sdk.Payment;
import rede.smartrede.sdk.RedePayments;

public class RedePaymentTechNativePlugin implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {

  private static final String CHANNEL_NAME = "rede_payment_tech";
  private static final String METHOD_CONFIGURE_LICENCE = "RedeConfigureLicence";
  private static final String TAG = "RedePaymentTech";
  private static final String LICENCE_ERROR = "LICENCE_ERROR";

  private MethodChannel channel;
  private Context context;
  private Activity activity;
  private RedeTech redeTech;
  private Gson gson;
  private String licenceKey;
  private String licenceInternalKey;
  /** Resposta da operação com tela da Rede em andamento (respondida no onActivityResult). */
  private static ReplyOnce pendingResult;
  private CheckLicenceTech checkLicenceTech;
  private final ExecutorService licenceExecutor = Executors.newSingleThreadExecutor();
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), CHANNEL_NAME);
    context = flutterPluginBinding.getApplicationContext();
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    if (channel != null) {
      channel.setMethodCallHandler(null);
    }
    channel = null;
    redeTech = null;
    checkLicenceTech = null;
    context = null;
    pendingResult = null;
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
    if (METHOD_CONFIGURE_LICENCE.equals(call.method)) {
      handleConfigureLicence(call, result);
      return;
    }

    ensureRedeTech();

    if (redeTech == null) {
      result.error("ACTIVITY_NOT_ATTACHED", "Plugin não está anexado a uma Activity no momento. Tente novamente após o onAttachedToActivity.", null);
      return;
    }

    if (!redeTech.isLicenceConfigured()) {
      result.error("LICENCE_NOT_CONFIGURED", "Configure a licença chamando configureLicence antes de usar o SDK.", null);
      return;
    }

    // Gate por operação: toda chamada de negócio (pagamento, estorno, reimpressão, autenticação,
    // dados do POS) só chega ao SDK da Rede com licença válida (cache local válido ou servidor).
    withValidLicence(result, () -> {
      ensureRedeTech();
      if (redeTech == null) {
        result.error("ACTIVITY_NOT_ATTACHED", "Plugin não está anexado a uma Activity no momento. Tente novamente após o onAttachedToActivity.", null);
        return;
      }
      startOperation(call, result);
    });
  }

  /**
   * Garante resposta única: operações síncronas (dados do POS, erros) respondem na hora e mantêm
   * a operação pendente anterior; operações com tela da Rede ficam pendentes até o
   * onActivityResult e, se substituírem outra ainda pendente, a anterior recebe OPERATION_REPLACED.
   */
  private void startOperation(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
    final ReplyOnce reply = new ReplyOnce(result);
    final ReplyOnce previous = pendingResult;
    pendingResult = reply;
    try {
      redeTech.initPayment(call, reply);
    } catch (RuntimeException e) {
      RedeOperationException error = RedeOperationException.from(e);
      reply.error(error.getCode(), error.getMessage(), null);
    }
    if (reply.isReplied()) {
      if (pendingResult == reply) {
        pendingResult = previous;
      }
    } else if (previous != null && previous != reply) {
      previous.error(RedeOperationException.OPERATION_REPLACED,
          "Uma nova operação da Rede foi iniciada antes de esta responder.", null);
    }
  }

  private void handleConfigureLicence(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
    String incomingLicenceKey = call.argument("licenceKey");
    String incomingLicenceInternalKey = call.argument("licenceInternalKey");

    if (incomingLicenceKey == null || incomingLicenceKey.trim().isEmpty()) {
      result.error("INVALID_ARGUMENT", "licenceKey é obrigatório", null);
      return;
    }

    licenceKey = incomingLicenceKey.trim();
    // Opcional: vazio/nulo ou fora do servidor oficial usa a URL padrão do core (LicenceConfig).
    licenceInternalKey = incomingLicenceInternalKey != null ? incomingLicenceInternalKey.trim() : "";

    ensureRedeTech();
    if (redeTech != null) {
      redeTech.updateLicenceKeys(licenceKey, licenceInternalKey);
    }

    withValidLicence(result, () -> result.success(true));
  }

  /**
   * Valida a licença fora da main thread (cache local válido primeiro, depois o servidor) e
   * executa {@code onValid} na main thread. Licença inválida devolve LICENCE_ERROR e a operação
   * não é executada.
   */
  private void withValidLicence(@NonNull MethodChannel.Result result, @NonNull Runnable onValid) {
    final String key = licenceKey;
    final String internalKey = licenceInternalKey;
    final CheckLicenceTech licenceTech = getCheckLicenceTech();
    if (key == null || key.isEmpty() || licenceTech == null) {
      result.error("LICENCE_NOT_CONFIGURED", "Configure a licença chamando configureLicence antes de usar o SDK.", null);
      return;
    }

    licenceExecutor.execute(() -> {
      LicenceResult licenceResult;
      try {
        licenceResult = licenceTech.validateLicence(key, internalKey);
      } catch (Exception e) {
        licenceResult = null;
        Log.e(TAG, "Erro ao validar licença: " + e.getMessage());
      }

      final boolean valid = licenceResult != null && licenceResult.getResult() == LicenceRules.RESULT_SUCCESS;
      final String message = licenceResult != null && licenceResult.getErrorMessage() != null
          ? licenceResult.getErrorMessage()
          : "Erro ao validar licença.";
      mainHandler.post(() -> {
        if (valid) {
          onValid.run();
        } else {
          Log.e(TAG, "Licença inválida: " + message);
          result.error(LICENCE_ERROR, "Sua licença não foi ativada. " + message, null);
        }
      });
    });
  }

  private CheckLicenceTech getCheckLicenceTech() {
    if (checkLicenceTech == null && context != null) {
      checkLicenceTech = new CheckLicenceTech(context.getApplicationContext());
    }
    return checkLicenceTech;
  }

  private void ensureRedeTech() {
    if (redeTech == null && context != null && channel != null && activity != null) {
      redeTech = new RedeTech(context, channel, activity);
      if (licenceKey != null && licenceInternalKey != null) {
        redeTech.updateLicenceKeys(licenceKey, licenceInternalKey);
      }
    }
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
    binding.addActivityResultListener(this);
    ensureRedeTech();
    Log.d("RedePaymentTechPlugin", "onAttachedToActivity: ActivityResultListener registrado");
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    this.activity = null;
    this.redeTech = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
    binding.addActivityResultListener(this);
    ensureRedeTech();
  }

  @Override
  public void onDetachedFromActivity() {
    this.activity = null;
    this.redeTech = null;
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    ActivityResultOutcome outcome = ActivityResultOutcome.classify(requestCode, resultCode, data != null);
    if (outcome == ActivityResultOutcome.NOT_HANDLED) {
      return false;
    }

    // Cada operação responde uma única vez: a pendência é consumida aqui.
    ReplyOnce result = pendingResult;
    pendingResult = null;
    if (result == null) {
      return true;
    }

    try {
      RedeResponse redeResponse = new RedeResponse();
      boolean reversal = requestCode == REVERSAL_REQUEST_CODE;
      switch (outcome) {
        case TRANSACTION_RESULT: {
          Payment payment = RedePayments.getPaymentFromIntent(data);
          redeResponse.buildPaymentResponse(reversal ? "Reversal" : "Payment", payment);
          result.success(getGson().toJson(redeResponse));
          break;
        }
        case TRANSACTION_CANCELLED: {
          Payment payment = RedePayments.getPaymentFromIntent(data);
          redeResponse.buildCancelledResponse(reversal ? "Reversal" : "PaymentCancelled", payment);
          result.success(getGson().toJson(redeResponse));
          break;
        }
        case TRANSACTION_CANCELLED_NO_DATA:
          result.success(reversal ? "Operação de reversão cancelada" : "Operação de pagamento cancelada");
          break;
        case TRANSACTION_NO_DATA:
          result.error(RedeOperationException.UNEXPECTED_RESULT,
              "O aplicativo da Rede concluiu a operação sem devolver os dados da transação.", null);
          break;
        case AUTH_OK:
          redeResponse.buildAuthResponse("Ok");
          result.success(getGson().toJson(redeResponse));
          break;
        case AUTH_CANCELLED:
          redeResponse.buildAuthResponse("Canceled");
          result.success(getGson().toJson(redeResponse));
          break;
        case REPRINT_OK:
          redeResponse.buildPrintResponse("Ok");
          result.success(getGson().toJson(redeResponse));
          break;
        case REPRINT_CANCELLED:
          redeResponse.buildPrintResponse("Canceled");
          result.success(getGson().toJson(redeResponse));
          break;
        default:
          result.error(RedeOperationException.UNEXPECTED_RESULT,
              "O aplicativo da Rede devolveu um resultado inesperado (resultCode " + resultCode + ").", null);
          break;
      }
    } catch (RuntimeException e) {
      Log.e(TAG, "Erro ao ler o retorno da Rede: " + e.getMessage());
      RedeOperationException error = new RedeOperationException(RedeOperationException.REDE_ERROR,
          RedeOperationException.withDetail("Erro ao ler o retorno do aplicativo da Rede", e), e);
      result.error(error.getCode(), error.getMessage(), null);
    }
    return true;
  }

  private Gson getGson() {
    if (gson == null) {
      gson = new Gson();
    }
    return gson;
  }
}



