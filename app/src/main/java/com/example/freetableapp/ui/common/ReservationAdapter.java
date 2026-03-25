package com.example.freetableapp.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.freetableapp.R;
import com.example.freetableapp.data.model.Reservation;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    public interface OnReservationActionListener {
        void onCancel(Reservation reservation);
    }

    private final List<Reservation> items = new ArrayList<>();
    private final OnReservationActionListener listener;

    public ReservationAdapter(OnReservationActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Reservation> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = items.get(position);
        holder.bind(reservation, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRestaurantName;
        private final TextView tvDate;
        private final TextView tvPeople;
        private final TextView tvStatus;
        private final MaterialButton btnCancel;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPeople = itemView.findViewById(R.id.tvPeople);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCancel = itemView.findViewById(R.id.btnCancelReservation);
        }

        void bind(Reservation reservation, OnReservationActionListener listener) {
            String restaurantName = reservation.restaurant != null ? reservation.restaurant.name : "Restaurante";
            tvRestaurantName.setText(restaurantName);
            tvDate.setText(reservation.reservation_time);
            tvPeople.setText(reservation.people + " personas");
            tvStatus.setText(statusText(reservation.status));

            int statusColor;
            switch (reservation.status) {
                case "confirmed":
                    statusColor = R.color.colorConfirmed;
                    break;
                case "cancelled":
                    statusColor = R.color.colorCancelled;
                    break;
                default:
                    statusColor = R.color.colorPending;
                    break;
            }
            tvStatus.setTextColor(itemView.getContext().getColor(statusColor));

            if ("cancelled".equals(reservation.status)) {
                btnCancel.setVisibility(View.GONE);
            } else {
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(v -> listener.onCancel(reservation));
            }
        }

        private String statusText(String status) {
            if ("confirmed".equals(status)) return "Confirmada";
            if ("cancelled".equals(status)) return "Cancelada";
            return "Pendiente";
        }
    }
}

