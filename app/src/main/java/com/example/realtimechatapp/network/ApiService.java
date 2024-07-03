package com.example.realtimechatapp.network;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @POST("v1/projects/chat-app-d6748/messages:send")
    Call<ResponseBody> sendMessage(
            @Header("Authorization") String authorizationHeader,
            @Header("Content-Type") String contentType,
            @Body String messageBody
    );
}

