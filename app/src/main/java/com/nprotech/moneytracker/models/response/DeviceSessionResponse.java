package com.nprotech.moneytracker.models.response;

import com.google.gson.annotations.SerializedName;
import com.nprotech.moneytracker.models.DeviceSessionModel;

import java.util.Collections;
import java.util.List;

public class DeviceSessionResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Data data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<DeviceSessionModel> getDevices() {
        return data != null && data.devices != null ? data.devices : Collections.emptyList();
    }

    public static class Data {
        @SerializedName("devices")
        private List<DeviceSessionModel> devices;
    }
}