package com.hf.webflux.hfai.aiservice.deepseek;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DeepSeekClient {
    private static final String BASE_URL = "https://api.deepseek.com/v1";
    private final OkHttpClient httpClient;
    private final String apiKey;

    public DeepSeekClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient();
    }

    public Response sendRequest(String endpoint, String jsonBody) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(jsonBody, okhttp3.MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseString = response.body().string();
            Gson gson = new Gson();
            return gson.fromJson(responseString, Response.class);
        }
    }

    public Response exampleMethod(String param1, String param2) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("param1", param1);
        params.put("param2", param2);
        Gson gson = new Gson();
        String jsonBody = gson.toJson(params);
        return sendRequest("/example-endpoint", jsonBody);
    }
}
