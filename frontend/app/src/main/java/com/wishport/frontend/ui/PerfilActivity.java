package com.wishport.frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.wishport.frontend.R;
import com.wishport.frontend.api.ApiService;
import com.wishport.frontend.api.RetrofitClient;
import com.wishport.frontend.models.Usuario;
import com.wishport.frontend.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilActivity extends AppCompatActivity {
    private TextView tvNombre, tvEmail, tvTelefono;
    private Button btnPistas, btnReservas, btnLogout;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        tokenManager = new TokenManager(this);
        tvNombre = findViewById(R.id.tvNombre);
        tvEmail = findViewById(R.id.tvEmail);
        tvTelefono = findViewById(R.id.tvTelefono);
        btnPistas = findViewById(R.id.btnPistas);
        btnReservas = findViewById(R.id.btnReservas);
        btnLogout = findViewById(R.id.btnLogout);

        btnPistas.setOnClickListener(v -> {
            Intent intent = new Intent(this, PistasActivity.class);
            startActivity(intent);
        });

        btnReservas.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReservasActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            tokenManager.clearToken();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        cargarPerfil();
    }

    private void cargarPerfil() {
        String token = tokenManager.getToken();
        Integer userId = tokenManager.getUserId();
        ApiService apiService = RetrofitClient.getApiService(token);
        
        apiService.getUsuario(userId).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Usuario usuario = response.body();
                    tvNombre.setText(usuario.getNombre());
                    tvEmail.setText(usuario.getEmail());
                    tvTelefono.setText(usuario.getTelefono());
                } else {
                    Toast.makeText(PerfilActivity.this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                Toast.makeText(PerfilActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
