package br.com.jylabtech.rede_payment_tech;

import static br.com.jylabtech.rede_payment_tech.constants.RedeConstants.REVERSAL_REQUEST_CODE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import br.com.jylabtech.rede_payment_tech.constants.RedeResponse;
import br.com.jylabtech.rede_payment_tech.core.ActivityResultOutcome;
import br.com.jylabtech.rede_payment_tech.core.RedeOperationException;
import br.com.jylabtech.rede_payment_tech.core.RedeTech;
import br.com.jylabtech.rede_payment_tech.core.ReplyOnce;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import rede.smartrede.sdk.Payment;
import rede.smartrede.sdk.RedePayments;

public class RedePaymentTechNativePlugin implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {

  private static final String CHANNEL_NAME = "rede_payment_tech";
  private static final String TAG = "RedePaymentTech";

  private MethodChannel channel;
  private Context context;
  private Activity activity;
  private RedeTech redeTech;
  private Gson gson;
  /** Resposta da operação com tela da Rede em andamento (respondida no onActivityResult). */
  private static ReplyOnce pendingResult;

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
    context = null;
    pendingResult = null;
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
    ensureRedeTech();

    if (redeTech == null) {
      result.error("ACTIVITY_NOT_ATTACHED", "Plugin não está anexado a uma Activity no momento. Tente novamente após o onAttachedToActivity.", null);
      return;
    }

    startOperation(call, result);
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

  private void ensureRedeTech() {
    if (redeTech == null && context != null && channel != null && activity != null) {
      redeTech = new RedeTech(context, channel, activity);
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

    boolean reversal = requestCode == REVERSAL_REQUEST_CODE;
    if (outcome == ActivityResultOutcome.TRANSACTION_CANCELLED_NO_DATA) {
      // Erro estruturado em vez de texto puro (que quebrava o fromJson no Dart).
      result.error(RedeOperationException.CANCELED,
          reversal ? "Operação de reversão cancelada" : "Operação de pagamento cancelada", null);
      return true;
    }

    try {
      RedeResponse redeResponse = new RedeResponse();
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



