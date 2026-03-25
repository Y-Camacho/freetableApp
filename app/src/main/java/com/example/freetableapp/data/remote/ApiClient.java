package com.example.freetableapp.data.remote;

import android.content.Context;

import com.example.freetableapp.data.local.SessionManager;
import com.google.gson.Gson;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Retrofit requires the base URL to end with '/'.
    private static final String BASE_URL = "http://10.0.2.2:8081/laravel-freetable-api/public/api/";

    private static ApiService apiService;

    public static ApiService getService(Context context) {
        if (apiService == null) {
            SessionManager sessionManager = new SessionManager(context.getApplicationContext());

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                Request.Builder builder = original.newBuilder();

                String token = sessionManager.getToken();
                if (token != null && !token.isEmpty()) {
                    builder.header("Authorization", "Bearer " + token);
                }

                return chain.proceed(builder.build());
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(logging)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(new Gson()))
                    .client(client)
                    .build();

            apiService = retrofit.create(ApiService.class);
        }

        return apiService;
    }
}

