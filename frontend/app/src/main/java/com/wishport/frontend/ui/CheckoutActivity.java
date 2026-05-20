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
import java.time.LocalDateTime;

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
            private boolean isFormatting = false;

            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}

            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String original = s.toString().replace(" ", "");
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < original.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(original.charAt(i));
                }

                etNumTarjeta.setText(formatted.toString());
                etNumTarjeta.setSelection(formatted.length());

                isFormatting = false;
            }
        });

        etCaducidad.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}

            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String original = s.toString().replace("/", "");
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < original.length() && i < 4; i++) {
                    if (i == 2) {
                        formatted.append("/");
                    }
                    formatted.append(original.charAt(i));
                }

                etCaducidad.setText(formatted.toString());
                etCaducidad.setSelection(formatted.length());

                isFormatting = false;
            }
        });
    }

    private void procesarPago() {
        btnPagar.setEnabled(false);
        progressBarPago.setVisibility(View.VISIBLE);

        String titular = etTitular.getText().toString().trim();
        String numTarjeta = etNumTarjeta.getText().toString().replace(" ", "");
        String caducidad = etCaducidad.getText().toString().trim();
        String cvv = etCVV.getText().toString().trim();

        if (titular.isEmpty() || titular.length() < 2) {
            etTitular.setError("Introduce el nombre del titular");
            etTitular.requestFocus();
            btnPagar.setEnabled(true);
            progressBarPago.setVisibility(View.GONE);
            return;
        }
        if (numTarjeta.length() != 16 || !numTarjeta.matches("\\d{16}")) {
            etNumTarjeta.setError("Debe tener 16 dígitos");
            etNumTarjeta.requestFocus();
            btnPagar.setEnabled(true);
            progressBarPago.setVisibility(View.GONE);
            return;
        }
        if (!caducidad.matches("\\d{2}/\\d{2}")) {
            etCaducidad.setError("Formato MM/AA");
            etCaducidad.requestFocus();
            btnPagar.setEnabled(true);
            progressBarPago.setVisibility(View.GONE);
            return;
        }
        try {
            int mes = Integer.parseInt(caducidad.substring(0, 2));
            if (mes < 1 || mes > 12) {
                etCaducidad.setError("Mes inválido");
                etCaducidad.requestFocus();
                btnPagar.setEnabled(true);
                progressBarPago.setVisibility(View.GONE);
                return;
            }
        } catch (NumberFormatException e) {
            etCaducidad.setError("Formato MM/AA");
            etCaducidad.requestFocus();
            btnPagar.setEnabled(true);
            progressBarPago.setVisibility(View.GONE);
            return;
        }
        if (!cvv.matches("\\d{3}")) {
            etCVV.setError("3 dígitos");
            etCVV.requestFocus();
            btnPagar.setEnabled(true);
            progressBarPago.setVisibility(View.GONE);
            return;
        }

        TokenManager tokenManager = new TokenManager(this);
        Integer userId = tokenManager.getUserId();
        String token = tokenManager.getToken();
        if (userId == null || userId == -1) {
            Toast.makeText(this, "Error: sesión no válida", Toast.LENGTH_SHORT).show();
            btnPagar.setEnabled(true);
            progressBarPago.setVisibility(View.GONE);
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(userId);

        Reserva reserva = new Reserva();
        // Backend tiene fecha=LocalDateTime; lo enviamos con la hora seleccionada
        reserva.setFecha(LocalDate.parse(fechaStr).atTime(horaInicio, 0));
        reserva.setHoraInicio(String.format("%02d:00", horaInicio));
        reserva.setHoraFin(String.format("%02d:00", horaInicio + 1));
        reserva.setIdPista(pista);
        reserva.setIdUsuario(usuario);
        reserva.setEstadoReserva("ACTIVA");

        RetrofitClient.getApiService(token).crearReserva(reserva).enqueue(new Callback<Reserva>() {
            @Override
            public void onResponse(Call<Reserva> call, Response<Reserva> response) {
                progressBarPago.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CheckoutActivity.this, "Reserva confirmada \u2713", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CheckoutActivity.this, DetalleReservaActivity.class);
                    intent.putExtra(DetalleReservaActivity.EXTRA_RESERVA, response.body());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
