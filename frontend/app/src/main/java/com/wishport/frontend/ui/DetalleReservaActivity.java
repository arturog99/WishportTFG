package com.wishport.frontend.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.wishport.frontend.R;
import com.wishport.frontend.api.RetrofitClient;
import com.wishport.frontend.models.Reserva;
import com.wishport.frontend.utils.TokenManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla detalle de reserva: muestra información, código QR y permite cancelar.
 * Recibe la Reserva por Intent (Serializable). Genera QR con ZXing.
 */
public class DetalleReservaActivity extends AppCompatActivity {

    public static final String EXTRA_RESERVA = "extra_reserva";

    private TextView tvDeporte, tvPista, tvFecha, tvHora, tvEstado, tvIdReserva;
    private ImageView ivCodigoQR;
    private Button btnCancelarReserva, btnVolverReservas;
    private ProgressBar progressBar;

    private Reserva reserva;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_reserva);

        tvDeporte = findViewById(R.id.tvDeporte);
        tvPista = findViewById(R.id.tvPista);
        tvFecha = findViewById(R.id.tvFecha);
        tvHora = findViewById(R.id.tvHora);
        tvEstado = findViewById(R.id.tvEstado);
        tvIdReserva = findViewById(R.id.tvIdReserva);
        ivCodigoQR = findViewById(R.id.ivCodigoQR);
        btnCancelarReserva = findViewById(R.id.btnCancelarReserva);
        btnVolverReservas = findViewById(R.id.btnVolverReservas);
        progressBar = findViewById(R.id.progressBar);

        reserva = (Reserva) getIntent().getSerializableExtra(EXTRA_RESERVA);
        if (reserva == null) {
            Toast.makeText(this, "Error al cargar la reserva", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rellenarDatosPantalla();
        generarImagenQR();

        btnCancelarReserva.setOnClickListener(v -> confirmarCancelacion());
        btnVolverReservas.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReservasActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void rellenarDatosPantalla() {
        String deporte = (reserva.getIdPista() != null) ? reserva.getIdPista().getDeporte() : "Pista";
        String nombrePista = (reserva.getIdPista() != null) ? reserva.getIdPista().getNombre() : "N/A";
        tvDeporte.setText(deporte);
        tvPista.setText(nombrePista);

        String fechaStr = "N/A";
        if (reserva.getFecha() != null) {
            fechaStr = reserva.getFecha().toLocalDate().format(dateFormatter);
        }
        tvFecha.setText(fechaStr);

        String horaI = (reserva.getHoraInicio() != null) ? reserva.getHoraInicio() : "N/A";
        String horaF = (reserva.getHoraFin() != null) ? reserva.getHoraFin() : "N/A";
        tvHora.setText(horaI + " - " + horaF);

        tvIdReserva.setText("#" + reserva.getIdReserva());

        configurarVisualEstado();

        if (esReservaAntigua()) {
            desactivarBotonCancelacion("Finalizada");
        }
    }

    private void configurarVisualEstado() {
        String estado = (reserva.getEstadoReserva() != null) ? reserva.getEstadoReserva() : "ACTIVA";
        tvEstado.setText(estado.toUpperCase());

        if (estado.equalsIgnoreCase("activa")) {
            tvEstado.setTextColor(Color.parseColor("#4CAF50"));
        } else if (estado.equalsIgnoreCase("cancelada")) {
            tvEstado.setTextColor(Color.parseColor("#F44336"));
            desactivarBotonCancelacion("Cancelada");
        } else {
            tvEstado.setTextColor(Color.GRAY);
        }
    }

    private boolean esReservaAntigua() {
        if (reserva.getFecha() == null) return false;
        return reserva.getFecha().isBefore(LocalDateTime.now());
    }

    private void desactivarBotonCancelacion(String label) {
        btnCancelarReserva.setEnabled(false);
        btnCancelarReserva.setAlpha(0.5f);
        btnCancelarReserva.setText(label);
    }

    private void generarImagenQR() {
        String data = reserva.getCodigoQr();
        if (data == null || data.isEmpty()) data = "ID-" + reserva.getIdReserva();

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512);
            Bitmap bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);
            for (int x = 0; x < 512; x++) {
                for (int y = 0; y < 512; y++) {
                    bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            ivCodigoQR.setImageBitmap(bmp);
        } catch (WriterException e) {
            Log.e("QR_ERROR", "No se pudo generar el QR", e);
        }
    }

    private void confirmarCancelacion() {
        new AlertDialog.Builder(this)
                .setTitle("¿Cancelar reserva?")
                .setMessage("Esta acción es irreversible.")
                .setPositiveButton("Sí, cancelar", (dialog, which) -> ejecutarPeticionCancelacion())
                .setNegativeButton("No", null)
                .show();
    }

    private void ejecutarPeticionCancelacion() {
        progressBar.setVisibility(View.VISIBLE);
        btnCancelarReserva.setEnabled(false);

        String token = new TokenManager(this).getToken();
        RetrofitClient.getApiService(token).cancelarReserva(reserva.getIdReserva()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(DetalleReservaActivity.this, "Reserva cancelada", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnCancelarReserva.setEnabled(true);
                    Toast.makeText(DetalleReservaActivity.this, "No se pudo cancelar (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnCancelarReserva.setEnabled(true);
                Toast.makeText(DetalleReservaActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
