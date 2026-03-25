package com.example.freetableapp.restaurant;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.freetableapp.R;
import com.example.freetableapp.auth.LoginActivity;
import com.example.freetableapp.data.local.SessionManager;
import com.example.freetableapp.data.model.CommentPage;
import com.example.freetableapp.data.model.RestaurantMedia;
import com.example.freetableapp.data.model.RestaurantMenu;
import com.example.freetableapp.data.model.Restaurant;
import com.example.freetableapp.data.model.Reservation;
import com.example.freetableapp.data.repository.DataCallback;
import com.example.freetableapp.data.repository.ReservationRepository;
import com.example.freetableapp.data.repository.RestaurantRepository;
import com.example.freetableapp.databinding.ActivityRestaurantDetailBinding;
import com.example.freetableapp.ui.common.CommentAdapter;
import com.example.freetableapp.ui.common.MenuAdapter;
import com.example.freetableapp.ui.common.RestaurantImageAdapter;
import com.example.freetableapp.util.UrlResolver;

import java.util.Calendar;
import java.util.Locale;

public class RestaurantDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RESTAURANT_ID = "extra_restaurant_id";

    private ActivityRestaurantDetailBinding binding;
    private RestaurantRepository restaurantRepository;
    private ReservationRepository reservationRepository;
    private SessionManager sessionManager;
    private RestaurantImageAdapter imageAdapter;
    private CommentAdapter commentAdapter;
    private MenuAdapter menuAdapter;

    private int restaurantId = -1;
    private String selectedReservationDateTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRestaurantDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        restaurantRepository = new RestaurantRepository(this);
        reservationRepository = new ReservationRepository(this);
        sessionManager = new SessionManager(this);

        setupLists();

        restaurantId = getIntent().getIntExtra(EXTRA_RESTAURANT_ID, -1);
        if (restaurantId <= 0) {
            Toast.makeText(this, "Restaurante invalido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupDefaultDate();
        binding.btnPickDate.setOnClickListener(v -> pickDateAndTime());
        binding.btnReserve.setOnClickListener(v -> createReservation());
        binding.btnSendComment.setOnClickListener(v -> createComment());

        loadRestaurant();
        loadGallery();
        loadComments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (restaurantId > 0) {
            loadComments();
        }
    }

    private void setupLists() {
        imageAdapter = new RestaurantImageAdapter();
        binding.rvGallery.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvGallery.setAdapter(imageAdapter);

        commentAdapter = new CommentAdapter();
        binding.rvComments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComments.setAdapter(commentAdapter);

        menuAdapter = new MenuAdapter(this::openMenuInBrowser);
        binding.rvMenus.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMenus.setAdapter(menuAdapter);
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
        updateRatingSummary(
                restaurant.average_rating != null ? restaurant.average_rating : 0,
                restaurant.ratings_count != null ? restaurant.ratings_count : 0
        );

        if (restaurant.images != null && !restaurant.images.isEmpty()) {
            imageAdapter.submitList(restaurant.images);
        }
        if (restaurant.menus != null) {
            bindMenus(restaurant.menus);
        }

        String imageUrl = restaurant.cover_image != null ? UrlResolver.resolveStorageUrl(restaurant.cover_image.url) : null;

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_restaurant_placeholder)
                .error(R.drawable.ic_restaurant_placeholder)
                .into(binding.ivCover);
    }

    private void loadGallery() {
        restaurantRepository.getRestaurantMedia(restaurantId, new DataCallback<RestaurantMedia>() {
            @Override
            public void onSuccess(RestaurantMedia data) {
                if (data != null && data.images != null) {
                    imageAdapter.submitList(data.images);
                }
                if (data != null && data.menus != null) {
                    bindMenus(data.menus);
                }
            }

            @Override
            public void onError(String message) {
                // Fallback to images returned by restaurant detail endpoint.
            }
        });
    }

    private void loadComments() {
        restaurantRepository.getComments(restaurantId, new DataCallback<CommentPage>() {
            @Override
            public void onSuccess(CommentPage data) {
                commentAdapter.submitList(data.comments);
                boolean isEmpty = data.comments == null || data.comments.isEmpty();
                binding.tvEmptyComments.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                int safeCount = data.ratingsCount > 0 ? data.ratingsCount : (data.comments != null ? data.comments.size() : 0);
                double safeAverage = data.averageRating > 0 ? data.averageRating : computeAverageFromComments(data);
                updateRatingSummary(safeAverage, safeCount);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(RestaurantDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRatingSummary(double average, int count) {
        if (count > 0) {
            String summary = getString(R.string.rating_summary, average, count);
            binding.tvAverageRating.setText(summary);
            binding.tvCommentSummary.setText(summary);
        } else {
            binding.tvAverageRating.setText(getString(R.string.no_reviews));
            binding.tvCommentSummary.setText(getString(R.string.no_reviews));
        }
    }

    private double computeAverageFromComments(CommentPage page) {
        if (page == null || page.comments == null || page.comments.isEmpty()) {
            return 0;
        }
        double sum = 0;
        int count = 0;
        for (com.example.freetableapp.data.model.Comment comment : page.comments) {
            if (comment == null || comment.rating == null) {
                continue;
            }
            try {
                sum += Double.parseDouble(comment.rating.replace(',', '.'));
                count++;
            } catch (NumberFormatException ignored) {
            }
        }
        return count == 0 ? 0 : (sum / count);
    }

    private void bindMenus(java.util.List<RestaurantMenu> menus) {
        menuAdapter.submitList(menus);
        boolean isEmpty = menus == null || menus.isEmpty();
        binding.tvEmptyMenus.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvMenus.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void openMenuInBrowser(RestaurantMenu menu) {
        if (menu == null) {
            return;
        }
        String url = UrlResolver.resolveStorageUrl(menu.url);
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, getString(R.string.open_menu_error), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.open_menu_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void createReservation() {
        if (!sessionManager.isLoggedIn()) {
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

    private void createComment() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, getString(R.string.login_to_comment), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        String content = String.valueOf(binding.etComment.getText()).trim();
        if (content.isEmpty()) {
            Toast.makeText(this, getString(R.string.comment_required), Toast.LENGTH_SHORT).show();
            return;
        }

        String ratingText = String.valueOf(binding.etCommentRating.getText()).trim();
        double ratingValue;
        try {
            ratingValue = Double.parseDouble(ratingText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.invalid_rating), Toast.LENGTH_SHORT).show();
            return;
        }

        if (ratingValue < 1 || ratingValue > 5 || !isHalfStep(ratingValue)) {
            Toast.makeText(this, getString(R.string.invalid_rating), Toast.LENGTH_SHORT).show();
            return;
        }

        setCommentLoading(true);
        String apiRating = formatRatingForApi(ratingValue);
        restaurantRepository.createComment(restaurantId, content, apiRating, new DataCallback<com.example.freetableapp.data.model.Comment>() {
            @Override
            public void onSuccess(com.example.freetableapp.data.model.Comment data) {
                setCommentLoading(false);
                binding.etComment.setText("");
                binding.etCommentRating.setText("");
                Toast.makeText(RestaurantDetailActivity.this, getString(R.string.comment_sent), Toast.LENGTH_SHORT).show();
                loadComments();
            }

            @Override
            public void onError(String message) {
                setCommentLoading(false);
                Toast.makeText(RestaurantDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isHalfStep(double value) {
        double scaled = value * 2;
        return Math.abs(scaled - Math.round(scaled)) < 0.0001;
    }

    private String formatRatingForApi(double value) {
        // Backend accepts 1, 1.5, 2...5 and may reject 4.0, so normalize integers.
        if (Math.abs(value - Math.round(value)) < 0.0001) {
            return String.valueOf((int) Math.round(value));
        }
        return String.format(Locale.US, "%.1f", value);
    }

    private void setCommentLoading(boolean loading) {
        binding.btnSendComment.setEnabled(!loading);
        binding.tilCommentContent.setEnabled(!loading);
        binding.tilCommentRating.setEnabled(!loading);
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnReserve.setEnabled(!loading);
        binding.btnPickDate.setEnabled(!loading);
    }
}

