package com.example.freetableapp.data.repository;

import android.content.Context;

import com.example.freetableapp.data.model.Category;
import com.example.freetableapp.data.model.Comment;
import com.example.freetableapp.data.model.Restaurant;
import com.example.freetableapp.data.remote.ApiClient;
import com.example.freetableapp.data.remote.ApiResponse;
import com.example.freetableapp.data.remote.ApiService;
import com.example.freetableapp.data.remote.PaginatedResponse;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantRepository {
    private final ApiService apiService;

    public RestaurantRepository(Context context) {
        this.apiService = ApiClient.getService(context);
    }

    public void getCategories(DataCallback<List<Category>> callback) {
        apiService.getCategories().enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call, Response<ApiResponse<List<Category>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onSuccess(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
                callback.onError("Error de conexion");
            }
        });
    }

    public void getRestaurants(String search, Integer categoryId, int perPage, DataCallback<List<Restaurant>> callback) {
        apiService.getRestaurants(search, categoryId, perPage).enqueue(new Callback<PaginatedResponse<Restaurant>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<Restaurant>> call, Response<PaginatedResponse<Restaurant>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onSuccess(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<Restaurant>> call, Throwable t) {
                callback.onError("Error de conexion");
            }
        });
    }

    public void getRestaurantById(int restaurantId, DataCallback<Restaurant> callback) {
        apiService.getRestaurant(restaurantId).enqueue(new Callback<ApiResponse<Restaurant>>() {
            @Override
            public void onResponse(Call<ApiResponse<Restaurant>> call, Response<ApiResponse<Restaurant>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onError("No se encontro restaurante");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Restaurant>> call, Throwable t) {
                callback.onError("Error de conexion");
            }
        });
    }

    public void getComments(int restaurantId, DataCallback<List<Comment>> callback) {
        apiService.getComments(restaurantId, 10).enqueue(new Callback<PaginatedResponse<Comment>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<Comment>> call, Response<PaginatedResponse<Comment>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onSuccess(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<Comment>> call, Throwable t) {
                callback.onError("Error de conexion");
            }
        });
    }
}

