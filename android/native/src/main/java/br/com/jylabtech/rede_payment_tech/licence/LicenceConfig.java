package br.com.jylabtech.rede_payment_tech.licence;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Configuração fixa da validação de licença.
 *
 * A URL de validação é travada no servidor oficial da JY Labtech (jylabtech_core). O
 * {@code licenceInternalKey} informado pelo app só é aceito se apontar para esse mesmo
 * servidor; qualquer outro valor é ignorado e a URL padrão é usada.
 *
 * Classe pura (sem dependências do Android) para permitir testes em JVM.
 */
public final class LicenceConfig {
    public static final String LICENCE_HOST = "licence-server.invalid";
    public static final String LICENCE_PATH_PREFIX = "/pos-payments/licence/check";
    public static final String LICENCE_CHECK_URL = "https://" + LICENCE_HOST + LICENCE_PATH_PREFIX;

    public static final int CONNECT_TIMEOUT_MS = 15000;
    public static final int READ_TIMEOUT_MS = 15000;

    private LicenceConfig() {}

    /**
     * Decodifica o {@code licenceInternalKey} (URL em texto ou em Base64) e devolve a URL
     * efetiva de validação. Só aceita {@code https://licence-server.invalid/pos-payments/licence/check...};
     * em qualquer outro caso devolve {@link #LICENCE_CHECK_URL}.
     */
    public static String resolveLicenceUrl(String licenceInternalKey) {
        if (licenceInternalKey == null) {
            return LICENCE_CHECK_URL;
        }
        String value = licenceInternalKey.trim();
        if (value.isEmpty()) {
            return LICENCE_CHECK_URL;
        }

        String candidate;
        if (value.toLowerCase(Locale.ROOT).contains("http")) {
            candidate = value;
        } else {
            candidate = decodeBase64(value);
            if (candidate == null) {
                return LICENCE_CHECK_URL;
            }
            candidate = candidate.trim();
        }

        return isOfficialUrl(candidate) ? candidate : LICENCE_CHECK_URL;
    }

    /**
     * Indica se o valor (URL em texto ou Base64) aponta para o servidor oficial. Valores vazios,
     * inválidos, a URL antiga do Cloud Run ou outros hosts devolvem false.
     */
    public static boolean isOfficialLicenceInternalKey(String licenceInternalKey) {
        if (licenceInternalKey == null) {
            return false;
        }
        String value = licenceInternalKey.trim();
        if (value.isEmpty()) {
            return false;
        }
        if (value.toLowerCase(Locale.ROOT).contains("http")) {
            return isOfficialUrl(value);
        }
        String decoded = decodeBase64(value);
        return decoded != null && isOfficialUrl(decoded.trim());
    }

    static boolean isOfficialUrl(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        try {
            URI uri = new URI(value);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                return false;
            }
            if (uri.getRawUserInfo() != null) {
                return false;
            }
            String host = uri.getHost();
            if (host == null || !LICENCE_HOST.equals(host.toLowerCase(Locale.ROOT))) {
                return false;
            }
            int port = uri.getPort();
            if (port != -1 && port != 443) {
                return false;
            }
            String path = uri.getPath();
            if (path == null || !path.startsWith(LICENCE_PATH_PREFIX)) {
                return false;
            }
            String rest = path.substring(LICENCE_PATH_PREFIX.length());
            return rest.isEmpty() || rest.startsWith("/");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Decodificador Base64 (padrão e URL-safe) sem dependências do Android e compatível com
     * minSdk 24 (java.util.Base64 só existe a partir da API 26). Devolve null se inválido.
     */
    static String decodeBase64(String input) {
        StringBuilder clean = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!Character.isWhitespace(c)) {
                clean.append(c);
            }
        }
        String s = clean.toString();
        while (s.endsWith("=")) {
            s = s.substring(0, s.length() - 1);
        }
        if (s.isEmpty() || s.length() % 4 == 1) {
            return null;
        }

        byte[] out = new byte[s.length() * 3 / 4];
        int outIndex = 0;
        int buffer = 0;
        int bits = 0;
        for (int i = 0; i < s.length(); i++) {
            int v = base64Value(s.charAt(i));
            if (v < 0) {
                return null;
            }
            buffer = (buffer << 6) | v;
            bits += 6;
            if (bits >= 8) {
                bits -= 8;
                out[outIndex++] = (byte) ((buffer >> bits) & 0xFF);
            }
        }
        return new String(out, 0, outIndex, StandardCharsets.UTF_8);
    }

    private static int base64Value(char c) {
        if (c >= 'A' && c <= 'Z') return c - 'A';
        if (c >= 'a' && c <= 'z') return c - 'a' + 26;
        if (c >= '0' && c <= '9') return c - '0' + 52;
        if (c == '+' || c == '-') return 62;
        if (c == '/' || c == '_') return 63;
        return -1;
    }
}
