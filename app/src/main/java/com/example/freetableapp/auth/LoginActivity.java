package com.example.freetableapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.freetableapp.MainActivity;
import com.example.freetableapp.databinding.ActivityLoginBinding;
import com.example.freetableapp.data.repository.AuthRepository;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepository = new AuthRepository(this);

        if (authRepository.sessionManager().isLoggedIn()) {
            goToMain();
            return;
        }

        binding.btnLogin.setOnClickListener(v -> doLogin());
        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private void doLogin() {
        String email = String.valueOf(binding.etEmail.getText()).trim();
        String password = String.valueOf(binding.etPassword.getText()).trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError("Completa correo y contrasena");
            return;
        }

        setLoading(true);
        authRepository.login(email, password, new com.example.freetableapp.data.repository.DataCallback<com.example.freetableapp.data.model.User>() {
            @Override
            public void onSuccess(com.example.freetableapp.data.model.User data) {
                setLoading(false);
                goToMain();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                showError(message);
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
        if (loading) {
            binding.tvError.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        binding.tvError.setText(message);
        binding.tvError.setVisibility(View.VISIBLE);
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

