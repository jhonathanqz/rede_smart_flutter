package br.com.jylabtech.rede_payment_tech.licence;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

public class LicenceProvider implements LicenceNetwork {
    private static final String TAG = "RedePaymentTech";
    private final Context context;

    public LicenceProvider(Context context) {
        this.context = context;
    }

    /**
     * Consulta a licença no servidor. Executa a requisição de forma síncrona: chame fora da
     * main thread. Qualquer resposta diferente de 200 com licença utilizável vira erro.
     */
    @Override
    public LicenceResult checkLicence(final String urlString, final String licenceKey) {
        LicenceResult licenceResult = new LicenceResult();
        licenceResult.setMethod("licence");
        HttpURLConnection connection = null;
        try {
            String deviceId = getAndroidId();
            String deviceUid = getSerialNumber();
            String deviceLocalId = getDeviceUUID();
            String deviceName = Build.MODEL;

            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(LicenceConfig.CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(LicenceConfig.READ_TIMEOUT_MS);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            if (licenceKey != null && !licenceKey.isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer licence:" + licenceKey);
            }

            connection.setDoOutput(true);

            JSONObject body = new JSONObject();
            body.put("deviceId", deviceId);
            body.put("deviceUid", deviceUid);
            body.put("deviceLocalId", deviceLocalId);
            body.put("deviceName", deviceName);

            OutputStream os = connection.getOutputStream();
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int responseCode = connection.getResponseCode();
            String responseBody;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                responseBody = readStream(connection.getInputStream());
            } else {
                responseBody = readStream(connection.getErrorStream());
            }

            LicenceResult decided = LicenceRules.decideHttpResult(responseCode, responseBody, new Date());
            if (decided.getResult() != LicenceRules.RESULT_SUCCESS) {
                Log.d(TAG, "Licença recusada (HTTP " + responseCode + "): " + decided.getErrorMessage());
            }
            return decided;
        } catch (SocketTimeoutException e) {
            licenceResult.setResult(LicenceRules.RESULT_ERROR);
            licenceResult.setErrorMessage("Tempo esgotado ao validar licença. Verifique a conexão com a internet.");
            return licenceResult;
        } catch (Exception e) {
            licenceResult.setResult(LicenceRules.RESULT_ERROR);
            String message = e.getMessage();
            licenceResult.setErrorMessage("Erro de conexão ao validar licença: "
                    + (message != null ? message : e.getClass().getSimpleName()));
            return licenceResult;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readStream(InputStream stream) {
        if (stream == null) {
            return null;
        }
        StringBuilder responseStr = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseStr.append(line);
            }
        } catch (Exception e) {
            return responseStr.length() > 0 ? responseStr.toString() : null;
        }
        return responseStr.toString();
    }

    @SuppressLint("HardwareIds")
    private String getAndroidId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private String getSerialNumber() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Build.getSerial();
            } else {
                return Build.SERIAL != null ? Build.SERIAL : "UNKNOWN_SERIAL";
            }
        } catch (SecurityException e) {
            return "UNKNOWN_SERIAL";
        } catch (Exception e) {
            return "UNKNOWN_SERIAL";
        }
    }

    private String getDeviceUUID() {
        return UUID.randomUUID().toString();
    }
}

