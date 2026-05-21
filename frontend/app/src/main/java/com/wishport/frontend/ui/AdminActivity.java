package com.wishport.frontend.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.wishport.frontend.R;
import com.wishport.frontend.ui.adapters.ReservaAdapter;
import com.wishport.frontend.api.RetrofitClient;
import com.wishport.frontend.utils.TokenManager;
import com.wishport.frontend.models.Reserva;
import com.wishport.frontend.models.Usuario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;

    private RecyclerView recyclerViewReservas;
    private ReservaAdapter reservaAdapter;
    private ProgressBar progressBar;
    private View emptyStateLayout;
    private TokenManager tokenManager;
    private List<Reserva> reservasHoy = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        tokenManager = new TokenManager(this);

        if (!validarAccesoAdmin()) {
            return;
        }

        vincularVistas();

        recyclerViewReservas.setLayoutManager(new LinearLayoutManager(this));
        reservaAdapter = new ReservaAdapter(new ArrayList<>());
        reservaAdapter.setOnReservaClickListener(reserva -> {
            Intent intent = new Intent(this, DetalleReservaActivity.class);
            intent.putExtra(DetalleReservaActivity.EXTRA_RESERVA, reserva);
            intent.putExtra("ES_ADMIN", true);
            startActivity(intent);
        });
        recyclerViewReservas.setAdapter(reservaAdapter);

        findViewById(R.id.btnLogout).setOnClickListener(v -> hacerLogout());
        findViewById(R.id.btnCrearAdmin).setOnClickListener(v -> mostrarDialogoCrearAdmin());
        findViewById(R.id.btnEscanearQr).setOnClickListener(v -> solicitarPermisoCamara());

        cargarReservasDelDia();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarReservasDelDia();
    }

    private boolean validarAccesoAdmin() {
        SharedPreferences prefs = getSharedPreferences("WishPortPrefs", MODE_PRIVATE);
        String rol = prefs.getString("rolUsuario", "");
        if (!"ADMIN".equals(rol)) {
            Toast.makeText(this, "Acceso denegado: No eres administrador", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        return true;
    }

    private void vincularVistas() {
        recyclerViewReservas = findViewById(R.id.recyclerViewReservasHoy);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
    }

    private void cargarReservasDelDia() {
        String token = tokenManager.getToken();
        progressBar.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);

        RetrofitClient.getApiService(token).getReservas().enqueue(new Callback<List<Reserva>>() {
            @Override
            public void onResponse(Call<List<Reserva>> call, Response<List<Reserva>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    reservasHoy.clear();
                    LocalDate hoy = LocalDate.now();
                    for (Reserva r : response.body()) {
                        if (r.getFecha() != null && r.getFecha().toLocalDate().equals(hoy)) {
                            reservasHoy.add(r);
                        }
                    }
                    actualizarInterfaz();
                } else {
                    Toast.makeText(AdminActivity.this, "Error al cargar reservas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Reserva>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarInterfaz() {
        if (reservasHoy.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerViewReservas.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerViewReservas.setVisibility(View.VISIBLE);
            reservaAdapter.actualizarLista(reservasHoy);
        }
    }

    private void solicitarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            abrirEscanner();
        }
    }

    private void abrirEscanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Enfoca el código QR del usuario");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.initiateScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirEscanner();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            validarCodigoEscaneado(result.getContents());
        }
    }

    private void validarCodigoEscaneado(String qrLeido) {
        Reserva reservaValida = null;
        for (Reserva r : reservasHoy) {
            if (qrLeido.equals(r.getCodigoQr())) {
                reservaValida = r;
                break;
            }
        }

        if (reservaValida != null) {
            String nombre = reservaValida.getIdUsuario() != null ? reservaValida.getIdUsuario().getNombre() : "Usuario";
            Toast.makeText(this, "✅ ACCESO CONCEDIDO: " + nombre, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "❌ ACCESO DENEGADO: QR inválido o de otro día", Toast.LENGTH_LONG).show();
        }
    }

    private void hacerLogout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Cerrar sesión", (dialog, which) -> {
                getSharedPreferences("WishPortPrefs", MODE_PRIVATE).edit().clear().apply();
                tokenManager.clearToken();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void mostrarDialogoCrearAdmin() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);

        EditText etNombre = new EditText(this);
        etNombre.setHint("Nombre completo");
        etNombre.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        LinearLayout.LayoutParams paramsNombre = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        paramsNombre.setMargins(0, 0, 0, 20);
        etNombre.setLayoutParams(paramsNombre);
        layout.addView(etNombre);

        EditText etEmail = new EditText(this);
        etEmail.setHint("Email");
        etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        LinearLayout.LayoutParams paramsEmail = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        paramsEmail.setMargins(0, 0, 0, 20);
        etEmail.setLayoutParams(paramsEmail);
        layout.addView(etEmail);

        EditText etPassword = new EditText(this);
        etPassword.setHint("Contraseña (mínimo 6 caracteres)");
        etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams paramsPassword = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        paramsPassword.setMargins(0, 0, 0, 20);
        etPassword.setLayoutParams(paramsPassword);
        layout.addView(etPassword);

        EditText etTelefono = new EditText(this);
        etTelefono.setHint("Teléfono");
        etTelefono.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        LinearLayout.LayoutParams paramsTelefono = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        paramsTelefono.setMargins(0, 0, 0, 0);
        etTelefono.setLayoutParams(paramsTelefono);
        layout.addView(etTelefono);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Crear Nuevo Administrador");
        builder.setView(layout);
        builder.setPositiveButton("Crear", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            String telefono = etTelefono.getText().toString().trim();

            if (nombre.isEmpty()) {
                etNombre.setError("El nombre es obligatorio");
                etNombre.requestFocus();
                return;
            }
            if (email.isEmpty()) {
                etEmail.setError("El email es obligatorio");
                etEmail.requestFocus();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Email inválido");
                etEmail.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("La contraseña es obligatoria");
                etPassword.requestFocus();
                return;
            }
            if (password.length() < 6) {
                etPassword.setError("La contraseña debe tener al menos 6 caracteres");
                etPassword.requestFocus();
                return;
            }
            if (telefono.isEmpty()) {
                etTelefono.setError("El teléfono es obligatorio");
                etTelefono.requestFocus();
                return;
            }

            Usuario nuevoAdmin = new Usuario();
            nuevoAdmin.setNombre(nombre);
            nuevoAdmin.setEmail(email);
            nuevoAdmin.setPassword(password);
            nuevoAdmin.setTelefono(telefono);
            nuevoAdmin.setRol("ADMIN");

            crearAdmin(nuevoAdmin, dialog);
        });
    }

    private void crearAdmin(Usuario usuario, AlertDialog dialog) {
        String token = tokenManager.getToken();
        progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getApiService(token).crearAdmin(usuario).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AdminActivity.this, "✅ Administrador creado exitosamente", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    cargarReservasDelDia();
                } else {
                    String errorMsg = response.code() == 403 ? 
                        "No tienes permiso para crear administradores" : 
                        "Error al crear administrador";
                    Toast.makeText(AdminActivity.this, "❌ " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminActivity.this, "❌ Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
