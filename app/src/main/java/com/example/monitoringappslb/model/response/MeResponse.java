package com.example.monitoringappslb.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Response untuk GET /api/auth/me
 * Backend kirim: { success: true, data: { id, nama, email, role, ... } }
 */
public class MeResponse {

    @SerializedName("success") private boolean success;
    @SerializedName("message") private String message;
    @SerializedName("data")    private UserResponse data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public UserResponse getData() { return data; }
}
