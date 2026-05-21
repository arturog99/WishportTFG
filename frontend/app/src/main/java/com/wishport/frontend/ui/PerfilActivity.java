package com.wishport.frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.wishport.frontend.R;
import com.wishport.frontend.api.ApiService;
import com.wishport.frontend.api.RetrofitClient;
import com.wishport.frontend.models.Usuario;
import com.wishport.frontend.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity de perfil de usuario
 * Muestra la información del usuario actual
 * Permite navegar a otras pantallas y cerrar sesión
 */
public class PerfilActivity extends AppCompatActivity {
    /**
     * TextView para mostrar el nombre del usuario
     */
    private TextView tvNombre;
    
    /**
     * TextView para mostrar el email del usuario
     */
    private TextView tvEmail;
    
    /**
     * TextView para mostrar el teléfono del usuario
     */
    private TextView tvTelefono;
    
    /**
     * Botón para ir a la pantalla de pistas
     */
    private Button btnPistas;
    
    /**
     * Botón para ir a la pantalla de reservas
     */
    private Button btnReservas;
    
    /**
     * Botón para cerrar sesión (logout)
     */
    private Button btnLogout;
    
    /**
     * Gestor de tokens JWT para autenticación
     */
    private TokenManager tokenManager;

    /**
     * Método llamado al crear la Activity
     * Inicializa los componentes de la UI, configura los listeners y carga el perfil
     * 
     * @param savedInstanceState Bundle con el estado guardado de la Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // Inicializar el gestor de tokens
        tokenManager = new TokenManager(this);
        
        // Encontrar referencias a los elementos de la UI
        tvNombre = findViewById(R.id.tvNombre);
        tvEmail = findViewById(R.id.tvEmail);
        tvTelefono = findViewById(R.id.tvTelefono);
        btnPistas = findViewById(R.id.btnPistas);
        btnReservas = findViewById(R.id.btnReservas);
        btnLogout = findViewById(R.id.btnLogout);

        // Configurar listener del botón de pistas
        btnPistas.setOnClickListener(v -> {
            Intent intent = new Intent(this, PistasActivity.class);
            startActivity(intent);
        });

        // Configurar listener del botón de reservas
        btnReservas.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReservasActivity.class);
            startActivity(intent);
        });

        // Configurar listener del botón de logout
        btnLogout.setOnClickListener(v -> {
            // Limpiar el token JWT almacenado
            tokenManager.clearToken();
            
            // Ir a la pantalla de login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            
            // Finalizar esta Activity para que el usuario no pueda volver atrás
            finish();
        });

        // Cargar los datos del perfil del usuario
        cargarPerfil();
    }

    /**
     * Carga los datos del perfil del usuario desde el backend
     * Realiza una petición GET al backend para obtener la información del usuario actual
     */
    private void cargarPerfil() {
        // Obtener el token JWT y el ID del usuario
        String token = tokenManager.getToken();
        Integer userId = tokenManager.getUserId();
        
        // Obtener instancia de ApiService con el token de autenticación
        ApiService apiService = RetrofitClient.getApiService(token);
        
        // Realizar petición asíncrona al backend para obtener los datos del usuario
        apiService.getUsuario(userId).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                // Verificar que la petición fue exitosa
                if (response.isSuccessful() && response.body() != null) {
                    // Obtener el usuario de la respuesta
                    Usuario usuario = response.body();
                    
                    // Mostrar los datos del usuario en la UI
                    tvNombre.setText(usuario.getNombre());
                    tvEmail.setText(usuario.getEmail());
                    tvTelefono.setText(usuario.getTelefono());
                } else {
                    // La petición no fue exitosa
                    Toast.makeText(PerfilActivity.this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                // Mostrar error de conexión al usuario
                Toast.makeText(PerfilActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
