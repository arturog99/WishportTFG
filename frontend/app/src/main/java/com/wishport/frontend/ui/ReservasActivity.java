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
import com.wishport.frontend.models.Reserva;
import com.wishport.frontend.ui.adapters.ReservaAdapter;
import com.wishport.frontend.utils.TokenManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity de reservas del usuario
 * Muestra la lista de reservas realizadas por el usuario actual
 * Permite ver el detalle de cada reserva y navegar a otras pantallas
 */
public class ReservasActivity extends AppCompatActivity {
    /**
     * RecyclerView para mostrar la lista de reservas en formato lista
     */
    private RecyclerView rvReservas;
    
    /**
     * Botón para ir a la pantalla de pistas
     */
    private Button btnPistas;
    
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
     * Adapter para mostrar las reservas en el RecyclerView
     */
    private ReservaAdapter reservaAdapter;

    /**
     * Método llamado al crear la Activity
     * Inicializa los componentes de la UI, configura el RecyclerView y carga las reservas
     * 
     * @param savedInstanceState Bundle con el estado guardado de la Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservas);

        // Inicializar el gestor de tokens
        tokenManager = new TokenManager(this);
        
        // Encontrar referencias a los elementos de la UI
        rvReservas = findViewById(R.id.rvReservas);
        btnPistas = findViewById(R.id.btnPistas);
        btnPerfil = findViewById(R.id.btnPerfil);
        btnLogout = findViewById(R.id.btnLogout);

        // Configurar el RecyclerView
        // LinearLayoutManager organiza los elementos en una lista vertical
        rvReservas.setLayoutManager(new LinearLayoutManager(this));
        
        // Crear el adapter con una lista vacía
        reservaAdapter = new ReservaAdapter(new java.util.ArrayList<>());
        
        // Configurar el listener para cuando se hace clic en una reserva
        reservaAdapter.setOnReservaClickListener(this::abrirDetalleReserva);
        
        // Asignar el adapter al RecyclerView
        rvReservas.setAdapter(reservaAdapter);

        // Configurar listener del botón de pistas
        btnPistas.setOnClickListener(v -> {
            Intent intent = new Intent(this, PistasActivity.class);
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

        // Cargar las reservas del usuario desde el backend
        cargarReservas();
    }

    /**
     * Método para cargar las reservas del usuario desde el backend
     * Realiza una petición GET al backend para obtener todas las reservas del usuario actual
     */
    private void cargarReservas() {
        // Obtener el token JWT y el ID del usuario
        String token = tokenManager.getToken();
        Integer userId = tokenManager.getUserId();
        
        // Obtener instancia de ApiService con el token de autenticación
        ApiService apiService = RetrofitClient.getApiService(token);
        
        // Realizar petición asíncrona al backend para obtener las reservas del usuario
        apiService.getReservasUsuario(userId).enqueue(new Callback<List<Reserva>>() {
            /**
             * Método llamado cuando la petición al backend tiene respuesta
             * Procesa la respuesta y actualiza el RecyclerView con las reservas
             * 
             * @param call Objeto Call que representa la petición
             * @param response Respuesta del servidor con la lista de reservas
             */
            @Override
            public void onResponse(Call<List<Reserva>> call, Response<List<Reserva>> response) {
                // Verificar que la petición fue exitosa (código 200-299)
                if (response.isSuccessful() && response.body() != null) {
                    // Actualizar el adapter con la lista de reservas recibida
                    reservaAdapter.actualizarLista(response.body());
                } else {
                    // La petición no fue exitosa (código 400-599)
                    Toast.makeText(ReservasActivity.this, "Error al cargar reservas", Toast.LENGTH_SHORT).show();
                }
            }

            /**
             * Método llamado cuando la petición falla (error de red, timeout, etc.)
             * 
             * @param call Objeto Call que representa la petición
             * @param t Throwable con el error ocurrido
             */
            @Override
            public void onFailure(Call<List<Reserva>> call, Throwable t) {
                // Mostrar error de conexión al usuario
                Toast.makeText(ReservasActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Método para abrir el detalle de una reserva específica
     * Navega a DetalleReservaActivity pasando la reserva seleccionada
     * 
     * @param reserva Objeto Reserva seleccionada por el usuario
     */
    private void abrirDetalleReserva(Reserva reserva) {
        // Crear Intent para navegar a DetalleReservaActivity
        Intent intent = new Intent(this, DetalleReservaActivity.class);
        
        // Pasar el objeto Reserva como extra del Intent
        intent.putExtra(DetalleReservaActivity.EXTRA_RESERVA, reserva);
        
        // Iniciar la Activity de detalle
        startActivity(intent);
    }
}
