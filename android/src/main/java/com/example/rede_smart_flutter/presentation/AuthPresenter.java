package com.example.rede_smart_flutter.presentation;

import android.app.Activity;
import android.content.Context;

import com.example.rede_smart_flutter.usecase.AuthUsecase;

import rede.smartrede.sdk.api.IRedeSdk;

public class AuthPresenter {
    private AuthUsecase authUsecase;

    public AuthPresenter(Context context, Activity activity, IRedeSdk sdk) {
        this.authUsecase = new AuthUsecase(sdk, context, activity);
    }

    public void authWithChangePin(String description, int amount) {
        this.authUsecase.authWithChangePin(description, amount);
    }

    public void authWithOnlyPin(String description, int amount) {
        this.authUsecase.authWithOnlyPin(description, amount);
    }

    public void authWithValueAndPin(String description, int amount) {
        this.authUsecase.authWithValueAndPin(description, amount);
    }

    }
