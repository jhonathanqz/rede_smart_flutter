package br.com.jylabtech.rede_payment_tech.presentation;

import android.content.Context;

import br.com.jylabtech.rede_payment_tech.usecase.TerminalUsecase;

import rede.smartrede.sdk.api.IRedeSdk;

public class TerminalPresenter {
    private TerminalUsecase terminalUsecase;

    public TerminalPresenter(IRedeSdk sdk) {
        this.terminalUsecase = new TerminalUsecase(sdk);
    }


    public String getSimCardSerialNumber() {
        return terminalUsecase.getSimCardSerialNumber();
    }

    public String getIMEI() {
        return terminalUsecase.getIMEI();
    }

    public String getSerialNumber() {
        return terminalUsecase.getSerialNumber();
    }
}
