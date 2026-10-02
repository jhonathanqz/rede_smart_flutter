package br.com.jylabtech.rede_payment_tech.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

import io.flutter.plugin.common.MethodChannel;

/**
 * {@link MethodChannel.Result} que repassa só a primeira resposta. Responder duas vezes ao mesmo
 * Result derruba o app ("Reply already submitted"); as respostas seguintes são descartadas.
 */
public final class ReplyOnce implements MethodChannel.Result {

    private final MethodChannel.Result delegate;
    private final AtomicBoolean replied = new AtomicBoolean(false);

    public ReplyOnce(@NonNull MethodChannel.Result delegate) {
        this.delegate = delegate;
    }

    public boolean isReplied() {
        return replied.get();
    }

    @Override
    public void success(@Nullable Object result) {
        if (replied.compareAndSet(false, true)) {
            delegate.success(result);
        }
    }

    @Override
    public void error(@NonNull String errorCode, @Nullable String errorMessage, @Nullable Object errorDetails) {
        if (replied.compareAndSet(false, true)) {
            delegate.error(errorCode, errorMessage, errorDetails);
        }
    }

    @Override
    public void notImplemented() {
        if (replied.compareAndSet(false, true)) {
            delegate.notImplemented();
        }
    }
}
