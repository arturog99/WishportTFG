package com.wishport.frontend.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.wishport.frontend.R;
import com.wishport.frontend.api.ApiService;
import com.wishport.frontend.api.RetrofitClient;
import com.wishport.frontend.models.Usuario;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroActivity extends AppCompatActivity {
    private EditText etNombre, etEmail, etPassword, etTelefono;
    private Button btnRegistro, btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etTelefono = findViewById(R.id.etTelefono);
        btnRegistro = findViewById(R.id.btnRegistro);
        btnVolver = findViewById(R.id.btnVolver);

        btnRegistro.setOnClickListener(v -> registrar());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void registrar() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String telefono = etTelefono.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (nombre.length() < 2) {
            etNombre.setError("Nombre demasiado corto");
            etNombre.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email no válido");
            etEmail.requestFocus();
            return;
        }
        if (password.length() < 4) {
            etPassword.setError("Mínimo 4 caracteres");
            etPassword.requestFocus();
            return;
        }
        if (telefono.length() < 9 || !telefono.matches("\\+?\\d{9,15}")) {
            etTelefono.setError("Teléfono no válido");
            etTelefono.requestFocus();
            return;
        }

        btnRegistro.setEnabled(false);

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(password);
        usuario.setTelefono(telefono);

        ApiService apiService = RetrofitClient.getApiService(null);
        apiService.register(usuario).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegistroActivity.this, "Registro exitoso \u2713", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnRegistro.setEnabled(true);
                    if (response.code() == 400) {
                        Toast.makeText(RegistroActivity.this, "Email ya registrado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegistroActivity.this, "Error al registrar (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                btnRegistro.setEnabled(true);
                Toast.makeText(RegistroActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
