package com.example.freetableapp.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.freetableapp.R;
import com.example.freetableapp.data.model.RestaurantImage;
import com.example.freetableapp.util.UrlResolver;

import java.util.ArrayList;
import java.util.List;

public class RestaurantImageAdapter extends RecyclerView.Adapter<RestaurantImageAdapter.ImageViewHolder> {

    private final List<RestaurantImage> items = new ArrayList<>();

    public void submitList(List<RestaurantImage> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivImage;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
        }

        void bind(RestaurantImage image) {
            Glide.with(itemView.getContext())
                    .load(UrlResolver.resolveStorageUrl(image != null ? image.url : null))
                    .placeholder(R.drawable.ic_restaurant_placeholder)
                    .error(R.drawable.ic_restaurant_placeholder)
                    .into(ivImage);
        }
    }
}

