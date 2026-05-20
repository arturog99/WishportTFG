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
import com.wishport.frontend.models.Pista;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

        btnSeleccionarFecha.setOnClickListener(v -> mostrarCalendario());
        btnReservar.setOnClickListener(v -> proceder());
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
        for (Button btn : botonesHorarios) {
            int h = (int) btn.getTag();
            boolean pasada = esHoy && h <= horaActual;
            if (pasada) {
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

    private void marcarHoraSeleccionada(int hora) {
        horaInicioSeleccionada = hora;
        refrescarEstadoBotones();
    }

    private void proceder() {
        if (horaInicioSeleccionada == -1) {
            Toast.makeText(this, "Elige un horario primero", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra(CheckoutActivity.EXTRA_PISTA, pistaActual);
        intent.putExtra(CheckoutActivity.EXTRA_FECHA, fechaSeleccionada.format(DATE_FMT));
        intent.putExtra(CheckoutActivity.EXTRA_HORA_INICIO, horaInicioSeleccionada);
        startActivity(intent);
    }
}
