package br.com.jylabtech.rede_payment_tech.usecase;

import static br.com.jylabtech.rede_payment_tech.constants.RedeConstants.AUTH_REQUEST_CODE;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import rede.smartrede.sdk.AuthenticationType;
import rede.smartrede.sdk.AuthenticatorIntentBuilder;
import rede.smartrede.sdk.RedeAuthentication;
import rede.smartrede.sdk.RedeAuthenticationValidationException;
import rede.smartrede.sdk.api.IRedeSdk;

import br.com.jylabtech.rede_payment_tech.core.RedeOperationException;

public class AuthUsecase {
    private RedeAuthentication authentication;
    private Activity mActivity;


    public AuthUsecase(IRedeSdk sdk, Context context, Activity activity) {
        this.mActivity = activity;
       this.authentication =  sdk.getRedeAuthentication(context);
    }

    public void authWithChangePin(String description, long amount) {
        start(AuthenticationType.OPERATION_WITH_CHANGE_PIN, description, amount);
    }

    public void authWithOnlyPin(String description, long amount) {
        start(AuthenticationType.OPERATION_WITH_ONLY_PIN, description, amount);
    }

    public void authWithValueAndPin(String description, long amount) {
        start(AuthenticationType.OPERATION_WITH_VALUE_AND_PIN, description, amount);
    }

    /**
     * Abre a autenticação da Rede. Falhas viram {@link RedeOperationException} (antes a exceção
     * do SDK subia sem tratamento).
     */
    private void start(AuthenticationType type, String description, long amount) {
        if (mActivity == null) {
            throw new RedeOperationException(RedeOperationException.ACTIVITY_NOT_ATTACHED,
                    "Plugin não está anexado a uma Activity no momento.");
        }
        try {
            AuthenticatorIntentBuilder value = this.authentication.intentForAuthenticationBuilder(type, description, amount);
            Intent i = value.build();
            mActivity.startActivityForResult(i, AUTH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            throw new RedeOperationException(RedeOperationException.REDE_APP_NOT_FOUND,
                    "Aplicativo de autenticação da Rede não encontrado no terminal.", e);
        } catch (RedeAuthenticationValidationException e) {
            String detail = e.getMessage() != null ? e.getMessage() : String.valueOf(e.getValidationError());
            throw new RedeOperationException(RedeOperationException.INVALID_AUTH,
                    "Dados da autenticação recusados pelo SDK da Rede: " + detail, e);
        }
    }
}
