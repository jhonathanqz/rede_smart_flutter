package com.example.rede_smart_flutter.usecase;

import static com.example.rede_smart_flutter.constants.RedeConstants.AUTH_REQUEST_CODE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import rede.smartrede.sdk.AuthenticationType;
import rede.smartrede.sdk.AuthenticatorIntentBuilder;
import rede.smartrede.sdk.RedeAuthentication;
import rede.smartrede.sdk.api.IRedeSdk;

public class AuthUsecase {
    private RedeAuthentication authentication;
    private Activity mActivity;


    public AuthUsecase(IRedeSdk sdk, Context context, Activity activity) {
        this.mActivity = activity;
       this.authentication =  sdk.getRedeAuthentication(context);
    }

    public void authWithChangePin(String description, int amount) {
        try {
            AuthenticatorIntentBuilder value =  this.authentication.intentForAuthenticationBuilder(AuthenticationType.OPERATION_WITH_CHANGE_PIN, description, amount);
            Intent i = value.build();
            mActivity.startActivityForResult(i, AUTH_REQUEST_CODE);
        }catch (Exception e) {
            throw e;
        }
    }

    public void authWithOnlyPin(String description, int amount) {
        try {
            AuthenticatorIntentBuilder value =  this.authentication.intentForAuthenticationBuilder(AuthenticationType.OPERATION_WITH_ONLY_PIN, description, amount);
            Intent i = value.build();
            mActivity.startActivityForResult(i, AUTH_REQUEST_CODE);
        }catch (Exception e) {
            throw e;
        }
    }

    public void authWithValueAndPin(String description, int amount) {
        try {
            AuthenticatorIntentBuilder value =  this.authentication.intentForAuthenticationBuilder(AuthenticationType.OPERATION_WITH_VALUE_AND_PIN, description, amount);
            Intent i = value.build();
            mActivity.startActivityForResult(i, AUTH_REQUEST_CODE);
        }catch (Exception e) {
            throw e;
        }
    }
}
