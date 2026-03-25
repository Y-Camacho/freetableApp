package com.example.freetableapp.data.repository;

import android.content.Context;

import com.example.freetableapp.data.model.Reservation;
import com.example.freetableapp.data.remote.ApiClient;
import com.example.freetableapp.data.remote.ApiResponse;
import com.example.freetableapp.data.remote.ApiService;
import com.example.freetableapp.data.remote.CreateReservationRequest;
import com.example.freetableapp.data.remote.PaginatedResponse;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationRepository {
    private final ApiService apiService;

    public ReservationRepository(Context context) {
        this.apiService = ApiClient.getService(context);
    }

    public void createReservation(int restaurantId, String reservationTime, int people, DataCallback<Reservation> callback) {
        CreateReservationRequest request = new CreateReservationRequest(reservationTime, people);
        apiService.createReservation(restaurantId, request).enqueue(new Callback<ApiResponse<Reservation>>() {
            @Override
            public void onResponse(Call<ApiResponse<Reservation>> call, Response<ApiResponse<Reservation>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onError("No se pudo crear la reserva");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Reservation>> call, Throwable t) {
                callback.onError("Error de conexion");
            }
        });
    }

    public void getMyReservations(int perPage, DataCallback<List<Reservation>> callback) {
        apiService.getMyReservations(perPage).enqueue(new Callback<PaginatedResponse<Reservation>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<Reservation>> call, Response<PaginatedResponse<Reservation>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onSuccess(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<Reservation>> call, Throwable t) {
                callback.onError("Error de conexion");
            }
        });
    }

    public void cancelReservation(int reservationId, DataCallback<Reservation> callback) {
        apiService.cancelReservation(reservationId).enqueue(new Callback<ApiResponse<Reservation>>() {
            @Override
            public void onResponse(Call<ApiResponse<Reservation>> call, Response<ApiResponse<Reservation>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onError("No se pudo cancelar la reserva");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Reservation>> call, Throwable t) {
                callback.onError("Error de conexion");
            }
        });
    }
}

