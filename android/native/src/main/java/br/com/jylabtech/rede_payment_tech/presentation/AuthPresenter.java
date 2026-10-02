package br.com.jylabtech.rede_payment_tech.presentation;

import android.app.Activity;
import android.content.Context;

import br.com.jylabtech.rede_payment_tech.usecase.AuthUsecase;

import rede.smartrede.sdk.api.IRedeSdk;

public class AuthPresenter {
    private AuthUsecase authUsecase;

    public AuthPresenter(Context context, Activity activity, IRedeSdk sdk) {
        this.authUsecase = new AuthUsecase(sdk, context, activity);
    }

    public void authWithChangePin(String description, long amount) {
        this.authUsecase.authWithChangePin(description, amount);
    }

    public void authWithOnlyPin(String description, long amount) {
        this.authUsecase.authWithOnlyPin(description, amount);
    }

    public void authWithValueAndPin(String description, long amount) {
        this.authUsecase.authWithValueAndPin(description, amount);
    }

    }
