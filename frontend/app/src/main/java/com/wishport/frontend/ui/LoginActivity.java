package com.wishport.frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.wishport.frontend.R;
import com.wishport.frontend.api.ApiService;
import com.wishport.frontend.api.RetrofitClient;
import com.wishport.frontend.models.Usuario;
import com.wishport.frontend.utils.TokenManager;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegistro;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = new TokenManager(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegistro = findViewById(R.id.btnRegistro);

        btnLogin.setOnClickListener(v -> login());
        btnRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistroActivity.class);
            startActivity(intent);
        });
    }

    private void login() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Usuario credenciales = new Usuario();
        credenciales.setEmail(email);
        credenciales.setPassword(password);

        ApiService apiService = RetrofitClient.getApiService(null);
        apiService.login(credenciales).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = (String) response.body().get("token");
                    Integer userId = ((Double) response.body().get("idUsuario")).intValue();
                    
                    tokenManager.saveToken(token);
                    tokenManager.saveUserId(userId);

                    Intent intent = new Intent(LoginActivity.this, PistasActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
