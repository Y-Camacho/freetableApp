package com.example.freetableapp.ui.search;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.freetableapp.R;
import com.example.freetableapp.data.model.Restaurant;
import com.example.freetableapp.data.repository.DataCallback;
import com.example.freetableapp.data.repository.RestaurantRepository;
import com.example.freetableapp.databinding.FragmentSearchBinding;
import com.example.freetableapp.restaurant.RestaurantDetailActivity;
import com.example.freetableapp.ui.common.RestaurantAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.List;

public class SearchFragment extends Fragment {

    private static final int[] RADIUS_VALUES = {2, 4, 6, 9, 10};

    private FragmentSearchBinding binding;
    private RestaurantRepository restaurantRepository;
    private RestaurantAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private FusedLocationProviderClient fusedLocationClient;
    private int pendingRadius = -1;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean granted = Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_FINE_LOCATION))
                        || Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                if (granted) {
                    if (pendingRadius > 0) {
                        searchNearby(pendingRadius);
                    }
                } else {
                    uncheckAllRadiusChips();
                    Toast.makeText(requireContext(), getString(R.string.location_permission_denied), Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        restaurantRepository = new RestaurantRepository(requireContext());

        adapter = new RestaurantAdapter(restaurant -> {
            Intent intent = new Intent(requireContext(), RestaurantDetailActivity.class);
            intent.putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_ID, restaurant.id);
            startActivity(intent);
        });

        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSearchResults.setAdapter(adapter);

        setupSearchField();
        setupRadiusChips();

        performSearch();
    }

    private void setupSearchField() {
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                uncheckAllRadiusChips();
                performSearch();
                return true;
            }
            return false;
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                uncheckAllRadiusChips();
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
                searchRunnable = SearchFragment.this::performSearch;
                handler.postDelayed(searchRunnable, 400);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void setupRadiusChips() {
        int[] chipIds = {
                R.id.chip2km, R.id.chip4km, R.id.chip6km,
                R.id.chip9km, R.id.chip10km
        };

        for (int i = 0; i < chipIds.length; i++) {
            final int radius = RADIUS_VALUES[i];
            com.google.android.material.chip.Chip chip = binding.getRoot().findViewById(chipIds[i]);
            chip.setOnClickListener(v -> onRadiusChipClicked(radius));
        }
    }

    private void onRadiusChipClicked(int radius) {
        pendingRadius = radius;

        boolean hasPermission = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasPermission) {
            searchNearby(radius);
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressWarnings("MissingPermission")
    private void searchNearby(int radius) {
        binding.progressBar.setVisibility(View.VISIBLE);

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (!isAdded() || binding == null) return;

                    if (location == null) {
                        binding.progressBar.setVisibility(View.GONE);
                        uncheckAllRadiusChips();
                        Toast.makeText(requireContext(), getString(R.string.location_unavailable), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    doSearchNearby(location, radius);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    uncheckAllRadiusChips();
                    Toast.makeText(requireContext(), getString(R.string.location_unavailable), Toast.LENGTH_SHORT).show();
                });
    }

    private void doSearchNearby(Location location, int radius) {
        restaurantRepository.getNearbyRestaurants(
                location.getLatitude(),
                location.getLongitude(),
                radius,
                new DataCallback<List<Restaurant>>() {
                    @Override
                    public void onSuccess(List<Restaurant> data) {
                        if (!isAdded() || binding == null) return;
                        binding.progressBar.setVisibility(View.GONE);
                        adapter.submitList(data);
                        binding.tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded() || binding == null) return;
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uncheckAllRadiusChips() {
        binding.chipGroupRadius.clearCheck();
        pendingRadius = -1;
    }

    private void performSearch() {
        if (binding == null) return;
        String query = String.valueOf(binding.etSearch.getText()).trim();
        binding.progressBar.setVisibility(View.VISIBLE);

        restaurantRepository.getRestaurants(query.isEmpty() ? null : query, null, 20, new DataCallback<List<Restaurant>>() {
            @Override
            public void onSuccess(List<Restaurant> data) {
                if (!isAdded() || binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                adapter.submitList(data);
                binding.tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                if (!isAdded() || binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }
        binding = null;
    }
}

