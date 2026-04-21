package com.example.freetableapp.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Restaurant {
    public int id;
    public String name;
    public String description;
    public String address;
    public String phone;
    public int manager_id;
    public String created_at;
    public User manager;
    public List<Category> categories;
    public RestaurantImage cover_image;
    public List<RestaurantImage> images;
    public List<RestaurantMenu> menus;
    public Double average_rating;
    public Integer ratings_count;
    @SerializedName(value = "latitude", alternate = {"lat"})
    public Double latitude;
    @SerializedName(value = "longitude", alternate = {"lng", "lon"})
    public Double longitude;
}

