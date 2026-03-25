package com.example.freetableapp.data.repository;

import android.content.Context;

import com.example.freetableapp.data.local.SessionManager;
import com.example.freetableapp.data.model.User;
import com.example.freetableapp.data.remote.ApiClient;
import com.example.freetableapp.data.remote.ApiService;
import com.example.freetableapp.data.remote.AuthResponse;
import com.example.freetableapp.data.remote.LoginRequest;
import com.example.freetableapp.data.remote.MeResponse;
import com.example.freetableapp.data.remote.MessageResponse;
import com.example.freetableapp.data.remote.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private final ApiService apiService;
    private final SessionManager sessionManager;

    public AuthRepository(Context context) {
        this.apiService = ApiClient.getService(context);
        this.sessionManager = new SessionManager(context);
    }

    public void login(String email, String password, DataCallback<User> callback) {
        apiService.login(new LoginRequest(email, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().user != null) {
                    sessionManager.saveSession(response.body().token, response.body().user);
                    callback.onSuccess(response.body().user);
                } else {
                    callback.onError("Credenciales invalidas");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Error de conexion");
            }
        });
    }

    public void register(String name, String email, String password, DataCallback<User> callback) {
        apiService.register(new RegisterRequest(name, email, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().user != null) {
                    sessionManager.saveSession(response.body().token, response.body().user);
                    callback.onSuccess(response.body().user);
                } else {
                    callback.onError("No se pudo registrar");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Error de conexion");
            }
        });
    }

    public void me(DataCallback<User> callback) {
        apiService.me().enqueue(new Callback<MeResponse>() {
            @Override
            public void onResponse(Call<MeResponse> call, Response<MeResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().user != null) {
                    sessionManager.saveSession(sessionManager.getToken(), response.body().user);
                    callback.onSuccess(response.body().user);
                } else {
                    callback.onError("Sesion invalida");
                }
            }

            @Override
            public void onFailure(Call<MeResponse> call, Throwable t) {
                callback.onError("Error de conexion");
            }
        });
    }

    public void logout(DataCallback<String> callback) {
        apiService.logout().enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                sessionManager.clearSession();
                callback.onSuccess("Logout correcto");
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                sessionManager.clearSession();
                callback.onSuccess("Logout local");
            }
        });
    }

    public SessionManager sessionManager() {
        return sessionManager;
    }
}

