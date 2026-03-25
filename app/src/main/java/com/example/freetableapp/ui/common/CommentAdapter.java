package com.example.freetableapp.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.freetableapp.R;
import com.example.freetableapp.data.model.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> items = new ArrayList<>();

    public void submitList(List<Comment> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAuthor;
        private final TextView tvRating;
        private final TextView tvContent;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvCommentAuthor);
            tvRating = itemView.findViewById(R.id.tvCommentRating);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
        }

        void bind(Comment comment) {
            tvAuthor.setText(comment != null && comment.user != null ? comment.user.name : "Cliente");
            tvContent.setText(comment != null && comment.content != null ? comment.content : "");

            double ratingValue = 0;
            if (comment != null && comment.rating != null) {
                try {
                    ratingValue = Double.parseDouble(comment.rating);
                } catch (NumberFormatException ignored) {
                    ratingValue = 0;
                }
            }
            tvRating.setText(String.format(Locale.getDefault(), "%.1f", ratingValue));
        }
    }
}

