package com.example.rede_smart_flutter;

import static com.example.rede_smart_flutter.constants.RedeConstants.AUTH_REQUEST_CODE;
import static com.example.rede_smart_flutter.constants.RedeConstants.PAYMENT_REQUEST_CODE;
import static com.example.rede_smart_flutter.constants.RedeConstants.REPRINT_REQUEST_CODE;
import static com.example.rede_smart_flutter.constants.RedeConstants.REVERSAL_REQUEST_CODE;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.rede_smart_flutter.constants.RedeResponse;
import com.example.rede_smart_flutter.core.RedeSmart;
import com.google.gson.Gson;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.BinaryMessenger;

import rede.smartrede.sdk.Payment;
import rede.smartrede.sdk.PaymentStatus;
import rede.smartrede.sdk.Receipt;
import rede.smartrede.sdk.RedePayments;
import rede.smartrede.sdk.api.IRedeSdk;

/** RedeSmartFlutterPlugin */
public class RedeSmartFlutterPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener  {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Context context;
  private Activity activity;
  public static MethodChannel.Result _result;
  private RedeSmart redeSmart;
  Gson gson = null;

  public void checkRedeSmart() {
    if(context != null  && channel != null && activity != null) {
      if(redeSmart == null) {
        redeSmart = new RedeSmart(context, channel, activity);
      }
    }
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "rede_smart_flutter");
    channel.setMethodCallHandler(this);
    this.context = flutterPluginBinding.getApplicationContext();
    checkRedeSmart();

  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
    binding.addActivityResultListener(this);
    Log.d("RedeSmartFlutterPlugin", "**** onAttachedToActivity: Listener registrado");
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    this.activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
    binding.addActivityResultListener(this);
  }

  @Override
  public void onDetachedFromActivity() {
    this.activity = null;
  }


  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    _result = result;
    checkRedeSmart();
    if(redeSmart != null) {
      redeSmart.initPayment(call, result);
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
    channel = null;
    redeSmart = null;
  }


  @Override
  public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    RedeResponse redeResponse = new RedeResponse();

    if (requestCode == PAYMENT_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        if (data != null) {
          Payment payment = RedePayments.getPaymentFromIntent(data);
          redeResponse.buildPaymentResponse("Payment",payment);
          if (payment.getStatus() == PaymentStatus.AUTHORIZED) {
            Receipt receipt = payment.getReceipt();
            //Toast.makeText(this, “Payment Authorized”, LENGTH_LONG).show();
          } else if (payment.getStatus() == PaymentStatus.FAILED) {
            //Toast.makeText(this, “Payment Failed”, LENGTH_LONG).show();
          } else if (payment.getStatus() == PaymentStatus.DECLINED) {
            //Toast.makeText(this, “Payment Declined”, LENGTH_LONG).show();
          }
          String json = getGson().toJson(redeResponse);
          _result.success(json);
        }
      } else if (resultCode == Activity.RESULT_CANCELED) {
        if (data != null) {
          Payment payment = RedePayments.getPaymentFromIntent(data);
          redeResponse.buildCancelledResponse("PaymentCancelled", payment);
          String json = getGson().toJson(redeResponse);
          _result.success(json);
        }else {
          _result.success("Operação de pagamento cancelada");
        }
      }
    }

    //Operação de reversão
    if (requestCode == REVERSAL_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        if (data != null) {
          Payment payment = RedePayments.getPaymentFromIntent(data);
          redeResponse.buildPaymentResponse("Reversal",payment);
          if (payment.getStatus() == PaymentStatus.AUTHORIZED) {
            //Toast.makeText(this, “Payment Authorized”, LENGTH_LONG).show();
          } else if (payment.getStatus() == PaymentStatus.FAILED) {
            //Toast.makeText(this, “Payment Failed”, LENGTH_LONG).show();
          } else if (payment.getStatus() == PaymentStatus.DECLINED) {
            //Toast.makeText(this, “Payment Declined”, LENGTH_LONG).show();
          }
          String json = getGson().toJson(redeResponse);
          _result.success(json);
        }
      } else if (resultCode == Activity.RESULT_CANCELED) {
        if (data != null) {
          Payment payment = RedePayments.getPaymentFromIntent(data);
          redeResponse.buildCancelledResponse("Reversal", payment);
          String json = getGson().toJson(redeResponse);
          _result.success(json);
        }else {
          _result.success("Operação de reversão cancelada");
        }
      }
    }

    //Operação de autenticação
    if (requestCode == AUTH_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        redeResponse.buildAuthResponse("Ok");
      } else if (resultCode == Activity.RESULT_CANCELED) {
        redeResponse.buildAuthResponse("Canceled");
        //Toast.makeText(this, “Payment Canceled”, Toast.LENGTH_LONG).show();
      }
      String json = getGson().toJson(redeResponse);
      _result.success(json);
    }


    //Operação de reimpressão
    if (requestCode == REPRINT_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
      redeResponse.buildPrintResponse("Ok");
      } else if (resultCode == Activity.RESULT_CANCELED) {
        redeResponse.buildPrintResponse("Canceled");
        //Toast.makeText(this, “Payment Canceled”, Toast.LENGTH_LONG).show();
      }
      String json = getGson().toJson(redeResponse);
      _result.success(json);
    }

    return false;
  }

  private Gson getGson() {
    if (gson == null) {
      gson = new Gson();
    }
    return gson;
  }
}
