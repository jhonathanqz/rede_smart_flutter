package br.com.jylabtech.rede_payment_tech.licence;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/** Cache da licença em EncryptedSharedPreferences (nomes e formato mantidos das versões anteriores). */
public class EncryptedLicenceStorage implements LicenceStorage {
    private static final String PREFS_NAME = "rede_licence_prefs_encrypted";
    private static final String KEY_JSON = "licence_entity";
    private static final String TAG = "CheckLicenceTech";

    private final Context context;

    public EncryptedLicenceStorage(Context context) {
        this.context = context;
    }

    private SharedPreferences prefs() throws Exception {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    @Override
    public CheckLicenceEntity load() {
        try {
            String savedJson = prefs().getString(KEY_JSON, null);
            if (savedJson != null) {
                return CheckLicenceEntity.fromJson(savedJson);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting licence: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void save(CheckLicenceEntity entity) {
        try {
            String json = entity.toJson();
            if (json != null) {
                prefs().edit().putString(KEY_JSON, json).apply();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving licence: " + e.getMessage());
        }
    }

    @Override
    public void delete() {
        try {
            prefs().edit().remove(KEY_JSON).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error deleting licence: " + e.getMessage());
        }
    }
}
