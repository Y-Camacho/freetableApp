package com.example.freetableapp.ui.search;

import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.freetableapp.data.model.Restaurant;
import com.example.freetableapp.data.repository.DataCallback;
import com.example.freetableapp.data.repository.RestaurantRepository;
import com.example.freetableapp.databinding.FragmentSearchBinding;
import com.example.freetableapp.restaurant.RestaurantDetailActivity;
import com.example.freetableapp.ui.common.RestaurantAdapter;

import java.util.List;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private RestaurantRepository restaurantRepository;
    private RestaurantAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        restaurantRepository = new RestaurantRepository(requireContext());
        adapter = new RestaurantAdapter(restaurant -> {
            Intent intent = new Intent(requireContext(), RestaurantDetailActivity.class);
            intent.putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_ID, restaurant.id);
            startActivity(intent);
        });

        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSearchResults.setAdapter(adapter);

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
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
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
                searchRunnable = SearchFragment.this::performSearch;
                handler.postDelayed(searchRunnable, 400);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        performSearch();
    }

    private void performSearch() {
        String query = String.valueOf(binding.etSearch.getText()).trim();
        binding.progressBar.setVisibility(View.VISIBLE);

        restaurantRepository.getRestaurants(query.isEmpty() ? null : query, null, 20, new DataCallback<List<Restaurant>>() {
            @Override
            public void onSuccess(List<Restaurant> data) {
                binding.progressBar.setVisibility(View.GONE);
                adapter.submitList(data);
                binding.tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
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

