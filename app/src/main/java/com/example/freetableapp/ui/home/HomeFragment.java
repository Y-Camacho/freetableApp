package com.example.freetableapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.freetableapp.data.model.Category;
import com.example.freetableapp.data.model.Restaurant;
import com.example.freetableapp.data.model.User;
import com.example.freetableapp.data.repository.AuthRepository;
import com.example.freetableapp.data.repository.DataCallback;
import com.example.freetableapp.data.repository.RestaurantRepository;
import com.example.freetableapp.databinding.FragmentHomeBinding;
import com.example.freetableapp.ui.common.RestaurantAdapter;
import com.example.freetableapp.restaurant.RestaurantDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RestaurantRepository restaurantRepository;
    private AuthRepository authRepository;

    private RestaurantAdapter popularAdapter;
    private RestaurantAdapter recommendedAdapter;
    private CategoryAdapter categoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        restaurantRepository = new RestaurantRepository(requireContext());
        authRepository = new AuthRepository(requireContext());

        setupRecyclerViews();
        setupGreeting();

        binding.swipeRefresh.setOnRefreshListener(this::loadData);
        loadData();
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter(category -> loadRestaurantsByCategory(category));
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);

        popularAdapter = new RestaurantAdapter(this::openRestaurantDetail);
        binding.rvPopular.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPopular.setAdapter(popularAdapter);

        recommendedAdapter = new RestaurantAdapter(this::openRestaurantDetail);
        binding.rvRecommendations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecommendations.setAdapter(recommendedAdapter);
    }

    private void setupGreeting() {
        User user = authRepository.sessionManager().getUser();
        if (user != null && user.name != null) {
            binding.tvGreeting.setText("Hola, " + user.name + "");
        }
    }

    private void loadData() {
        binding.swipeRefresh.setRefreshing(true);

        restaurantRepository.getCategories(new DataCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> data) {
                categoryAdapter.submitList(data);
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });

        restaurantRepository.getRestaurants(null, null, 20, new DataCallback<List<Restaurant>>() {
            @Override
            public void onSuccess(List<Restaurant> data) {
                List<Restaurant> popular = new ArrayList<>();
                List<Restaurant> recommended = new ArrayList<>();

                for (int i = 0; i < data.size(); i++) {
                    if (i < 5) {
                        popular.add(data.get(i));
                    } else {
                        recommended.add(data.get(i));
                    }
                }

                if (recommended.isEmpty()) {
                    recommended.addAll(popular);
                }

                popularAdapter.submitList(popular);
                recommendedAdapter.submitList(recommended);
                binding.swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onError(String message) {
                binding.swipeRefresh.setRefreshing(false);
                showToast(message);
            }
        });
    }

    private void loadRestaurantsByCategory(Category category) {
        binding.swipeRefresh.setRefreshing(true);
        restaurantRepository.getRestaurants(null, category.id, 20, new DataCallback<List<Restaurant>>() {
            @Override
            public void onSuccess(List<Restaurant> data) {
                popularAdapter.submitList(data);
                recommendedAdapter.submitList(new ArrayList<>());
                binding.swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onError(String message) {
                binding.swipeRefresh.setRefreshing(false);
                showToast(message);
            }
        });
    }

    private void openRestaurantDetail(Restaurant restaurant) {
        Intent intent = new Intent(requireContext(), RestaurantDetailActivity.class);
        intent.putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_ID, restaurant.id);
        startActivity(intent);
    }

    private void showToast(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

