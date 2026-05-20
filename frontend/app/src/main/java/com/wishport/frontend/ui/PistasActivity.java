package com.wishport.frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wishport.frontend.R;
import com.wishport.frontend.api.ApiService;
import com.wishport.frontend.api.RetrofitClient;
import com.wishport.frontend.models.Pista;
import com.wishport.frontend.ui.adapters.PistaAdapter;
import com.wishport.frontend.utils.TokenManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PistasActivity extends AppCompatActivity {
    private RecyclerView rvPistas;
    private Button btnReservas, btnPerfil, btnLogout;
    private TokenManager tokenManager;
    private PistaAdapter pistaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pistas);

        tokenManager = new TokenManager(this);
        rvPistas = findViewById(R.id.rvPistas);
        btnReservas = findViewById(R.id.btnReservas);
        btnPerfil = findViewById(R.id.btnPerfil);
        btnLogout = findViewById(R.id.btnLogout);

        rvPistas.setLayoutManager(new LinearLayoutManager(this));
        pistaAdapter = new PistaAdapter(new ArrayList<>());
        pistaAdapter.setOnPistaClickListener(this::abrirDetallePista);
        rvPistas.setAdapter(pistaAdapter);

        btnReservas.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReservasActivity.class);
            startActivity(intent);
        });

        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(this, PerfilActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            tokenManager.clearToken();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        cargarPistas();
    }

    private void cargarPistas() {
        String token = tokenManager.getToken();
        ApiService apiService = RetrofitClient.getApiService(token);
        
        apiService.getPistas().enqueue(new Callback<List<Pista>>() {
            @Override
            public void onResponse(Call<List<Pista>> call, Response<List<Pista>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pistaAdapter.actualizarLista(response.body());
                    if (response.body().isEmpty()) {
                        Toast.makeText(PistasActivity.this, "No hay pistas disponibles", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PistasActivity.this, "Error al cargar pistas (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Pista>> call, Throwable t) {
                Toast.makeText(PistasActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void abrirDetallePista(Pista pista) {
        Intent intent = new Intent(this, DetallePistaActivity.class);
        intent.putExtra(DetallePistaActivity.EXTRA_PISTA, pista);
        startActivity(intent);
    }
}
