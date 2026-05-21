package com.wishport.frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wishport.frontend.R;
import com.wishport.frontend.api.ApiService;
import com.wishport.frontend.api.RetrofitClient;
import com.wishport.frontend.models.Pista;
import com.wishport.frontend.ui.adapters.PistaAdapter;
import com.wishport.frontend.utils.TokenManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity principal de pistas deportivas
 * Muestra la lista de pistas disponibles para reservar
 * Permite navegar a reservas, perfil y logout
 */
public class PistasActivity extends AppCompatActivity {
    /**
     * RecyclerView para mostrar la lista de pistas en formato lista
     */
    private RecyclerView rvPistas;
    
    /**
     * Botón para ir a la pantalla de reservas del usuario
     */
    private Button btnReservas;
    
    /**
     * Botón para ir al perfil del usuario
     */
    private Button btnPerfil;
    
    /**
     * Botón para cerrar sesión (logout)
     */
    private Button btnLogout;
    
    /**
     * Gestor de tokens JWT para autenticación
     */
    private TokenManager tokenManager;
    
    /**
     * Adapter para mostrar las pistas en el RecyclerView
     */
    private PistaAdapter pistaAdapter;

    /**
     * Método llamado al crear la Activity
     * Inicializa los componentes de la UI, configura el RecyclerView y carga las pistas
     * 
     * @param savedInstanceState Bundle con el estado guardado de la Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pistas);

        // Inicializar el gestor de tokens
        tokenManager = new TokenManager(this);
        
        // Encontrar referencias a los elementos de la UI
        rvPistas = findViewById(R.id.rvPistas);
        btnReservas = findViewById(R.id.btnReservas);
        btnPerfil = findViewById(R.id.btnPerfil);
        btnLogout = findViewById(R.id.btnLogout);

        // Configurar el RecyclerView
        // LinearLayoutManager organiza los elementos en una lista vertical
        rvPistas.setLayoutManager(new LinearLayoutManager(this));
        
        // Crear el adapter con una lista vacía
        pistaAdapter = new PistaAdapter(new ArrayList<>());
        
        // Configurar el listener para cuando se hace clic en una pista
        pistaAdapter.setOnPistaClickListener(this::abrirDetallePista);
        
        // Asignar el adapter al RecyclerView
        rvPistas.setAdapter(pistaAdapter);

        // Configurar listener del botón de reservas
        btnReservas.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReservasActivity.class);
            startActivity(intent);
        });

        // Configurar listener del botón de perfil
        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(this, PerfilActivity.class);
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

        // Cargar las pistas desde el backend
        cargarPistas();
    }

    /**
     * Método para cargar las pistas desde el backend
     * Realiza una petición GET al backend para obtener todas las pistas disponibles
     */
    private void cargarPistas() {
        // Obtener el token JWT almacenado
        String token = tokenManager.getToken();
        
        // Obtener instancia de ApiService con el token de autenticación
        ApiService apiService = RetrofitClient.getApiService(token);
        
        // Realizar petición asíncrona al backend para obtener las pistas
        apiService.getPistas().enqueue(new Callback<List<Pista>>() {
            /**
             * Método llamado cuando la petición al backend tiene respuesta
             * Procesa la respuesta y actualiza el RecyclerView con las pistas
             * 
             * @param call Objeto Call que representa la petición
             * @param response Respuesta del servidor con la lista de pistas
             */
            @Override
            public void onResponse(Call<List<Pista>> call, Response<List<Pista>> response) {
                // Verificar que la petición fue exitosa (código 200-299)
                if (response.isSuccessful() && response.body() != null) {
                    // Actualizar el adapter con la lista de pistas recibida
                    pistaAdapter.actualizarLista(response.body());
                    
                    // Mostrar mensaje si no hay pistas disponibles
                    if (response.body().isEmpty()) {
                        Toast.makeText(PistasActivity.this, "No hay pistas disponibles", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // La petición no fue exitosa (código 400-599)
                    Toast.makeText(PistasActivity.this, "Error al cargar pistas (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            /**
             * Método llamado cuando la petición falla (error de red, timeout, etc.)
             * 
             * @param call Objeto Call que representa la petición
             * @param t Throwable con el error ocurrido
             */
            @Override
            public void onFailure(Call<List<Pista>> call, Throwable t) {
                // Mostrar error de conexión al usuario
                Toast.makeText(PistasActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                
                // Imprimir el error en el log para debugging
                t.printStackTrace();
            }
        });
    }

    /**
     * Método para abrir el detalle de una pista específica
     * Navega a DetallePistaActivity pasando la pista seleccionada
     * 
     * @param pista Objeto Pista seleccionada por el usuario
     */
    private void abrirDetallePista(Pista pista) {
        // Crear Intent para navegar a DetallePistaActivity
        Intent intent = new Intent(this, DetallePistaActivity.class);
        
        // Pasar el objeto Pista como extra del Intent
        intent.putExtra(DetallePistaActivity.EXTRA_PISTA, pista);
        
        // Iniciar la Activity de detalle
        startActivity(intent);
    }
}
