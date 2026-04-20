package com.example.freetableapp.data.repository;

import android.content.Context;

import com.example.freetableapp.data.model.Category;
import com.example.freetableapp.data.model.Comment;
import com.example.freetableapp.data.model.CommentPage;
import com.example.freetableapp.data.model.RestaurantMedia;
import com.example.freetableapp.data.model.Restaurant;
import com.example.freetableapp.data.remote.ApiClient;
import com.example.freetableapp.data.remote.ApiResponse;
import com.example.freetableapp.data.remote.ApiService;
import com.example.freetableapp.data.remote.CreateCommentRequest;
import com.example.freetableapp.data.remote.ErrorResponse;
import com.example.freetableapp.data.remote.PaginatedResponse;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantRepository {
    private final ApiService apiService;
    private final Gson gson = new Gson();

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

    public void getRestaurantMedia(int restaurantId, DataCallback<RestaurantMedia> callback) {
        apiService.getRestaurantMedia(restaurantId).enqueue(new Callback<ApiResponse<RestaurantMedia>>() {
            @Override
            public void onResponse(Call<ApiResponse<RestaurantMedia>> call, Response<ApiResponse<RestaurantMedia>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onError("No se pudo cargar la galeria");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<RestaurantMedia>> call, Throwable t) {
                callback.onError("Error de conexion");
            }
        });
    }

    public void getComments(int restaurantId, DataCallback<CommentPage> callback) {
        apiService.getComments(restaurantId, 10).enqueue(new Callback<PaginatedResponse<Comment>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<Comment>> call, Response<PaginatedResponse<Comment>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    PaginatedResponse<Comment> body = response.body();
                    int ratingsCount = body.meta != null && body.meta.ratings_count != null
                            ? body.meta.ratings_count
                            : body.data.size();
                    double averageRating;
                    if (body.meta != null && body.meta.average_rating != null) {
                        averageRating = body.meta.average_rating;
                    } else {
                        averageRating = computeAverageRating(body.data);
                    }
                    callback.onSuccess(new CommentPage(body.data, averageRating, ratingsCount));
                } else {
                    callback.onSuccess(new CommentPage(Collections.emptyList(), 0, 0));
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<Comment>> call, Throwable t) {
                callback.onError("Error de conexion");
            }
        });
    }

    public void createComment(int restaurantId, String content, String rating, DataCallback<Comment> callback) {
        apiService.createComment(restaurantId, new CreateCommentRequest(content, rating)).enqueue(new Callback<ApiResponse<Comment>>() {
            @Override
            public void onResponse(Call<ApiResponse<Comment>> call, Response<ApiResponse<Comment>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onError(extractErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Comment>> call, Throwable t) {
                callback.onError("Error de conexion");
            }
        });
    }

    public void getNearbyRestaurants(double lat, double lng, int radius, DataCallback<List<Restaurant>> callback) {
        apiService.getNearbyRestaurants(lat, lng, radius).enqueue(new Callback<PaginatedResponse<Restaurant>>() {
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

    private double computeAverageRating(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return 0;
        }

        double sum = 0;
        int count = 0;
        for (Comment comment : comments) {
            if (comment == null || comment.rating == null) {
                continue;
            }
            try {
                sum += Double.parseDouble(comment.rating.replace(',', '.'));
                count++;
            } catch (NumberFormatException ignored) {
                // Ignore invalid rating payloads.
            }
        }

        if (count == 0) {
            return 0;
        }
        return Double.parseDouble(String.format(Locale.US, "%.1f", (sum / count)));
    }

    private String extractErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                ErrorResponse errorResponse = gson.fromJson(raw, ErrorResponse.class);
                if (errorResponse != null && errorResponse.message != null && !errorResponse.message.trim().isEmpty()) {
                    return errorResponse.message;
                }
            }
        } catch (Exception ignored) {
            // Fallback message below.
        }
        return "No se pudo enviar el comentario";
    }
}

