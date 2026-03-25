package com.example.freetableapp.data.model;

import java.util.List;

public class CommentPage {
    public List<Comment> comments;
    public double averageRating;
    public int ratingsCount;

    public CommentPage(List<Comment> comments, double averageRating, int ratingsCount) {
        this.comments = comments;
        this.averageRating = averageRating;
        this.ratingsCount = ratingsCount;
    }
}

