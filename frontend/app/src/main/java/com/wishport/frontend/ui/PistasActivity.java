package com.wishport.frontend.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.wishport.frontend.models.Reserva;
import com.wishport.frontend.models.Usuario;
import com.wishport.frontend.ui.adapters.PistaAdapter;
import com.wishport.frontend.utils.TokenManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
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
        pistaAdapter.setOnPistaClickListener(this::iniciarReserva);
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

    private void iniciarReserva(Pista pista) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (dpv, anio, mes, dia) -> {
            LocalDate fecha = LocalDate.of(anio, mes + 1, dia);
            new TimePickerDialog(this, (tpv, hora, minuto) -> {
                LocalTime horaInicio = LocalTime.of(hora, minuto);
                LocalTime horaFin = horaInicio.plusHours(1);
                crearReserva(pista, fecha, horaInicio, horaFin);
            }, 10, 0, true).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void crearReserva(Pista pista, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        Integer userId = tokenManager.getUserId();
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(userId);

        Reserva reserva = new Reserva();
        reserva.setIdPista(pista);
        reserva.setIdUsuario(usuario);
        reserva.setFecha(fecha);
        reserva.setHoraInicio(horaInicio);
        reserva.setHoraFin(horaFin);
        reserva.setEstadoReserva("ACTIVA");

        String token = tokenManager.getToken();
        RetrofitClient.getApiService(token).crearReserva(reserva).enqueue(new Callback<Reserva>() {
            @Override
            public void onResponse(Call<Reserva> call, Response<Reserva> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PistasActivity.this, "Reserva creada \u2713", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PistasActivity.this, "Error al reservar (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Reserva> call, Throwable t) {
                Toast.makeText(PistasActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
}
