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

import com.wishport.frontend.R;
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
 * Pantalla de detalle de pista: muestra info, permite seleccionar fecha y hora.
 * Versión simplificada: las horas pasadas (si la fecha es hoy) aparecen deshabilitadas.
 * No comprueba ocupación contra el servidor; el backend rechaza colisiones al crear.
 */
public class DetallePistaActivity extends AppCompatActivity {

    public static final String EXTRA_PISTA = "extra_pista";
    private static final int HORA_APERTURA = 8;
    private static final int HORA_CIERRE = 22;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private TextView tvNombre, tvDeporte, tvEstado, tvFechaSeleccionada;
    private Button btnSeleccionarFecha, btnReservar;
    private GridLayout gridHorarios;

    private Pista pistaActual;
    private LocalDate fechaSeleccionada;
    private int horaInicioSeleccionada = -1;
    private final List<Button> botonesHorarios = new ArrayList<>();
    private final Set<Integer> horasOcupadas = new HashSet<>();
    private List<Reserva> reservasPista = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_pista);

        tvNombre = findViewById(R.id.tvDetalleNombre);
        tvDeporte = findViewById(R.id.tvDetalleDeporte);
        tvEstado = findViewById(R.id.tvDetalleEstado);
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada);
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha);
        btnReservar = findViewById(R.id.btnReservar);
        gridHorarios = findViewById(R.id.gridHorarios);

        pistaActual = (Pista) getIntent().getSerializableExtra(EXTRA_PISTA);
        fechaSeleccionada = LocalDate.now();

        if (pistaActual == null) {
            Toast.makeText(this, "Error: pista no recibida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mostrarInfoPista();
        prepararGridHorarios();
        refrescarEstadoBotones();
        cargarReservasPista();

        btnSeleccionarFecha.setOnClickListener(v -> mostrarCalendario());
        btnReservar.setOnClickListener(v -> proceder());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarReservasPista();
    }

    private void mostrarInfoPista() {
        tvNombre.setText(pistaActual.getNombre() != null ? pistaActual.getNombre() : "Pista");
        tvDeporte.setText("Deporte: " + (pistaActual.getDeporte() != null ? pistaActual.getDeporte() : "-"));
        tvEstado.setText("Estado: " + (pistaActual.getEstado() != null ? pistaActual.getEstado() : "-"));
        tvFechaSeleccionada.setText("Fecha: " + fechaSeleccionada.format(DATE_FMT));
    }

    private void prepararGridHorarios() {
        gridHorarios.removeAllViews();
        botonesHorarios.clear();
        for (int hora = HORA_APERTURA; hora < HORA_CIERRE; hora++) {
            Button btn = new Button(this);
            btn.setText(String.format("%02d:00", hora));
            btn.setTag(hora);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(4, 4, 4, 4);
            btn.setLayoutParams(params);

            final int horaFinal = hora;
            btn.setOnClickListener(v -> marcarHoraSeleccionada(horaFinal));

            botonesHorarios.add(btn);
            gridHorarios.addView(btn);
        }
    }

    private void mostrarCalendario() {
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            fechaSeleccionada = LocalDate.of(year, month + 1, day);
            tvFechaSeleccionada.setText("Fecha: " + fechaSeleccionada.format(DATE_FMT));
            horaInicioSeleccionada = -1;
            refrescarEstadoBotones();
        }, fechaSeleccionada.getYear(), fechaSeleccionada.getMonthValue() - 1, fechaSeleccionada.getDayOfMonth());
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void refrescarEstadoBotones() {
        boolean esHoy = fechaSeleccionada.equals(LocalDate.now());
        int horaActual = LocalTime.now().getHour();
        recalcularHorasOcupadas();
        for (Button btn : botonesHorarios) {
            int h = (int) btn.getTag();
            boolean pasada = esHoy && h <= horaActual;
            boolean ocupada = horasOcupadas.contains(h);
            if (pasada || ocupada) {
                btn.setEnabled(false);
                btn.setBackgroundColor(Color.GRAY);
                btn.setAlpha(0.3f);
            } else {
                btn.setEnabled(true);
                btn.setBackgroundColor(h == horaInicioSeleccionada ? Color.GREEN : Color.LTGRAY);
                btn.setAlpha(1.0f);
            }
        }
    }

    private void recalcularHorasOcupadas() {
        horasOcupadas.clear();
        for (Reserva r : reservasPista) {
            if (r == null) continue;
            // Filtrar canceladas
            if (r.getEstadoReserva() != null && r.getEstadoReserva().equalsIgnoreCase("CANCELADA")) continue;
            LocalDateTime fecha = r.getFecha();
            if (fecha == null) continue;
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

    private void cargarReservasPista() {
        if (pistaActual == null || pistaActual.getIdPista() == null) return;
        String token = new TokenManager(this).getToken();
        RetrofitClient.getApiService(token).getReservasPista(pistaActual.getIdPista())
                .enqueue(new Callback<List<Reserva>>() {
                    @Override
                    public void onResponse(Call<List<Reserva>> call, Response<List<Reserva>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            reservasPista = response.body();
                            refrescarEstadoBotones();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Reserva>> call, Throwable t) {
                        Toast.makeText(DetallePistaActivity.this, "No se pudieron cargar reservas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void marcarHoraSeleccionada(int hora) {
        horaInicioSeleccionada = hora;
        refrescarEstadoBotones();
    }

    private void proceder() {
        if (horaInicioSeleccionada == -1) {
            Toast.makeText(this, "Elige un horario primero", Toast.LENGTH_SHORT).show();
            return;
        }
        if (horasOcupadas.contains(horaInicioSeleccionada)) {
            Toast.makeText(this, "Ese horario ya está ocupado", Toast.LENGTH_SHORT).show();
            horaInicioSeleccionada = -1;
            refrescarEstadoBotones();
            return;
        }
        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra(CheckoutActivity.EXTRA_PISTA, pistaActual);
        intent.putExtra(CheckoutActivity.EXTRA_FECHA, fechaSeleccionada.format(DATE_FMT));
        intent.putExtra(CheckoutActivity.EXTRA_HORA_INICIO, horaInicioSeleccionada);
        startActivity(intent);
    }
}
