package com.example.freetableapp.data.remote;

public class CreateCommentRequest {
    public String content;
    public String rating;

    public CreateCommentRequest(String content, String rating) {
        this.content = content;
        this.rating = rating;
    }
}

