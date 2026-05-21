package com.wishport.frontend.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wishport.frontend.R;
import com.wishport.frontend.api.ApiService;
import com.wishport.frontend.api.RetrofitClient;
import com.wishport.frontend.models.Pista;
import com.wishport.frontend.models.Reserva;
import com.wishport.frontend.utils.TokenManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity de detalle de pista
 * Muestra información detallada de una pista deportiva
 * Permite seleccionar fecha y hora para realizar una reserva
 * 
 * Funcionalidades:
 * - Muestra nombre, deporte, estado e imagen de la pista
 * - Permite seleccionar fecha mediante un calendario
 * - Muestra horarios disponibles en un GridLayout
 * - Deshabilita horarios ya pasados (si es hoy) y horarios ocupados
 * - Navega a CheckoutActivity para confirmar la reserva
 */
public class DetallePistaActivity extends AppCompatActivity {

    /**
     * Clave para pasar el objeto Pista como extra en el Intent
     */
    public static final String EXTRA_PISTA = "extra_pista";
    
    /**
     * Hora de apertura de las pistas (8:00 AM)
     */
    private static final int HORA_APERTURA = 8;
    
    /**
     * Hora de cierre de las pistas (22:00)
     */
    private static final int HORA_CIERRE = 22;
    
    /**
     * Formateador de fecha para mostrar en la UI (yyyy-MM-dd)
     */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * ImageView para mostrar la imagen de la pista
     */
    private ImageView ivDetallePista;
    
    /**
     * TextView para mostrar el nombre de la pista
     */
    private TextView tvNombre;
    
    /**
     * TextView para mostrar el tipo de deporte
     */
    private TextView tvDeporte;
    
    /**
     * TextView para mostrar el estado de la pista
     */
    private TextView tvEstado;
    
    /**
     * TextView para mostrar la fecha seleccionada
     */
    private TextView tvFechaSeleccionada;
    
    /**
     * Botón para abrir el calendario y seleccionar fecha
     */
    private Button btnSeleccionarFecha;
    
    /**
     * Botón para proceder con la reserva
     */
    private Button btnReservar;
    
    /**
     * GridLayout para mostrar los horarios disponibles
     */
    private GridLayout gridHorarios;

    /**
     * Objeto Pista actual que se está mostrando
     */
    private Pista pistaActual;
    
    /**
     * Fecha seleccionada por el usuario para la reserva
     */
    private LocalDate fechaSeleccionada;
    
    /**
     * Hora de inicio seleccionada (-1 si no se ha seleccionado ninguna)
     */
    private int horaInicioSeleccionada = -1;
    
    /**
     * Lista de botones de horarios para poder actualizarlos
     */
    private final List<Button> botonesHorarios = new ArrayList<>();
    
    /**
     * Conjunto de horas ocupadas para la fecha seleccionada
     */
    private final Set<Integer> horasOcupadas = new HashSet<>();
    
    /**
     * Lista de reservas de la pista actual
     */
    private List<Reserva> reservasPista = new ArrayList<>();

    /**
     * Método llamado al crear la Activity
     * Inicializa los componentes de la UI, muestra la información de la pista
     * y carga las reservas existentes
     * 
     * @param savedInstanceState Bundle con el estado guardado de la Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_pista);

        // Encontrar referencias a los elementos de la UI
        ivDetallePista = findViewById(R.id.ivDetallePista);
        tvNombre = findViewById(R.id.tvDetalleNombre);
        tvDeporte = findViewById(R.id.tvDetalleDeporte);
        tvEstado = findViewById(R.id.tvDetalleEstado);
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada);
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha);
        btnReservar = findViewById(R.id.btnReservar);
        gridHorarios = findViewById(R.id.gridHorarios);

        // Obtener el objeto Pista pasado como extra
        pistaActual = (Pista) getIntent().getSerializableExtra(EXTRA_PISTA);
        
        // Establecer la fecha seleccionada por defecto a hoy
        fechaSeleccionada = LocalDate.now();

        // Validar que se haya recibido una pista
        if (pistaActual == null) {
            Toast.makeText(this, "Error: pista no recibida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mostrar la información de la pista en la UI
        mostrarInfoPista();
        
        // Preparar el GridLayout con los horarios disponibles
        prepararGridHorarios();
        
        // Actualizar el estado de los botones de horarios
        refrescarEstadoBotones();
        
        // Cargar las reservas existentes de la pista
        cargarReservasPista();

        // Configurar listener del botón de selección de fecha
        btnSeleccionarFecha.setOnClickListener(v -> mostrarCalendario());
        
        // Configurar listener del botón de reservar
        btnReservar.setOnClickListener(v -> proceder());
    }

    /**
     * Método llamado cuando la Activity se reanuda
     * Recarga las reservas para actualizar los horarios ocupados
     */
    @Override
    protected void onResume() {
        super.onResume();
        cargarReservasPista();
    }

    /**
     * Muestra la información de la pista en la UI
     * Carga la imagen usando Glide y muestra nombre, deporte y estado
     */
    private void mostrarInfoPista() {
        // Mostrar el nombre de la pista
        tvNombre.setText(pistaActual.getNombre() != null ? pistaActual.getNombre() : "Pista");
        
        // Mostrar el tipo de deporte
        tvDeporte.setText("Deporte: " + (pistaActual.getDeporte() != null ? pistaActual.getDeporte() : "-"));

        // Cargar la imagen de la pista usando Glide
        String fotoUrl = pistaActual.getFotoUrl();
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            // Construir la URL completa de la imagen
            String fullUrl = ApiService.IMAGES_BASE_URL + (fotoUrl.startsWith("/") ? fotoUrl : "/" + fotoUrl);
            Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.placeholder_pista)
                    .error(R.drawable.error_pista)
                    .into(ivDetallePista);
        } else {
            // Si no hay URL, mostrar imagen placeholder
            ivDetallePista.setImageResource(R.drawable.placeholder_pista);
        }
        
        // Mostrar el estado de la pista
        tvEstado.setText("Estado: " + (pistaActual.getEstado() != null ? pistaActual.getEstado() : "-"));
        
        // Mostrar la fecha seleccionada
        tvFechaSeleccionada.setText("Fecha: " + fechaSeleccionada.format(DATE_FMT));
    }

    /**
     * Prepara el GridLayout con botones para cada horario disponible
     * Crea botones desde la hora de apertura hasta la hora de cierre
     */
    private void prepararGridHorarios() {
        // Limpiar vistas anteriores
        gridHorarios.removeAllViews();
        botonesHorarios.clear();
        
        // Crear un botón para cada hora disponible
        for (int hora = HORA_APERTURA; hora < HORA_CIERRE; hora++) {
            Button btn = new Button(this);
            btn.setText(String.format("%02d:00", hora));
            btn.setTag(hora);

            // Configurar el layout del botón en el GridLayout
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(4, 4, 4, 4);
            btn.setLayoutParams(params);

            // Configurar listener para marcar la hora seleccionada
            final int horaFinal = hora;
            btn.setOnClickListener(v -> marcarHoraSeleccionada(horaFinal));

            // Agregar el botón a la lista y al GridLayout
            botonesHorarios.add(btn);
            gridHorarios.addView(btn);
        }
    }

    /**
     * Muestra un calendario para seleccionar la fecha
     * Al seleccionar una fecha, actualiza la fecha seleccionada y recarga los horarios
     */
    private void mostrarCalendario() {
        // Crear DatePickerDialog con la fecha actual seleccionada
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            // Actualizar la fecha seleccionada
            fechaSeleccionada = LocalDate.of(year, month + 1, day);
            
            // Actualizar el TextView con la nueva fecha
            tvFechaSeleccionada.setText("Fecha: " + fechaSeleccionada.format(DATE_FMT));
            
            // Limpiar la hora seleccionada
            horaInicioSeleccionada = -1;
            
            // Recargar el estado de los botones de horarios
            refrescarEstadoBotones();
        }, fechaSeleccionada.getYear(), fechaSeleccionada.getMonthValue() - 1, fechaSeleccionada.getDayOfMonth());
        
        // Establecer la fecha mínima a ayer (no se pueden seleccionar fechas pasadas)
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        
        // Mostrar el diálogo
        dialog.show();
    }

    /**
     * Actualiza el estado de los botones de horarios
     * Deshabilita horarios pasados (si es hoy) y horarios ocupados
     * Marca en verde el horario seleccionado
     */
    private void refrescarEstadoBotones() {
        // Verificar si la fecha seleccionada es hoy
        boolean esHoy = fechaSeleccionada.equals(LocalDate.now());
        
        // Obtener la hora actual
        int horaActual = LocalTime.now().getHour();
        
        // Recalcular las horas ocupadas para la fecha seleccionada
        recalcularHorasOcupadas();
        
        // Actualizar cada botón de horario
        for (Button btn : botonesHorarios) {
            int h = (int) btn.getTag();
            
            // Verificar si la hora ya pasó (solo si es hoy)
            boolean pasada = esHoy && h <= horaActual;
            
            // Verificar si la hora está ocupada
            boolean ocupada = horasOcupadas.contains(h);
            
            if (pasada || ocupada) {
                // Deshabilitar el botón si está pasada u ocupada
                btn.setEnabled(false);
                btn.setBackgroundColor(Color.GRAY);
                btn.setAlpha(0.3f);
            } else {
                // Habilitar el botón si está disponible
                btn.setEnabled(true);
                // Marcar en verde si está seleccionado, gris claro si no
                btn.setBackgroundColor(h == horaInicioSeleccionada ? Color.GREEN : Color.LTGRAY);
                btn.setAlpha(1.0f);
            }
        }
    }

    /**
     * Recalcula las horas ocupadas para la fecha seleccionada
     * Analiza las reservas de la pista y determina qué horas están ocupadas
     */
    private void recalcularHorasOcupadas() {
        horasOcupadas.clear();
        
        // Analizar cada reserva de la pista
        for (Reserva r : reservasPista) {
            if (r == null) continue;
            
            // Ignorar reservas canceladas
            if (r.getEstadoReserva() != null && r.getEstadoReserva().equalsIgnoreCase("CANCELADA")) continue;
            
            LocalDateTime fecha = r.getFecha();
            if (fecha == null) continue;
            
            // Ignorar reservas de otras fechas
            if (!fecha.toLocalDate().equals(fechaSeleccionada)) continue;
            
            // Intentar deducir la hora desde horaInicio ("HH:mm") o desde fecha
            int hora = -1;
            String hi = r.getHoraInicio();
            if (hi != null && hi.length() >= 2) {
                try { hora = Integer.parseInt(hi.substring(0, 2)); } catch (NumberFormatException ignored) {}
            }
            if (hora == -1) hora = fecha.getHour();
            
            horasOcupadas.add(hora);
        }
    }

    /**
     * Carga las reservas de la pista desde el backend
     * Realiza una petición GET al backend para obtener las reservas de la pista
     */
    private void cargarReservasPista() {
        if (pistaActual == null || pistaActual.getIdPista() == null) return;
        
        // Obtener el token JWT
        String token = new TokenManager(this).getToken();
        
        // Realizar petición asíncrona al backend
        RetrofitClient.getApiService(token).getReservasPista(pistaActual.getIdPista())
                .enqueue(new Callback<List<Reserva>>() {
                    @Override
                    public void onResponse(Call<List<Reserva>> call, Response<List<Reserva>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Actualizar la lista de reservas
                            reservasPista = response.body();
                            
                            // Recargar el estado de los botones de horarios
                            refrescarEstadoBotones();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Reserva>> call, Throwable t) {
                        Toast.makeText(DetallePistaActivity.this, "No se pudieron cargar reservas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Marca una hora como seleccionada
     * Actualiza la variable horaInicioSeleccionada y refresca los botones
     * 
     * @param hora Hora seleccionada por el usuario
     */
    private void marcarHoraSeleccionada(int hora) {
        horaInicioSeleccionada = hora;
        refrescarEstadoBotones();
    }

    /**
     * Procede con el proceso de reserva
     * Valida que se haya seleccionado un horario y navega a CheckoutActivity
     */
    private void proceder() {
        // Validar que se haya seleccionado un horario
        if (horaInicioSeleccionada == -1) {
            Toast.makeText(this, "Elige un horario primero", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validar que el horario no esté ocupado
        if (horasOcupadas.contains(horaInicioSeleccionada)) {
            Toast.makeText(this, "Ese horario ya está ocupado", Toast.LENGTH_SHORT).show();
            horaInicioSeleccionada = -1;
            refrescarEstadoBotones();
            return;
        }
        
        // Navegar a CheckoutActivity para confirmar la reserva
        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra(CheckoutActivity.EXTRA_PISTA, pistaActual);
        intent.putExtra(CheckoutActivity.EXTRA_FECHA, fechaSeleccionada.format(DATE_FMT));
        intent.putExtra(CheckoutActivity.EXTRA_HORA_INICIO, horaInicioSeleccionada);
        startActivity(intent);
    }
}
