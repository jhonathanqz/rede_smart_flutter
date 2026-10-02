package br.com.jylabtech.rede_payment_tech.licence;

import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CheckLicenceEntity {
    private String id;
    private String uid;
    private String deviceUid;
    private String deviceSerialNumber;
    private String userUid;
    private String productId;
    private String productName;
    private String productDescription;
    private String integrationLibId;
    private String integrationLibName;
    private String checkoutSessionId;
    private String licenceKey;
    private String licenceEncrypt;
    private Date expiredAt;
    private boolean isExpired;
    private boolean isInactive;
    private boolean isCanceled;
    private boolean isTrial;
    private String subscriptionId;
    private String checkoutType;
    private Date createdAt;
    private Date updatedAt;
    private Date savedLocalDatabaseAt;
    private String databaseVersion;
    private String licenceInternalKey;

    public void setLicenceInternalKey(String licenceInternalKey) {
        this.licenceInternalKey = licenceInternalKey;
    }

    public String getLicenceInternalKey() {
        return licenceInternalKey;
    }

    public CheckLicenceEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getDeviceUid() { return deviceUid; }
    public void setDeviceUid(String deviceUid) { this.deviceUid = deviceUid; }

    public String getDeviceSerialNumber() { return deviceSerialNumber; }
    public void setDeviceSerialNumber(String deviceSerialNumber) { this.deviceSerialNumber = deviceSerialNumber; }

    public String getUserUid() { return userUid; }
    public void setUserUid(String userUid) { this.userUid = userUid; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }

    public String getIntegrationLibId() { return integrationLibId; }
    public void setIntegrationLibId(String integrationLibId) { this.integrationLibId = integrationLibId; }

    public String getIntegrationLibName() { return integrationLibName; }
    public void setIntegrationLibName(String integrationLibName) { this.integrationLibName = integrationLibName; }

    public String getCheckoutSessionId() { return checkoutSessionId; }
    public void setCheckoutSessionId(String checkoutSessionId) { this.checkoutSessionId = checkoutSessionId; }

    public String getLicenceKey() { return licenceKey; }
    public void setLicenceKey(String licenceKey) { this.licenceKey = licenceKey; }

    public String getLicenceEncrypt() { return licenceEncrypt; }
    public void setLicenceEncrypt(String licenceEncrypt) { this.licenceEncrypt = licenceEncrypt; }

    public Date getExpiredAt() { return expiredAt; }
    public void setExpiredAt(Date expiredAt) { this.expiredAt = expiredAt; }

    public boolean isExpired() { return isExpired; }
    public void setExpired(boolean expired) { isExpired = expired; }

    public boolean isInactive() { return isInactive; }
    public void setInactive(boolean inactive) { isInactive = inactive; }

    public boolean isCanceled() { return isCanceled; }
    public void setCanceled(boolean canceled) { isCanceled = canceled; }

    public boolean isTrial() { return isTrial; }
    public void setTrial(boolean trial) { isTrial = trial; }

    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getCheckoutType() { return checkoutType; }
    public void setCheckoutType(String checkoutType) { this.checkoutType = checkoutType; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Date getSavedLocalDatabaseAt() { return savedLocalDatabaseAt; }
    public void setSavedLocalDatabaseAt(Date savedLocalDatabaseAt) { this.savedLocalDatabaseAt = savedLocalDatabaseAt; }

    public String getDatabaseVersion() { return databaseVersion; }
    public void setDatabaseVersion(String databaseVersion) { this.databaseVersion = databaseVersion; }

    /** Datas do servidor (expiredAt, createdAt, updatedAt): horário de Brasília, sem fuso. */
    private static SimpleDateFormat serverDateFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
        return sdf;
    }

    /** savedLocalDatabaseAt: gravada pelo próprio aparelho, no fuso local (formato mantido). */
    private static SimpleDateFormat localDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    }

    private static String optNullableString(JSONObject json, String key) {
        if (!json.has(key) || json.isNull(key)) {
            return "";
        }
        return json.optString(key, "");
    }

    private static Date parseDate(SimpleDateFormat sdf, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            // aceita também frações de segundo / sufixos (ex.: "2099-12-31T23:59:59.000")
            String trimmed = value.length() > 19 ? value.substring(0, 19) : value;
            return sdf.parse(trimmed);
        } catch (Exception e) {
            return null;
        }
    }

    public static CheckLicenceEntity fromJson(String jsonStr) {
        if (jsonStr == null) {
            return null;
        }
        try {
            JSONObject json = new JSONObject(jsonStr);
            SimpleDateFormat serverSdf = serverDateFormat();
            SimpleDateFormat localSdf = localDateFormat();

            CheckLicenceEntity entity = new CheckLicenceEntity();
            entity.setId(optNullableString(json, "id"));
            entity.setUid(optNullableString(json, "uid"));
            entity.setDeviceUid(optNullableString(json, "deviceUid"));
            entity.setDeviceSerialNumber(optNullableString(json, "deviceSerialNumber"));
            entity.setUserUid(optNullableString(json, "userUid"));
            entity.setProductId(optNullableString(json, "productId"));
            entity.setProductName(optNullableString(json, "productName"));
            entity.setProductDescription(optNullableString(json, "productDescription"));
            entity.setIntegrationLibId(optNullableString(json, "integrationLibId"));
            entity.setIntegrationLibName(optNullableString(json, "integrationLibName"));
            entity.setCheckoutSessionId(optNullableString(json, "checkoutSessionId"));
            entity.setLicenceKey(optNullableString(json, "licenceKey"));
            entity.setLicenceEncrypt(optNullableString(json, "licenceEncrypt"));
            entity.setExpired(json.optBoolean("isExpired"));
            entity.setInactive(json.optBoolean("isInactive"));
            entity.setCanceled(json.optBoolean("isCanceled"));
            entity.setTrial(json.optBoolean("isTrial"));
            entity.setSubscriptionId(optNullableString(json, "subscriptionId"));
            entity.setCheckoutType(optNullableString(json, "checkoutType"));
            entity.setDatabaseVersion(optNullableString(json, "databaseVersion"));
            entity.setLicenceInternalKey(optNullableString(json, "licenceInternalKey"));

            entity.setExpiredAt(parseDate(serverSdf, optNullableString(json, "expiredAt")));
            entity.setCreatedAt(parseDate(serverSdf, optNullableString(json, "createdAt")));
            entity.setUpdatedAt(parseDate(serverSdf, optNullableString(json, "updatedAt")));
            entity.setSavedLocalDatabaseAt(parseDate(localSdf, optNullableString(json, "savedLocalDatabaseAt")));

            return entity;
        } catch (Exception e) {
            return null;
        }
    }

    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("uid", uid);
            json.put("deviceUid", deviceUid);
            json.put("deviceSerialNumber", deviceSerialNumber);
            json.put("userUid", userUid);
            json.put("productId", productId);
            json.put("productName", productName);
            json.put("productDescription", productDescription);
            json.put("integrationLibId", integrationLibId);
            json.put("integrationLibName", integrationLibName);
            json.put("checkoutSessionId", checkoutSessionId);
            json.put("licenceKey", licenceKey);
            json.put("licenceEncrypt", licenceEncrypt);
            json.put("isExpired", isExpired);
            json.put("isInactive", isInactive);
            json.put("isCanceled", isCanceled);
            json.put("isTrial", isTrial);
            json.put("subscriptionId", subscriptionId);
            json.put("checkoutType", checkoutType);
            json.put("databaseVersion", databaseVersion);
            json.put("licenceInternalKey", licenceInternalKey);

            SimpleDateFormat serverSdf = serverDateFormat();
            SimpleDateFormat localSdf = localDateFormat();
            if (expiredAt != null) json.put("expiredAt", serverSdf.format(expiredAt));
            if (createdAt != null) json.put("createdAt", serverSdf.format(createdAt));
            if (updatedAt != null) json.put("updatedAt", serverSdf.format(updatedAt));
            if (savedLocalDatabaseAt != null) json.put("savedLocalDatabaseAt", localSdf.format(savedLocalDatabaseAt));

            return json.toString();
        } catch (Exception e) {
            return null;
        }
    }
}

