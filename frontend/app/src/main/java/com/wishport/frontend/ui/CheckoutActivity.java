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
 * Activity de checkout (pago y confirmación de reserva)
 * Simula un proceso de pago con tarjeta de crédito
 * Valida los datos de la tarjeta y crea la reserva en el backend
 * 
 * Nota: El pago es simulado, no hay pasarela real
 * Solo valida que la tarjeta tenga 16 dígitos numéricos
 */
public class CheckoutActivity extends AppCompatActivity {

    /**
     * Clave para pasar el objeto Pista como extra en el Intent
     */
    public static final String EXTRA_PISTA = "extra_pista";
    
    /**
     * Clave para pasar la fecha como extra en el Intent (formato yyyy-MM-dd)
     */
    public static final String EXTRA_FECHA = "extra_fecha";
    
    /**
     * Clave para pasar la hora de inicio como extra en el Intent (int)
     */
    public static final String EXTRA_HORA_INICIO = "extra_hora_inicio";

    /**
     * TextView para mostrar el resumen de la pista seleccionada
     */
    private TextView tvResumenPista;
    
    /**
     * TextView para mostrar el resumen de fecha y hora seleccionadas
     */
    private TextView tvResumenFechaHora;
    
    /**
     * EditText para ingresar el nombre del titular de la tarjeta
     */
    private EditText etTitular;
    
    /**
     * EditText para ingresar el número de la tarjeta
     */
    private EditText etNumTarjeta;
    
    /**
     * EditText para ingresar la fecha de caducidad de la tarjeta
     */
    private EditText etCaducidad;
    
    /**
     * EditText para ingresar el código CVV de la tarjeta
     */
    private EditText etCVV;
    
    /**
     * Botón para procesar el pago
     */
    private Button btnPagar;
    
    /**
     * ProgressBar para mostrar el progreso del pago
     */
    private ProgressBar progressBarPago;

    /**
     * Objeto Pista seleccionada para la reserva
     */
    private Pista pista;
    
    /**
     * Fecha seleccionada para la reserva (formato yyyy-MM-dd)
     */
    private String fechaStr;
    
    /**
     * Hora de inicio seleccionada para la reserva
     */
    private int horaInicio;

    /**
     * Método llamado al crear la Activity
     * Inicializa los componentes de la UI, muestra el resumen de la reserva
     * y configura los formateadores de los campos de tarjeta
     * 
     * @param savedInstanceState Bundle con el estado guardado de la Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Encontrar referencias a los elementos de la UI
        tvResumenPista = findViewById(R.id.tvResumenPista);
        tvResumenFechaHora = findViewById(R.id.tvResumenFechaHora);
        etTitular = findViewById(R.id.etTitular);
        etNumTarjeta = findViewById(R.id.etNumTarjeta);
        etCaducidad = findViewById(R.id.etCaducidad);
        etCVV = findViewById(R.id.etCVV);
        btnPagar = findViewById(R.id.btnPagar);
        progressBarPago = findViewById(R.id.progressBarPago);

        // Obtener los datos pasados como extra
        pista = (Pista) getIntent().getSerializableExtra(EXTRA_PISTA);
        fechaStr = getIntent().getStringExtra(EXTRA_FECHA);
        horaInicio = getIntent().getIntExtra(EXTRA_HORA_INICIO, -1);

        // Validar que se hayan recibido todos los datos necesarios
        if (pista == null || fechaStr == null || horaInicio == -1) {
            Toast.makeText(this, "Error: datos de reserva incompletos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mostrar el resumen de la pista seleccionada
        tvResumenPista.setText(pista.getNombre() + " (" + pista.getDeporte() + ")");
        
        // Mostrar el resumen de fecha y hora seleccionadas
        tvResumenFechaHora.setText("Día: " + fechaStr + "\nHora: " +
                String.format("%02d:00", horaInicio) + " - " + String.format("%02d:00", horaInicio + 1));

        // Configurar los formateadores automáticos de los campos de tarjeta
        configurarFormateadores();
        
        // Configurar listener del botón de pagar
        btnPagar.setOnClickListener(v -> procesarPago());
    }

    /**
     * Configura los formateadores automáticos de los campos de tarjeta
     * - Número de tarjeta: agrega espacios cada 4 dígitos
     * - Caducidad: agrega barra después de los primeros 2 dígitos
     */
    private void configurarFormateadores() {
        // Formateador para el número de tarjeta (agrega espacios cada 4 dígitos)
        etNumTarjeta.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}

            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Evitar bucle infinito de formateo
                if (isFormatting) return;
                isFormatting = true;

                // Eliminar espacios existentes
                String original = s.toString().replace(" ", "");
                StringBuilder formatted = new StringBuilder();

                // Agregar espacio cada 4 dígitos
                for (int i = 0; i < original.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(original.charAt(i));
                }

                // Actualizar el EditText con el formato
                etNumTarjeta.setText(formatted.toString());
                etNumTarjeta.setSelection(formatted.length());

                isFormatting = false;
            }
        });

        // Formateador para la caducidad (agrega barra después de 2 dígitos)
        etCaducidad.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}

            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Evitar bucle infinito de formateo
                if (isFormatting) return;
                isFormatting = true;

                // Eliminar barra existente
                String original = s.toString().replace("/", "");
                StringBuilder formatted = new StringBuilder();

                // Agregar barra después de 2 dígitos
                for (int i = 0; i < original.length() && i < 4; i++) {
                    if (i == 2) {
                        formatted.append("/");
                    }
                    formatted.append(original.charAt(i));
                }

                // Actualizar el EditText con el formato
                etCaducidad.setText(formatted.toString());
                etCaducidad.setSelection(formatted.length());

                isFormatting = false;
            }
        });
    }

    /**
     * Procesa el pago y crea la reserva
     * Valida los datos de la tarjeta y envía la reserva al backend
     */
    private void procesarPago() {
        // Deshabilitar el botón de pago para evitar múltiples peticiones
        btnPagar.setEnabled(false);
        
        // Mostrar el ProgressBar
        progressBarPago.setVisibility(View.VISIBLE);

        // Obtener los valores de los campos
        String titular = etTitular.getText().toString().trim();
        String numTarjeta = etNumTarjeta.getText().toString().replace(" ", "");
        String caducidad = etCaducidad.getText().toString().trim();
        String cvv = etCVV.getText().toString().trim();

        // Validar el nombre del titular
        if (titular.isEmpty() || titular.length() < 2) {
            etTitular.setError("Introduce el nombre del titular");
            etTitular.requestFocus();
            btnPagar.setEnabled(true);
            progressBarPago.setVisibility(View.GONE);
            return;
        }
        
        // Validar el número de tarjeta (debe tener 16 dígitos numéricos)
        if (numTarjeta.length() != 16 || !numTarjeta.matches("\\d{16}")) {
            etNumTarjeta.setError("Debe tener 16 dígitos");
            etNumTarjeta.requestFocus();
            btnPagar.setEnabled(true);
            progressBarPago.setVisibility(View.GONE);
            return;
        }
        
        // Validar el formato de la caducidad (MM/AA)
        if (!caducidad.matches("\\d{2}/\\d{2}")) {
            etCaducidad.setError("Formato MM/AA");
            etCaducidad.requestFocus();
            btnPagar.setEnabled(true);
            progressBarPago.setVisibility(View.GONE);
            return;
        }
        
        // Validar que el mes esté entre 1 y 12
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
        
        // Validar el CVV (debe tener 3 dígitos)
        if (!cvv.matches("\\d{3}")) {
            etCVV.setError("3 dígitos");
            etCVV.requestFocus();
            btnPagar.setEnabled(true);
            progressBarPago.setVisibility(View.GONE);
            return;
        }

        // Obtener el token JWT y el ID del usuario
        TokenManager tokenManager = new TokenManager(this);
        Integer userId = tokenManager.getUserId();
        String token = tokenManager.getToken();
        
        // Validar que el usuario esté autenticado
        if (userId == null || userId == -1) {
            Toast.makeText(this, "Error: sesión no válida", Toast.LENGTH_SHORT).show();
            btnPagar.setEnabled(true);
            progressBarPago.setVisibility(View.GONE);
            return;
        }

        // Crear objeto Usuario con el ID del usuario
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(userId);

        // Crear objeto Reserva con los datos de la reserva
        Reserva reserva = new Reserva();
        
        // Establecer la fecha y hora de la reserva
        // Backend espera LocalDateTime, enviamos la fecha con la hora seleccionada
        reserva.setFecha(LocalDate.parse(fechaStr).atTime(horaInicio, 0));
        reserva.setHoraInicio(String.format("%02d:00", horaInicio));
        reserva.setHoraFin(String.format("%02d:00", horaInicio + 1));
        
        // Establecer la pista y el usuario
        reserva.setIdPista(pista);
        reserva.setIdUsuario(usuario);
        
        // Establecer el estado como ACTIVA
        reserva.setEstadoReserva("ACTIVA");

        // Realizar petición asíncrona al backend para crear la reserva
        RetrofitClient.getApiService(token).crearReserva(reserva).enqueue(new Callback<Reserva>() {
            @Override
            public void onResponse(Call<Reserva> call, Response<Reserva> response) {
                // Ocultar el ProgressBar
                progressBarPago.setVisibility(View.GONE);
                
                // Verificar que la petición fue exitosa
                if (response.isSuccessful() && response.body() != null) {
                    // Reserva creada exitosamente
                    Toast.makeText(CheckoutActivity.this, "Reserva confirmada \u2713", Toast.LENGTH_SHORT).show();
                    
                    // Navegar a DetalleReservaActivity para mostrar los detalles de la reserva
                    Intent intent = new Intent(CheckoutActivity.this, DetalleReservaActivity.class);
                    intent.putExtra(DetalleReservaActivity.EXTRA_RESERVA, response.body());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    // La petición no fue exitosa
                    btnPagar.setEnabled(true);
                    Toast.makeText(CheckoutActivity.this, "Error al reservar (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Reserva> call, Throwable t) {
                // Ocultar el ProgressBar
                progressBarPago.setVisibility(View.GONE);
                
                // Reactivar el botón de pago
                btnPagar.setEnabled(true);
                
                // Mostrar error de conexión al usuario
                Toast.makeText(CheckoutActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                
                // Imprimir el error en el log para debugging
                t.printStackTrace();
            }
        });
    }
}
