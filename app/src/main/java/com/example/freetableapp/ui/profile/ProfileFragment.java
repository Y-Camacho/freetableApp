package com.example.freetableapp.ui.profile;

import android.app.AlertDialog;
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

import com.example.freetableapp.R;
import com.example.freetableapp.auth.LoginActivity;
import com.example.freetableapp.data.model.Reservation;
import com.example.freetableapp.data.model.User;
import com.example.freetableapp.data.repository.AuthRepository;
import com.example.freetableapp.data.repository.DataCallback;
import com.example.freetableapp.data.repository.ReservationRepository;
import com.example.freetableapp.databinding.FragmentProfileBinding;
import com.example.freetableapp.ui.common.ReservationAdapter;

import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private AuthRepository authRepository;
    private ReservationRepository reservationRepository;
    private ReservationAdapter reservationAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authRepository = new AuthRepository(requireContext());
        reservationRepository = new ReservationRepository(requireContext());

        reservationAdapter = new ReservationAdapter(this::confirmCancelReservation);
        binding.rvReservations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvReservations.setAdapter(reservationAdapter);

        binding.btnLogout.setOnClickListener(v -> doLogout());
        binding.btnGoToLogin.setOnClickListener(v -> startActivity(new Intent(requireContext(), LoginActivity.class)));

        renderBySessionState();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            renderBySessionState();
        }
    }

    private void renderBySessionState() {
        boolean isLoggedIn = authRepository.sessionManager().isLoggedIn();
        if (isLoggedIn) {
            showLoggedInState();
            loadProfile();
            loadReservations();
            return;
        }

        showGuestState();
    }

    private void showLoggedInState() {
        binding.btnGoToLogin.setVisibility(View.GONE);
        binding.btnLogout.setVisibility(View.VISIBLE);
        binding.tvReservationsTitle.setVisibility(View.VISIBLE);
        binding.rvReservations.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
    }

    private void showGuestState() {
        binding.tvUserName.setText(getString(R.string.guest_mode_title));
        binding.tvUserEmail.setText(getString(R.string.guest_mode_subtitle));
        binding.tvUserRole.setText("");

        reservationAdapter.submitList(java.util.Collections.emptyList());
        binding.btnGoToLogin.setVisibility(View.VISIBLE);
        binding.btnLogout.setVisibility(View.GONE);
        binding.tvReservationsTitle.setVisibility(View.GONE);
        binding.rvReservations.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.GONE);
        binding.tvEmptyReservations.setVisibility(View.GONE);
    }

    private void loadProfile() {
        User user = authRepository.sessionManager().getUser();
        if (user != null) {
            binding.tvUserName.setText(user.name);
            binding.tvUserEmail.setText(user.email);
            binding.tvUserRole.setText("Rol: " + user.role);
        }

        authRepository.me(new DataCallback<User>() {
            @Override
            public void onSuccess(User data) {
                binding.tvUserName.setText(data.name);
                binding.tvUserEmail.setText(data.email);
                binding.tvUserRole.setText("Rol: " + data.role);
            }

            @Override
            public void onError(String message) {
                // Keep cached user if API call fails.
            }
        });
    }

    private void loadReservations() {
        binding.progressBar.setVisibility(View.VISIBLE);
        reservationRepository.getMyReservations(20, new DataCallback<List<Reservation>>() {
            @Override
            public void onSuccess(List<Reservation> data) {
                binding.progressBar.setVisibility(View.GONE);
                reservationAdapter.submitList(data);
                binding.tvEmptyReservations.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmCancelReservation(Reservation reservation) {
        new AlertDialog.Builder(requireContext())
                .setMessage("Quieres cancelar esta reserva?")
                .setPositiveButton("Si", (dialog, which) -> cancelReservation(reservation.id))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelReservation(int reservationId) {
        reservationRepository.cancelReservation(reservationId, new DataCallback<Reservation>() {
            @Override
            public void onSuccess(Reservation data) {
                Toast.makeText(requireContext(), "Reserva cancelada", Toast.LENGTH_SHORT).show();
                loadReservations();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void doLogout() {
        authRepository.logout(new DataCallback<String>() {
            @Override
            public void onSuccess(String data) {
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

