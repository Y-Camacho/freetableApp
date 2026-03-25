package com.example.freetableapp.data.model;

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
}

