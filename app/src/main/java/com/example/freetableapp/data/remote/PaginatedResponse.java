package com.example.freetableapp.data.remote;

import java.util.List;

public class PaginatedResponse<T> {
    public List<T> data;
    public Meta meta;

    public static class Meta {
        public Double average_rating;
        public Integer ratings_count;
    }
}

