package com.example.freetableapp.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.freetableapp.R;
import com.example.freetableapp.data.model.RestaurantMenu;

import java.util.ArrayList;
import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    public interface OnMenuClickListener {
        void onMenuClick(RestaurantMenu menu);
    }

    private final List<RestaurantMenu> items = new ArrayList<>();
    private final OnMenuClickListener listener;

    public MenuAdapter(OnMenuClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<RestaurantMenu> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_pdf, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        RestaurantMenu menu = items.get(position);
        holder.bind(menu);
        holder.itemView.setOnClickListener(v -> listener.onMenuClick(menu));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMenuName;

        MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMenuName = itemView.findViewById(R.id.tvMenuName);
        }

        void bind(RestaurantMenu menu) {
            tvMenuName.setText(menu != null && menu.name != null ? menu.name : "Menu");
        }
    }
}

