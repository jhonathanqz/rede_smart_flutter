package br.com.jylabtech.rede_payment_tech.licence;

public class LicenceResult {
    private String method;
    private String errorMessage;
    // Começa como erro: só vira sucesso (0) quando a licença for validada explicitamente.
    private int result = LicenceRules.RESULT_ERROR;
    private String message;
    private CheckLicenceEntity licenceEntity;
    // true quando o servidor respondeu e recusou a licença de forma definitiva (4xx ou 200 com
    // licença inutilizável). Nesse caso o cache é apagado e o uso é bloqueado na hora; falhas de
    // rede, timeout e 5xx ficam false e podem usar o cache offline (até 72h).
    private boolean rejectedByServer;

    public boolean isRejectedByServer() {
        return this.rejectedByServer;
    }

    public void setRejectedByServer(boolean value) {
        this.rejectedByServer = value;
    }

    public void setLicenceEntity(CheckLicenceEntity entity) {
        this.licenceEntity = entity;
    }

    public CheckLicenceEntity getLicenceEntity() {
        return this.licenceEntity;
    }

    public void setMethod(String value) {
        this.method = value;
    }

    public void setResult(int value) {
        this.result = value;
    }

    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }

    public void setMessage(String value) {
        this.message = value;
    }

    public String getMessage() {
        return this.message;
    }

    public String getMethod() {
        return this.method;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public int getResult() {
        return this.result;
    }
}

