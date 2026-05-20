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
import com.wishport.frontend.adapters.ReservaAdapter;
import com.wishport.frontend.api.RetrofitClient;
import com.wishport.frontend.utils.TokenManager;
import com.wishport.frontend.models.Reserva;
import com.wishport.frontend.models.Usuario;

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

        validarAccesoAdmin();

        vincularVistas();

        recyclerViewReservas.setLayoutManager(new LinearLayoutManager(this));
        reservaAdapter = new ReservaAdapter(new ArrayList<>());
        recyclerViewReservas.setAdapter(reservaAdapter);

        findViewById(R.id.btnLogout).setOnClickListener(v -> hacerLogout());
        findViewById(R.id.btnCrearAdmin).setOnClickListener(v -> mostrarDialogoCrearAdmin());
        findViewById(R.id.btnEscanearQr).setOnClickListener(v -> solicitarPermisoCamara());

        cargarReservasDelDia();
    }

    private void validarAccesoAdmin() {
        SharedPreferences prefs = getSharedPreferences("WishPortPrefs", MODE_PRIVATE);
        String rol = prefs.getString("rolUsuario", "");
        if (!"ADMIN".equals(rol)) {
            Toast.makeText(this, "Acceso denegado: No eres administrador", Toast.LENGTH_SHORT).show();
            finish();
        }
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
                    reservasHoy.addAll(response.body());
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
        getSharedPreferences("WishPortPrefs", MODE_PRIVATE).edit().clear().apply();
        tokenManager.clearToken();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void mostrarDialogoCrearAdmin() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText etNombre = new EditText(this);
        etNombre.setHint("Nombre completo");
        etNombre.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        layout.addView(etNombre);

        EditText etEmail = new EditText(this);
        etEmail.setHint("Email");
        etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(etEmail);

        EditText etPassword = new EditText(this);
        etPassword.setHint("Contraseña");
        etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPassword);

        EditText etTelefono = new EditText(this);
        etTelefono.setHint("Teléfono");
        etTelefono.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
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
                return;
            }
            if (email.isEmpty()) {
                etEmail.setError("El email es obligatorio");
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("La contraseña es obligatoria");
                return;
            }
            if (telefono.isEmpty()) {
                etTelefono.setError("El teléfono es obligatorio");
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
        RetrofitClient.getApiService(token).crearAdmin(usuario).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AdminActivity.this,
                            "Administrador creado: " + response.body().getNombre(),
                            Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                } else if (response.code() == 403) {
                    Toast.makeText(AdminActivity.this,
                            "No tienes permisos para crear administradores",
                            Toast.LENGTH_LONG).show();
                } else if (response.code() == 409) {
                    Toast.makeText(AdminActivity.this,
                            "Este email ya está registrado",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AdminActivity.this,
                            "Error al crear administrador: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                Toast.makeText(AdminActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
