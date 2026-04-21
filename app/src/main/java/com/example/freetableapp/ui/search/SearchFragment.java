package com.example.freetableapp.ui.search;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
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

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import androidx.core.content.res.ResourcesCompat;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import java.util.List;

public class SearchFragment extends Fragment {

    private static final int[] RADIUS_VALUES = {2, 4, 6, 9, 10};
    private static final int MAX_MAP_MARKERS = 60;

    private FragmentSearchBinding binding;
    private RestaurantRepository restaurantRepository;
    private RestaurantAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private FusedLocationProviderClient fusedLocationClient;
    private int pendingRadius = -1;
    private int activeNearbyRadius = -1;
    private boolean nearbyModeActive = false;

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
                    hideNearbyMap();
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
        setupMap();

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
                hideNearbyMap();
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
                hideNearbyMap();
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
        nearbyModeActive = true;
        activeNearbyRadius = radius;
        binding.progressBar.setVisibility(View.VISIBLE);

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (!isAdded() || binding == null) return;

                    if (location == null) {
                        binding.progressBar.setVisibility(View.GONE);
                        uncheckAllRadiusChips();
                        hideNearbyMap();
                        Toast.makeText(requireContext(), getString(R.string.location_unavailable), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    doSearchNearby(location, radius);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    uncheckAllRadiusChips();
                    hideNearbyMap();
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
                        showNearbyMap();
                        renderNearbyMap(location, data);
                        binding.tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded() || binding == null) return;
                        binding.progressBar.setVisibility(View.GONE);
                        hideNearbyMap();
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uncheckAllRadiusChips() {
        binding.chipGroupRadius.clearCheck();
        pendingRadius = -1;
        activeNearbyRadius = -1;
        nearbyModeActive = false;
    }

    private void performSearch() {
        if (binding == null) return;
        activeNearbyRadius = -1;
        nearbyModeActive = false;
        hideNearbyMap();
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

    private void setupMap() {
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        MapView mapView = binding.mapSearch;
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(13.0);
    }

    private void showNearbyMap() {
        if (binding == null || !nearbyModeActive) {
            return;
        }
        binding.mapSearch.setVisibility(View.VISIBLE);
    }

    private void hideNearbyMap() {
        if (binding == null) {
            return;
        }
        binding.mapSearch.getOverlays().clear();
        binding.mapSearch.setVisibility(View.GONE);
        binding.mapSearch.invalidate();
    }

    private void renderNearbyMap(Location userLocation, List<Restaurant> restaurants) {
        if (binding == null || !nearbyModeActive) {
            return;
        }

        MapView mapView = binding.mapSearch;
        mapView.getOverlays().clear();

        GeoPoint userPoint = new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude());

        Drawable userIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_marker_user, null);
        Drawable restaurantIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_marker_restaurant, null);

        Marker userMarker = new Marker(mapView);
        userMarker.setPosition(userPoint);
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        userMarker.setTitle(getString(R.string.map_you_are_here));
        if (userIcon != null) userMarker.setIcon(userIcon);
        mapView.getOverlays().add(userMarker);

        int renderedMarkers = 0;
        for (Restaurant restaurant : restaurants) {
            if (renderedMarkers >= MAX_MAP_MARKERS) {
                break;
            }
            if (restaurant == null || restaurant.latitude == null || restaurant.longitude == null) {
                continue;
            }

            GeoPoint restaurantPoint = new GeoPoint(restaurant.latitude, restaurant.longitude);

            Marker marker = new Marker(mapView);
            marker.setPosition(restaurantPoint);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(restaurant.name);
            marker.setSnippet(restaurant.address == null ? "" : restaurant.address);
            if (restaurantIcon != null) marker.setIcon(restaurantIcon);
            marker.setInfoWindow(new RestaurantInfoWindow(mapView));
            mapView.getOverlays().add(marker);
            renderedMarkers++;
        }

        mapView.getController().setCenter(userPoint);
        mapView.getController().setZoom(getZoomForRadius(activeNearbyRadius));

        mapView.invalidate();
    }

    private double getZoomForRadius(int radiusKm) {
        switch (radiusKm) {
            case 2:
                return 14.5;
            case 4:
                return 13.5;
            case 6:
                return 13.0;
            case 9:
                return 12.5;
            case 10:
                return 12.3;
            default:
                return 13.0;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            binding.mapSearch.onResume();
        }
    }

    @Override
    public void onPause() {
        if (binding != null) {
            binding.mapSearch.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }
        if (binding != null) {
            binding.mapSearch.onDetach();
        }
        binding = null;
    }

    private void openNavigation(double lat, double lng, String label) {
        Uri gmmUri = Uri.parse("google.navigation:q=" + lat + "," + lng + "&mode=d");
        Intent intent = new Intent(Intent.ACTION_VIEW, gmmUri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination="
                    + lat + "," + lng + "&travelmode=driving");
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    // ── Custom InfoWindow ────────────────────────────────────────────────────

    private class RestaurantInfoWindow extends InfoWindow {

        RestaurantInfoWindow(MapView map) {
            super(R.layout.map_bubble, map);
        }

        @Override
        public void onOpen(Object item) {
            Marker marker = (Marker) item;
            TextView tvTitle = mView.findViewById(R.id.bubble_title);
            TextView tvDesc  = mView.findViewById(R.id.bubble_description);
            com.google.android.material.button.MaterialButton btnNavigate =
                    mView.findViewById(R.id.btnNavigate);

            tvTitle.setText(marker.getTitle());
            tvDesc.setText(marker.getSnippet() != null ? marker.getSnippet() : "");

            GeoPoint pos = marker.getPosition();
            btnNavigate.setOnClickListener(v -> {
                openNavigation(pos.getLatitude(), pos.getLongitude(), marker.getTitle());
                close();
            });
        }

        @Override
        public void onClose() { }
    }
}
