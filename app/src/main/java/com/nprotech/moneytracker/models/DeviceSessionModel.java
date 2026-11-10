package com.nprotech.moneytracker.models;

import com.google.gson.annotations.SerializedName;

public class DeviceSessionModel {

    @SerializedName("id")
    private long id;

    @SerializedName("device_name")
    private String deviceName;

    @SerializedName("device_type")
    private String deviceType;

    @SerializedName("device_model")
    private String deviceModel;

    @SerializedName("platform")
    private String platformVersion;

    @SerializedName("platform_version")
    private String appVersion;

    @SerializedName("last_used_at")
    private String lastActiveAt;

    @SerializedName("is_current")
    private boolean current;

    public long getId() {
        return id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getLastActiveAt() {
        return lastActiveAt;
    }

    public boolean isCurrent() {
        return current;
    }
}