package com.example.freetableapp.ui.common;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.freetableapp.R;
import com.example.freetableapp.data.model.Category;
import com.example.freetableapp.data.model.Restaurant;
import com.example.freetableapp.util.UrlResolver;

import java.util.ArrayList;
import java.util.Locale;
import java.util.List;


public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    public interface OnRestaurantClickListener {
        void onRestaurantClick(Restaurant restaurant);
    }

    private final List<Restaurant> items = new ArrayList<>();
    private final OnRestaurantClickListener listener;

    public RestaurantAdapter(OnRestaurantClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Restaurant> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = items.get(position);
        holder.bind(restaurant);
        holder.itemView.setOnClickListener(v -> listener.onRestaurantClick(restaurant));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        private final android.widget.ImageView ivCover;
        private final TextView tvName;
        private final TextView tvRating;
        private final TextView tvDescription;
        private final TextView tvAddress;
        private final TextView tvCategories;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvName = itemView.findViewById(R.id.tvName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvCategories = itemView.findViewById(R.id.tvCategories);
        }

        void bind(Restaurant restaurant) {
            tvName.setText(restaurant.name);
            tvDescription.setText(TextUtils.isEmpty(restaurant.description) ? "Sin descripcion" : restaurant.description);
            tvAddress.setText(restaurant.address == null ? "" : restaurant.address);

            if (restaurant.average_rating != null && restaurant.ratings_count != null && restaurant.ratings_count > 0) {
                String ratingText = String.format(Locale.getDefault(), "%.1f (%d)", restaurant.average_rating, restaurant.ratings_count);
                tvRating.setText(ratingText);
                tvRating.setVisibility(View.VISIBLE);
            } else {
                tvRating.setVisibility(View.GONE);
            }

            if (restaurant.categories != null && !restaurant.categories.isEmpty()) {
                StringBuilder text = new StringBuilder();
                for (int i = 0; i < restaurant.categories.size(); i++) {
                    Category category = restaurant.categories.get(i);
                    text.append(category.name);
                    if (i < restaurant.categories.size() - 1) {
                        text.append(" • ");
                    }
                }
                tvCategories.setText(text.toString());
                tvCategories.setVisibility(View.VISIBLE);
            } else {
                tvCategories.setVisibility(View.GONE);
            }

            String imageUrl = restaurant.cover_image != null ? UrlResolver.resolveStorageUrl(restaurant.cover_image.url) : null;

            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_restaurant_placeholder)
                    .error(R.drawable.ic_restaurant_placeholder)
                    .into(ivCover);
        }
    }
}

