package com.example.freetableapp.restaurant;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.freetableapp.R;
import com.example.freetableapp.auth.LoginActivity;
import com.example.freetableapp.data.local.SessionManager;
import com.example.freetableapp.data.model.Restaurant;
import com.example.freetableapp.data.model.Reservation;
import com.example.freetableapp.data.repository.DataCallback;
import com.example.freetableapp.data.repository.ReservationRepository;
import com.example.freetableapp.data.repository.RestaurantRepository;
import com.example.freetableapp.databinding.ActivityRestaurantDetailBinding;

import java.util.Calendar;

public class RestaurantDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RESTAURANT_ID = "extra_restaurant_id";

    private ActivityRestaurantDetailBinding binding;
    private RestaurantRepository restaurantRepository;
    private ReservationRepository reservationRepository;

    private int restaurantId = -1;
    private String selectedReservationDateTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRestaurantDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        restaurantRepository = new RestaurantRepository(this);
        reservationRepository = new ReservationRepository(this);

        restaurantId = getIntent().getIntExtra(EXTRA_RESTAURANT_ID, -1);
        if (restaurantId <= 0) {
            Toast.makeText(this, "Restaurante invalido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupDefaultDate();
        binding.btnPickDate.setOnClickListener(v -> pickDateAndTime());
        binding.btnReserve.setOnClickListener(v -> createReservation());

        loadRestaurant();
    }

    private void setupDefaultDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        selectedReservationDateTime = toApiDateTime(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
        );
        binding.tvDate.setText("Fecha seleccionada: " + selectedReservationDateTime);
    }

    private void pickDateAndTime() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            TimePickerDialog timePicker = new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                selectedReservationDateTime = toApiDateTime(year, month, dayOfMonth, hourOfDay, minute);
                binding.tvDate.setText("Fecha seleccionada: " + selectedReservationDateTime);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePicker.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private String toApiDateTime(int year, int month, int day, int hour, int minute) {
        int displayMonth = month + 1;
        return String.format("%04d-%02d-%02d %02d:%02d:00", year, displayMonth, day, hour, minute);
    }

    private void loadRestaurant() {
        setLoading(true);
        restaurantRepository.getRestaurantById(restaurantId, new DataCallback<Restaurant>() {
            @Override
            public void onSuccess(Restaurant data) {
                setLoading(false);
                bindRestaurant(data);
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(RestaurantDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bindRestaurant(Restaurant restaurant) {
        binding.tvName.setText(restaurant.name);
        binding.tvAddress.setText(restaurant.address);
        binding.tvDescription.setText(TextUtils.isEmpty(restaurant.description) ? "Sin descripcion" : restaurant.description);

        String imageUrl = restaurant.cover_image != null ? restaurant.cover_image.url : null;
        if (!TextUtils.isEmpty(imageUrl) && imageUrl.startsWith("/")) {
            imageUrl = "http://10.0.2.2:8000" + imageUrl;
        }

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_restaurant_placeholder)
                .error(R.drawable.ic_restaurant_placeholder)
                .into(binding.ivCover);
    }

    private void createReservation() {
        if (!new SessionManager(this).isLoggedIn()) {
            Toast.makeText(this, "Debes iniciar sesion para reservar", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        String peopleText = String.valueOf(binding.etPeople.getText()).trim();
        if (peopleText.isEmpty()) {
            Toast.makeText(this, "Ingresa numero de personas", Toast.LENGTH_SHORT).show();
            return;
        }

        int people;
        try {
            people = Integer.parseInt(peopleText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Numero invalido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (people < 1 || people > 30) {
            Toast.makeText(this, "El numero de personas debe estar entre 1 y 30", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        reservationRepository.createReservation(restaurantId, selectedReservationDateTime, people, new DataCallback<Reservation>() {
            @Override
            public void onSuccess(Reservation data) {
                setLoading(false);
                Toast.makeText(RestaurantDetailActivity.this, "Reserva creada correctamente", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(RestaurantDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnReserve.setEnabled(!loading);
        binding.btnPickDate.setEnabled(!loading);
    }
}

