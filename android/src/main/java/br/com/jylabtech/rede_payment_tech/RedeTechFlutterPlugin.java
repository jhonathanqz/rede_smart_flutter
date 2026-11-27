package br.com.jylabtech.rede_payment_tech;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

/**
 * Classe ponte que delega a implementação nativa real para {@link RedePaymentTechNativePlugin}.
 * O delegate vem pré-compilado em {@code android/libs/rede_payment_tech-native.jar}, gerado a
 * partir do código-fonte em {@code android/native} por {@code scripts/build_android_aar.sh}.
 */
public final class RedeTechFlutterPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {

  private final RedePaymentTechNativePlugin delegate = new RedePaymentTechNativePlugin();

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    delegate.onAttachedToEngine(binding);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    delegate.onDetachedFromEngine(binding);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
    delegate.onMethodCall(call, result);
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    delegate.onAttachedToActivity(binding);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    delegate.onDetachedFromActivityForConfigChanges();
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    delegate.onReattachedToActivityForConfigChanges(binding);
  }

  @Override
  public void onDetachedFromActivity() {
    delegate.onDetachedFromActivity();
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    return delegate.onActivityResult(requestCode, resultCode, data);
  }
}
