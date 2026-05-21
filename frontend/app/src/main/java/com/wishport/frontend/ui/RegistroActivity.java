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

/**
 * Activity de registro de nuevos usuarios
 * Permite a los usuarios crear una cuenta en la aplicación
 * Valida los datos y envía la petición de registro al backend
 */
public class RegistroActivity extends AppCompatActivity {
    /**
     * Campo de texto para ingresar el nombre del usuario
     */
    private EditText etNombre;
    
    /**
     * Campo de texto para ingresar el email del usuario
     */
    private EditText etEmail;
    
    /**
     * Campo de texto para ingresar la contraseña del usuario
     */
    private EditText etPassword;
    
    /**
     * Campo de texto para ingresar el teléfono del usuario
     */
    private EditText etTelefono;
    
    /**
     * Botón para completar el registro
     */
    private Button btnRegistro;
    
    /**
     * Botón para volver a la pantalla de login
     */
    private Button btnVolver;

    /**
     * Método llamado al crear la Activity
     * Inicializa los componentes de la UI y configura los listeners
     * 
     * @param savedInstanceState Bundle con el estado guardado de la Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Encontrar referencias a los elementos de la UI
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etTelefono = findViewById(R.id.etTelefono);
        btnRegistro = findViewById(R.id.btnRegistro);
        btnVolver = findViewById(R.id.btnVolver);

        // Configurar listener del botón de registro
        btnRegistro.setOnClickListener(v -> registrar());
        
        // Configurar listener del botón de volver
        btnVolver.setOnClickListener(v -> finish());
    }

    /**
     * Método para procesar el registro de un nuevo usuario
     * Valida los campos, realiza la petición al backend y maneja la respuesta
     * 
     * Proceso:
     * 1. Valida que los campos no estén vacíos
     * 2. Valida la longitud del nombre
     * 3. Valida el formato del email
     * 4. Valida la longitud de la contraseña
     * 5. Valida el formato del teléfono
     * 6. Deshabilita el botón para evitar múltiples peticiones
     * 7. Realiza petición POST al backend con los datos del usuario
     * 8. Si es exitoso, muestra mensaje y vuelve al login
     * 9. Si falla, muestra error y reactiva el botón
     */
    private void registrar() {
        // Obtener los valores de los campos de texto
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String telefono = etTelefono.getText().toString().trim();

        // Validar que los campos no estén vacíos
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validar que el nombre tenga al menos 2 caracteres
        if (nombre.length() < 2) {
            etNombre.setError("Nombre demasiado corto");
            etNombre.requestFocus();
            return;
        }
        
        // Validar el formato del email usando patrón de Android
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email no válido");
            etEmail.requestFocus();
            return;
        }
        
        // Validar que la contraseña tenga al menos 4 caracteres
        if (password.length() < 4) {
            etPassword.setError("Mínimo 4 caracteres");
            etPassword.requestFocus();
            return;
        }
        
        // Validar el formato del teléfono (9-15 dígitos, opcionalmente con +)
        if (telefono.length() < 9 || !telefono.matches("\\+?\\d{9,15}")) {
            etTelefono.setError("Teléfono no válido");
            etTelefono.requestFocus();
            return;
        }

        // Deshabilitar el botón para evitar múltiples peticiones simultáneas
        btnRegistro.setEnabled(false);

        // Crear objeto Usuario con los datos del formulario
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(password);
        usuario.setTelefono(telefono);

        // Obtener instancia de ApiService para realizar la petición
        // No se pasa token porque el endpoint de registro es público
        ApiService apiService = RetrofitClient.getApiService(null);
        
        // Realizar petición asíncrona al backend
        apiService.register(usuario).enqueue(new Callback<Usuario>() {
            /**
             * Método llamado cuando la petición al backend tiene respuesta
             * Procesa la respuesta del servidor y maneja el éxito o error
             * 
             * @param call Objeto Call que representa la petición
             * @param response Respuesta del servidor con los datos
             */
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                // Verificar que la petición fue exitosa (código 200-299)
                if (response.isSuccessful()) {
                    // Registro exitoso
                    Toast.makeText(RegistroActivity.this, "Registro exitoso \u2713", Toast.LENGTH_SHORT).show();
                    
                    // Volver a la pantalla de login
                    finish();
                } else {
                    // La petición no fue exitosa (código 400-599)
                    btnRegistro.setEnabled(true);
                    
                    // Manejar diferentes tipos de error según el código HTTP
                    if (response.code() == 400) {
                        // Email ya registrado
                        Toast.makeText(RegistroActivity.this, "Email ya registrado", Toast.LENGTH_SHORT).show();
                    } else {
                        // Otro error del servidor
                        Toast.makeText(RegistroActivity.this, "Error al registrar (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            /**
             * Método llamado cuando la petición falla (error de red, timeout, etc.)
             * 
             * @param call Objeto Call que representa la petición
             * @param t Throwable con el error ocurrido
             */
            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                // Reactivar el botón de registro
                btnRegistro.setEnabled(true);
                
                // Mostrar error de conexión al usuario
                Toast.makeText(RegistroActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
