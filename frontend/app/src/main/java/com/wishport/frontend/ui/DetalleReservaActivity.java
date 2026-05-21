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
 * Activity de detalle de reserva
 * Muestra información detallada de una reserva específica
 * Genera y muestra un código QR para validar el acceso
 * Permite cancelar la reserva si aún no ha pasado
 */
public class DetalleReservaActivity extends AppCompatActivity {

    /**
     * Clave para pasar el objeto Reserva como extra en el Intent
     */
    public static final String EXTRA_RESERVA = "extra_reserva";

    /**
     * TextView para mostrar el tipo de deporte de la pista
     */
    private TextView tvDeporte;
    
    /**
     * TextView para mostrar el nombre de la pista
     */
    private TextView tvPista;
    
    /**
     * TextView para mostrar la fecha de la reserva
     */
    private TextView tvFecha;
    
    /**
     * TextView para mostrar el horario de la reserva
     */
    private TextView tvHora;
    
    /**
     * TextView para mostrar el estado de la reserva
     */
    private TextView tvEstado;
    
    /**
     * TextView para mostrar el ID de la reserva
     */
    private TextView tvIdReserva;
    
    /**
     * ImageView para mostrar el código QR de la reserva
     */
    private ImageView ivCodigoQR;
    
    /**
     * Botón para cancelar la reserva
     */
    private Button btnCancelarReserva;
    
    /**
     * Botón para volver a la lista de reservas
     */
    private Button btnVolverReservas;
    
    /**
     * ProgressBar para mostrar el progreso de la cancelación
     */
    private ProgressBar progressBar;

    /**
     * Objeto Reserva que se está mostrando
     */
    private Reserva reserva;
    
    /**
     * Formateador de fecha para mostrar en la UI (dd/MM/yyyy)
     */
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Método llamado al crear la Activity
     * Inicializa los componentes de la UI, muestra los datos de la reserva
     * y genera el código QR
     * 
     * @param savedInstanceState Bundle con el estado guardado de la Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_reserva);

        // Encontrar referencias a los elementos de la UI
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

        // Obtener el objeto Reserva pasado como extra
        reserva = (Reserva) getIntent().getSerializableExtra(EXTRA_RESERVA);
        
        // Validar que se haya recibido una reserva
        if (reserva == null) {
            Toast.makeText(this, "Error al cargar la reserva", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mostrar los datos de la reserva en la UI
        rellenarDatosPantalla();
        
        // Generar y mostrar el código QR
        generarImagenQR();

        // Configurar listener del botón de cancelar reserva
        btnCancelarReserva.setOnClickListener(v -> confirmarCancelacion());
        
        // Configurar listener del botón de volver
        btnVolverReservas.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReservasActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        // Verificar si viene de la vista de administrador
        boolean esAdmin = getIntent().getBooleanExtra("ES_ADMIN", false);
        if (esAdmin) {
            btnVolverReservas.setText("Volver a lista de reservas");
            btnVolverReservas.setOnClickListener(v -> {
                finish();
            });
        }
    }

    /**
     * Rellena los campos de la UI con los datos de la reserva
     * Muestra deporte, pista, fecha, hora, estado y ID de la reserva
     */
    private void rellenarDatosPantalla() {
        // Obtener el deporte y nombre de la pista
        String deporte = (reserva.getIdPista() != null) ? reserva.getIdPista().getDeporte() : "Pista";
        String nombrePista = (reserva.getIdPista() != null) ? reserva.getIdPista().getNombre() : "N/A";
        tvDeporte.setText(deporte);
        tvPista.setText(nombrePista);

        // Formatear y mostrar la fecha
        String fechaStr = "N/A";
        if (reserva.getFecha() != null) {
            fechaStr = reserva.getFecha().toLocalDate().format(dateFormatter);
        }
        tvFecha.setText(fechaStr);

        // Mostrar el horario
        String horaI = (reserva.getHoraInicio() != null) ? reserva.getHoraInicio() : "N/A";
        String horaF = (reserva.getHoraFin() != null) ? reserva.getHoraFin() : "N/A";
        tvHora.setText(horaI + " - " + horaF);

        // Mostrar el ID de la reserva
        tvIdReserva.setText("#" + reserva.getIdReserva());

        // Configurar la visualización del estado
        configurarVisualEstado();

        // Desactivar el botón de cancelación si la reserva es antigua
        if (esReservaAntigua()) {
            desactivarBotonCancelacion("Finalizada");
        }
    }

    /**
     * Configura la visualización del estado de la reserva
     * Muestra el estado en color según su valor (verde para activa, rojo para cancelada)
     */
    private void configurarVisualEstado() {
        // Obtener el estado de la reserva
        String estado = (reserva.getEstadoReserva() != null) ? reserva.getEstadoReserva() : "ACTIVA";
        tvEstado.setText(estado.toUpperCase());

        // Configurar el color según el estado
        if (estado.equalsIgnoreCase("activa")) {
            // Verde para reservas activas
            tvEstado.setTextColor(Color.parseColor("#4CAF50"));
        } else if (estado.equalsIgnoreCase("cancelada")) {
            // Rojo para reservas canceladas
            tvEstado.setTextColor(Color.parseColor("#F44336"));
            desactivarBotonCancelacion("Cancelada");
        } else {
            // Gris para otros estados
            tvEstado.setTextColor(Color.GRAY);
        }
    }

    /**
     * Verifica si la reserva es antigua (ya pasó su fecha)
     * 
     * @return true si la fecha de la reserva es anterior a ahora, false en caso contrario
     */
    private boolean esReservaAntigua() {
        if (reserva.getFecha() == null) return false;
        return reserva.getFecha().isBefore(LocalDateTime.now());
    }

    /**
     * Desactiva el botón de cancelación de la reserva
     * Se usa cuando la reserva ya pasó o está cancelada
     * 
     * @param label Texto a mostrar en el botón (ej: "Cancelada", "Finalizada")
     */
    private void desactivarBotonCancelacion(String label) {
        btnCancelarReserva.setEnabled(false);
        btnCancelarReserva.setAlpha(0.5f);
        btnCancelarReserva.setText(label);
    }

    /**
     * Genera una imagen de código QR a partir del código QR de la reserva
     * Usa la librería ZXing para generar el código QR
     * Si no hay código QR, usa el ID de la reserva como dato
     */
    private void generarImagenQR() {
        // Obtener el código QR de la reserva
        String data = reserva.getCodigoQr();
        
        // Si no hay código QR, usar el ID de la reserva
        if (data == null || data.isEmpty()) data = "ID-" + reserva.getIdReserva();

        try {
            // Crear un writer de códigos QR
            QRCodeWriter writer = new QRCodeWriter();
            
            // Generar la matriz del código QR (512x512 píxeles)
            BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512);
            
            // Crear un bitmap de 512x512 píxeles
            Bitmap bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);
            
            // Rellenar el bitmap con los píxeles del código QR
            for (int x = 0; x < 512; x++) {
                for (int y = 0; y < 512; y++) {
                    bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            // Mostrar el código QR en el ImageView
            ivCodigoQR.setImageBitmap(bmp);
        } catch (WriterException e) {
            // Manejar error al generar el código QR
            Log.e("QR_ERROR", "No se pudo generar el QR", e);
        }
    }

    /**
     * Muestra un diálogo de confirmación para cancelar la reserva
     * El usuario debe confirmar antes de proceder con la cancelación
     */
    private void confirmarCancelacion() {
        new AlertDialog.Builder(this)
                .setTitle("¿Cancelar reserva?")
                .setMessage("Esta acción es irreversible.")
                .setPositiveButton("Sí, cancelar", (dialog, which) -> ejecutarPeticionCancelacion())
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Ejecuta la petición al backend para cancelar la reserva
     * Realiza una petición DELETE al endpoint de cancelación
     */
    private void ejecutarPeticionCancelacion() {
        // Mostrar el ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        
        // Deshabilitar el botón de cancelación para evitar múltiples peticiones
        btnCancelarReserva.setEnabled(false);

        // Obtener el token JWT
        String token = new TokenManager(this).getToken();
        
        // Realizar petición asíncrona al backend para cancelar la reserva
        RetrofitClient.getApiService(token).cancelarReserva(reserva.getIdReserva()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Ocultar el ProgressBar
                progressBar.setVisibility(View.GONE);
                
                // Verificar que la petición fue exitosa
                if (response.isSuccessful()) {
                    // Reserva cancelada exitosamente
                    Toast.makeText(DetalleReservaActivity.this, "Reserva cancelada", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // La petición no fue exitosa
                    btnCancelarReserva.setEnabled(true);
                    Toast.makeText(DetalleReservaActivity.this, "No se pudo cancelar (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Ocultar el ProgressBar
                progressBar.setVisibility(View.GONE);
                
                // Reactivar el botón de cancelación
                btnCancelarReserva.setEnabled(true);
                
                // Mostrar error de conexión al usuario
                Toast.makeText(DetalleReservaActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
