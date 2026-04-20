package com.example.freetableapp.data.remote;

import com.example.freetableapp.data.model.Category;
import com.example.freetableapp.data.model.Comment;
import com.example.freetableapp.data.model.Reservation;
import com.example.freetableapp.data.model.RestaurantMedia;
import com.example.freetableapp.data.model.Restaurant;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/logout")
    Call<MessageResponse> logout();

    @GET("auth/me")
    Call<MeResponse> me();

    @GET("categories")
    Call<ApiResponse<java.util.List<Category>>> getCategories();

    @GET("restaurants")
    Call<PaginatedResponse<Restaurant>> getRestaurants(
            @Query("search") String search,
            @Query("category_id") Integer categoryId,
            @Query("per_page") Integer perPage
    );

    @GET("restaurants/{restaurant}")
    Call<ApiResponse<Restaurant>> getRestaurant(@Path("restaurant") int restaurantId);

    @GET("restaurants/{restaurant}/media")
    Call<ApiResponse<RestaurantMedia>> getRestaurantMedia(@Path("restaurant") int restaurantId);

    @POST("restaurants/{restaurant}/reservations")
    Call<ApiResponse<Reservation>> createReservation(
            @Path("restaurant") int restaurantId,
            @Body CreateReservationRequest request
    );

    @GET("restaurants/nearby")
    Call<PaginatedResponse<Restaurant>> getNearbyRestaurants(
            @Query("lat") double lat,
            @Query("lng") double lng,
            @Query("radius") int radius
    );

    @GET("restaurants/{restaurant}/availability")
    Call<List<String>> getAvailability(
            @Path("restaurant") int restaurantId,
            @Query("date") String date,
            @Query("people") int people
    );

    @GET("reservations/me")
    Call<PaginatedResponse<Reservation>> getMyReservations(@Query("per_page") Integer perPage);

    @DELETE("reservations/{reservation}")
    Call<ApiResponse<Reservation>> cancelReservation(@Path("reservation") int reservationId);

    @GET("restaurants/{restaurant}/comments")
    Call<PaginatedResponse<Comment>> getComments(
            @Path("restaurant") int restaurantId,
            @Query("per_page") Integer perPage
    );

    @POST("restaurants/{restaurant}/comments")
    Call<ApiResponse<Comment>> createComment(
            @Path("restaurant") int restaurantId,
            @Body CreateCommentRequest request
    );
}

