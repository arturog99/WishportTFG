package com.wishport.frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.wishport.frontend.R;
import com.wishport.frontend.api.ApiService;
import com.wishport.frontend.api.RetrofitClient;
import com.wishport.frontend.models.Usuario;
import com.wishport.frontend.utils.TokenManager;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity de login (inicio de sesión)
 * Pantalla principal donde los usuarios se autentican para acceder a la aplicación
 * Valida credenciales y redirige al usuario según su rol (ADMIN o USER)
 */
public class LoginActivity extends AppCompatActivity {
    /**
     * Campo de texto para ingresar el email del usuario
     */
    private EditText etEmail;
    
    /**
     * Campo de texto para ingresar la contraseña del usuario
     */
    private EditText etPassword;
    
    /**
     * Botón para iniciar sesión
     */
    private Button btnLogin;
    
    /**
     * Botón para ir a la pantalla de registro
     */
    private Button btnRegistro;
    
    /**
     * Gestor de tokens JWT para autenticación
     * Almacena y recupera el token de autenticación
     */
    private TokenManager tokenManager;

    /**
     * Método llamado al crear la Activity
     * Inicializa los componentes de la UI y configura los listeners
     * 
     * @param savedInstanceState Bundle con el estado guardado de la Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar el gestor de tokens
        tokenManager = new TokenManager(this);

        // Encontrar referencias a los elementos de la UI
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegistro = findViewById(R.id.btnRegistro);

        // Configurar listener del botón de login
        btnLogin.setOnClickListener(v -> login());
        
        // Configurar listener del botón de registro
        btnRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistroActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Método para procesar el inicio de sesión
     * Valida los campos, realiza la petición al backend y maneja la respuesta
     * 
     * Proceso:
     * 1. Valida que los campos no estén vacíos
     * 2. Valida el formato del email
     * 3. Valida la longitud de la contraseña
     * 4. Deshabilita el botón de login para evitar múltiples peticiones
     * 5. Realiza petición POST al backend con las credenciales
     * 6. Si es exitoso, guarda el token y redirige según el rol
     * 7. Si falla, muestra error y reactiva el botón
     */
    private void login() {
        // Obtener los valores de los campos de texto
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Validar que los campos no estén vacíos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
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
            etPassword.setError("Contraseña demasiado corta");
            etPassword.requestFocus();
            return;
        }

        // Deshabilitar el botón para evitar múltiples peticiones simultáneas
        btnLogin.setEnabled(false);
        
        // Crear objeto Usuario con las credenciales
        Usuario credenciales = new Usuario();
        credenciales.setEmail(email);
        credenciales.setPassword(password);

        // Obtener instancia de ApiService para realizar la petición
        // No se pasa token porque el endpoint de login es público
        ApiService apiService = RetrofitClient.getApiService(null);
        
        // Realizar petición asíncrona al backend
        apiService.login(credenciales).enqueue(new Callback<Map<String, Object>>() {
            /**
             * Método llamado cuando la petición al backend tiene respuesta
             * Procesa la respuesta del servidor y maneja el éxito o error
             * 
             * @param call Objeto Call que representa la petición
             * @param response Respuesta del servidor con los datos
             */
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                try {
                    // Verificar que la petición fue exitosa (código 200-299)
                    if (response.isSuccessful() && response.body() != null) {
                        // Extraer el cuerpo de la respuesta
                        Map<String, Object> body = response.body();
                        
                        // Extraer el token JWT de la respuesta
                        String token = (String) body.get("token");

                        // Extraer el ID del usuario de la respuesta
                        // Se maneja como Number para compatibilidad con diferentes tipos numéricos
                        Object userIdObj = body.get("idUsuario");
                        Integer userId = (userIdObj instanceof Number) ? ((Number) userIdObj).intValue() : -1;

                        // Extraer el rol del usuario de la respuesta
                        String rol = (String) body.get("rol");
                        if (rol == null) rol = "";

                        // Validar que el token no sea null
                        if (token == null) {
                            Toast.makeText(LoginActivity.this, "Respuesta inválida del servidor", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Guardar el token JWT en SharedPreferences
                        tokenManager.saveToken(token);
                        
                        // Guardar el ID del usuario en SharedPreferences
                        tokenManager.saveUserId(userId);
                        
                        // Guardar el rol del usuario en SharedPreferences
                        getSharedPreferences("WishPortPrefs", MODE_PRIVATE)
                                .edit().putString("rolUsuario", rol).apply();

                        // Redirigir al usuario según su rol
                        // Si es ADMIN → AdminActivity, si es USER → PistasActivity
                        Class<?> destino = "ADMIN".equals(rol) ? AdminActivity.class : PistasActivity.class;
                        Intent intent = new Intent(LoginActivity.this, destino);
                        startActivity(intent);
                        
                        // Finalizar esta Activity para que el usuario no pueda volver atrás
                        finish();
                    } else {
                        // La petición no fue exitosa (código 400-599)
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Credenciales incorrectas (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    // Error al procesar la respuesta
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Error procesando respuesta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            /**
             * Método llamado cuando la petición falla (error de red, timeout, etc.)
             * 
             * @param call Objeto Call que representa la petición
             * @param t Throwable con el error ocurrido
             */
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // Reactivar el botón de login
                btnLogin.setEnabled(true);
                
                // Mostrar error de conexión al usuario
                Toast.makeText(LoginActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                
                // Imprimir el error en el log para debugging
                t.printStackTrace();
            }
        });
    }
}
