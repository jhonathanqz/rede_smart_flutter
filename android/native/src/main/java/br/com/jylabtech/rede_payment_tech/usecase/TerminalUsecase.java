package br.com.jylabtech.rede_payment_tech.usecase;

import android.content.Context;

import rede.smartrede.commons.contract.ITerminalFunctions;
import rede.smartrede.commons.exception.InvalidSdkStateException;
import rede.smartrede.sdk.api.IRedeSdk;

import br.com.jylabtech.rede_payment_tech.core.RedeOperationException;

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
            throw sdkError(e);
        }
    }

    public String getIMEI() {
        try {
            String result = iTerminalFunctions.getIMEI();
            return result;
        }catch(InvalidSdkStateException e) {
            throw sdkError(e);
        }
    }

    public String getSerialNumber() {
        try {
            String result = iTerminalFunctions.getSerialNumber();
            return result;
        }catch(InvalidSdkStateException e) {
            throw sdkError(e);
        }
    }

    private static RedeOperationException sdkError(InvalidSdkStateException e) {
        return new RedeOperationException(RedeOperationException.REDE_SDK_ERROR,
                RedeOperationException.withDetail("SDK da Rede indisponível", e), e);
    }
}
