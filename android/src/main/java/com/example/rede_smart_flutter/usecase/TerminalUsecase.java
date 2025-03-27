package com.example.rede_smart_flutter.usecase;

import android.content.Context;

import rede.smartrede.commons.contract.ITerminalFunctions;
import rede.smartrede.commons.exception.InvalidSdkStateException;
import rede.smartrede.sdk.api.IRedeSdk;

public class TerminalUsecase {
    private ITerminalFunctions iTerminalFunctions;

    public TerminalUsecase(IRedeSdk sdk) {
        this.iTerminalFunctions = sdk.getTerminalFunctions();
    }

    public String getSimCardSerialNumber() {
        try {
            String result = iTerminalFunctions.getSimCardSerialNumber();
            return result;
        }catch(InvalidSdkStateException e) {
            throw e;
        }
    }

    public String getIMEI() {
        try {
            String result = iTerminalFunctions.getIMEI();
            return result;
        }catch(InvalidSdkStateException e) {
            throw e;
        }
    }

    public String getSerialNumber() {
        try {
            String result = iTerminalFunctions.getSerialNumber();
            return result;
        }catch(InvalidSdkStateException e) {
            throw e;
        }
    }
}
