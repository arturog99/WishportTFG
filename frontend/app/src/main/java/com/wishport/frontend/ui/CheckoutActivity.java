package com.wishport.frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wishport.frontend.R;
import com.wishport.frontend.api.RetrofitClient;
import com.wishport.frontend.models.Pista;
import com.wishport.frontend.models.Reserva;
import com.wishport.frontend.models.Usuario;
import com.wishport.frontend.utils.TokenManager;

import java.time.LocalDate;
import java.time.LocalTime;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla de checkout: pago simulado y confirmación de la reserva.
 * Recibe pista, fecha (yyyy-MM-dd) y hora de inicio (int).
 * Solo valida 16 dígitos en la tarjeta. No hay pasarela real.
 */
public class CheckoutActivity extends AppCompatActivity {

    public static final String EXTRA_PISTA = "extra_pista";
    public static final String EXTRA_FECHA = "extra_fecha";
    public static final String EXTRA_HORA_INICIO = "extra_hora_inicio";

    private TextView tvResumenPista, tvResumenFechaHora;
    private EditText etTitular, etNumTarjeta, etCaducidad, etCVV;
    private Button btnPagar;
    private ProgressBar progressBarPago;

    private Pista pista;
    private String fechaStr;
    private int horaInicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        tvResumenPista = findViewById(R.id.tvResumenPista);
        tvResumenFechaHora = findViewById(R.id.tvResumenFechaHora);
        etTitular = findViewById(R.id.etTitular);
        etNumTarjeta = findViewById(R.id.etNumTarjeta);
        etCaducidad = findViewById(R.id.etCaducidad);
        etCVV = findViewById(R.id.etCVV);
        btnPagar = findViewById(R.id.btnPagar);
        progressBarPago = findViewById(R.id.progressBarPago);

        pista = (Pista) getIntent().getSerializableExtra(EXTRA_PISTA);
        fechaStr = getIntent().getStringExtra(EXTRA_FECHA);
        horaInicio = getIntent().getIntExtra(EXTRA_HORA_INICIO, -1);

        if (pista == null || fechaStr == null || horaInicio == -1) {
            Toast.makeText(this, "Error: datos de reserva incompletos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvResumenPista.setText(pista.getNombre() + " (" + pista.getDeporte() + ")");
        tvResumenFechaHora.setText("Día: " + fechaStr + "\nHora: " +
                String.format("%02d:00", horaInicio) + " - " + String.format("%02d:00", horaInicio + 1));

        configurarFormateadores();
        btnPagar.setOnClickListener(v -> procesarPago());
    }

    private void configurarFormateadores() {
        etNumTarjeta.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                String original = s.toString().replace(" ", "");
                if (original.length() > 0 && original.length() % 4 == 0 && original.length() < 16) {
                    if (s.charAt(s.length() - 1) != ' ') s.append(" ");
                }
            }
        });
        etCaducidad.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 2 && !s.toString().contains("/")) s.append("/");
            }
        });
    }

    private void procesarPago() {
        if (etNumTarjeta.getText().toString().replace(" ", "").length() != 16) {
            etNumTarjeta.setError("16 dígitos");
            return;
        }

        TokenManager tokenManager = new TokenManager(this);
        Integer userId = tokenManager.getUserId();
        String token = tokenManager.getToken();
        if (userId == null || userId == -1) {
            Toast.makeText(this, "Error: sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPagar.setEnabled(false);
        progressBarPago.setVisibility(View.VISIBLE);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(userId);

        Reserva reserva = new Reserva();
        reserva.setFecha(LocalDate.parse(fechaStr));
        reserva.setHoraInicio(LocalTime.of(horaInicio, 0));
        reserva.setHoraFin(LocalTime.of(horaInicio + 1, 0));
        reserva.setIdPista(pista);
        reserva.setIdUsuario(usuario);
        reserva.setEstadoReserva("ACTIVA");

        RetrofitClient.getApiService(token).crearReserva(reserva).enqueue(new Callback<Reserva>() {
            @Override
            public void onResponse(Call<Reserva> call, Response<Reserva> response) {
                progressBarPago.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CheckoutActivity.this, "Reserva confirmada \u2713", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(CheckoutActivity.this, ReservasActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    btnPagar.setEnabled(true);
                    Toast.makeText(CheckoutActivity.this, "Error al reservar (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Reserva> call, Throwable t) {
                progressBarPago.setVisibility(View.GONE);
                btnPagar.setEnabled(true);
                Toast.makeText(CheckoutActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
}
