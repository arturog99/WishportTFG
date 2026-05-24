package com.wishport.frontend.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity de administrador
 * Permite a los usuarios con rol ADMIN gestionar las reservas del día actual
 * Funcionalidades:
 * - Ver todas las reservas del día
 * - Escanear códigos QR para validar el acceso de los usuarios
 * - Crear nuevos administradores
 * - Cerrar sesión
 */
public class AdminActivity extends AppCompatActivity {

    /**
     * Código de solicitud de permiso de cámara
     */
    private static final int CAMERA_PERMISSION_CODE = 100;

    /**
     * RecyclerView para mostrar la lista de reservas del día
     */
    private RecyclerView recyclerViewReservas;
    
    /**
     * Adapter para mostrar las reservas en el RecyclerView
     */
    private ReservaAdapter reservaAdapter;
    
    /**
     * ProgressBar para mostrar el progreso de las peticiones
     */
    private ProgressBar progressBar;
    
    /**
     * Layout de estado vacío (cuando no hay reservas)
     */
    private View emptyStateLayout;
    
    /**
     * Gestor de tokens JWT para autenticación
     */
    private TokenManager tokenManager;
    
    /**
     * Lista de reservas del día actual
     */
    private List<Reserva> reservasHoy = new ArrayList<>();

    /**
     * Método llamado al crear la Activity
     * Valida el acceso de administrador, inicializa la UI y carga las reservas
     * 
     * @param savedInstanceState Bundle con el estado guardado de la Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Inicializar el gestor de tokens
        tokenManager = new TokenManager(this);

        // Validar que el usuario tenga rol ADMIN
        if (!validarAccesoAdmin()) {
            return;
        }

        // Vincular las vistas de la UI
        vincularVistas();

        // Configurar el RecyclerView
        recyclerViewReservas.setLayoutManager(new LinearLayoutManager(this));
        reservaAdapter = new ReservaAdapter(new ArrayList<>());
        
        // Configurar listener para cuando se hace clic en una reserva
        reservaAdapter.setOnReservaClickListener(reserva -> {
            Intent intent = new Intent(this, DetalleReservaActivity.class);
            intent.putExtra(DetalleReservaActivity.EXTRA_RESERVA, reserva);
            intent.putExtra("ES_ADMIN", true);
            startActivity(intent);
        });
        recyclerViewReservas.setAdapter(reservaAdapter);

        // Configurar listener del botón de logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> hacerLogout());
        
        // Configurar listener del botón para crear admin
        findViewById(R.id.btnCrearAdmin).setOnClickListener(v -> mostrarDialogoCrearAdmin());
        
        // Configurar listener del botón para escanear QR
        findViewById(R.id.btnEscanearQr).setOnClickListener(v -> solicitarPermisoCamara());

        // Cargar las reservas del día
        cargarReservasDelDia();
    }

    /**
     * Método llamado cuando la Activity se reanuda
     * Recarga las reservas del día para mantener la información actualizada
     */
    @Override
    protected void onResume() {
        super.onResume();
        cargarReservasDelDia();
    }

    /**
     * Valida que el usuario tenga rol ADMIN
     * Si no tiene el rol, muestra un error y cierra la Activity
     * 
     * @return true si el usuario es ADMIN, false en caso contrario
     */
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

    /**
     * Vincula las vistas de la UI con las variables de la Activity
     */
    private void vincularVistas() {
        recyclerViewReservas = findViewById(R.id.recyclerViewReservasHoy);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
    }

    /**
     * Carga todas las reservas del backend y filtra las del día actual
     * Realiza una petición GET al backend para obtener todas las reservas
     */
    private void cargarReservasDelDia() {
        // Obtener el token JWT
        String token = tokenManager.getToken();
        
        // Mostrar el ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        
        // Ocultar el estado vacío
        emptyStateLayout.setVisibility(View.GONE);

        // Realizar petición asíncrona al backend para obtener todas las reservas
        RetrofitClient.getApiService(token).getReservas().enqueue(new Callback<List<Reserva>>() {
            @Override
            public void onResponse(Call<List<Reserva>> call, Response<List<Reserva>> response) {
                // Ocultar el ProgressBar
                progressBar.setVisibility(View.GONE);
                
                // Verificar que la petición fue exitosa
                if (response.isSuccessful() && response.body() != null) {
                    // Limpiar la lista de reservas del día
                    reservasHoy.clear();
                    
                    // Obtener la fecha actual
                    LocalDate hoy = LocalDate.now();
                    
                    // Filtrar las reservas para obtener solo las del día actual
                    for (Reserva r : response.body()) {
                        if (r.getFecha() != null && r.getFecha().toLocalDate().equals(hoy)) {
                            reservasHoy.add(r);
                        }
                    }
                    
                    // Actualizar la interfaz con las reservas filtradas
                    actualizarInterfaz();
                } else {
                    // La petición no fue exitosa
                    Toast.makeText(AdminActivity.this, "Error al cargar reservas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Reserva>> call, Throwable t) {
                // Ocultar el ProgressBar
                progressBar.setVisibility(View.GONE);
                
                // Mostrar error de conexión al usuario
                Toast.makeText(AdminActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Actualiza la interfaz según si hay reservas o no
     * Muestra el estado vacío si no hay reservas, o el RecyclerView si hay
     */
    private void actualizarInterfaz() {
        if (reservasHoy.isEmpty()) {
            // No hay reservas: mostrar estado vacío
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerViewReservas.setVisibility(View.GONE);
        } else {
            // Hay reservas: mostrar el RecyclerView
            emptyStateLayout.setVisibility(View.GONE);
            recyclerViewReservas.setVisibility(View.VISIBLE);
            reservaAdapter.actualizarLista(reservasHoy);
        }
    }

    /**
     * Solicita el permiso de cámara al usuario
     * Si ya tiene el permiso, abre el escáner de QR directamente
     */
    private void solicitarPermisoCamara() {
        Log.d("AdminActivity", "solicitarPermisoCamara llamado");
        // Verificar si ya se tiene el permiso de cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d("AdminActivity", "Permiso de cámara no concedido, solicitando...");
            // Solicitar el permiso al usuario
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            Log.d("AdminActivity", "Permiso de cámara ya concedido, abriendo escáner");
            // Ya se tiene el permiso: abrir el escáner
            abrirEscanner();
        }
    }

    /**
     * Abre el escáner de códigos QR usando la librería ZXing
     * Configura el escáner para solo leer códigos QR
     */
    private void abrirEscanner() {
        Log.d("AdminActivity", "abrirEscanner llamado");
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Enfoca el código QR del usuario");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        Log.d("AdminActivity", "Iniciando escaneo QR con ZXing");
        integrator.initiateScan();
    }

    /**
     * Maneja el resultado del escaneo de código QR
     * Si se escanea un código, valida si corresponde a una reserva del día
     * 
     * @param requestCode Código de solicitud
     * @param resultCode Resultado de la Activity
     * @param data Intent con los datos del resultado
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("AdminActivity", "onActivityResult llamado, requestCode: " + requestCode + ", resultCode: " + resultCode);
        
        // Parsear el resultado del escaneo
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        Log.d("AdminActivity", "IntentResult parseado: " + (result != null ? "no null" : "null"));
        if (result != null) {
            Log.d("AdminActivity", "Contenido del QR: " + result.getContents());
        }
        
        // Si se escaneó un código QR, validarlo
        if (result != null && result.getContents() != null) {
            Log.d("AdminActivity", "QR escaneado, validando...");
            validarCodigoEscaneado(result.getContents());
        } else {
            Log.d("AdminActivity", "No se escaneó ningún QR o result es null");
        }
    }

    /**
     * Maneja la respuesta del usuario a la solicitud de permisos
     * Si se concede el permiso de cámara, abre el escáner
     * 
     * @param requestCode Código de solicitud
     * @param permissions Permisos solicitados
     * @param grantResults Resultados de la solicitud
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("AdminActivity", "onRequestPermissionsResult llamado, requestCode: " + requestCode);
        
        // Verificar si se concedió el permiso de cámara
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("AdminActivity", "Permiso de cámara concedido, abriendo escáner");
            abrirEscanner();
        } else {
            Log.d("AdminActivity", "Permiso de cámara denegado");
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Valida si el código QR escaneado corresponde a una reserva del día
     * Busca el código en las reservas del día y muestra el resultado
     * Si es válido, marca la reserva como confirmada en el backend
     * 
     * @param qrLeido Código QR escaneado por el usuario
     */
    private void validarCodigoEscaneado(String qrLeido) {
        Reserva reservaValida = null;
        
        // Buscar el código QR en las reservas del día
        for (Reserva r : reservasHoy) {
            if (qrLeido.equals(r.getCodigoQr())) {
                reservaValida = r;
                break;
            }
        }

        // Mostrar el resultado de la validación
        if (reservaValida != null) {
            // Código QR válido: acceso concedido
            // Marcar la reserva como confirmada en el backend
            marcarReservaComoConfirmada(reservaValida);
        } else {
            // Código QR inválido o de otro día: acceso denegado
            Toast.makeText(this, "❌ ACCESO DENEGADO: QR inválido o de otro día", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Marca una reserva como confirmada en el backend
     * 
     * @param reserva Reserva a marcar como confirmada
     */
    private void marcarReservaComoConfirmada(Reserva reserva) {
        String token = tokenManager.getToken();
        String nombre = reserva.getIdUsuario() != null ? reserva.getIdUsuario().getNombre() : "Usuario";
        
        // Crear el body con el nuevo estado
        Map<String, String> body = new HashMap<>();
        body.put("estado", "CONFIRMADA");
        
        // Mostrar el ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        
        // Realizar petición asíncrona al backend para actualizar el estado
        RetrofitClient.getApiService(token).actualizarEstadoReserva(reserva.getIdReserva(), body).enqueue(new Callback<Reserva>() {
            @Override
            public void onResponse(Call<Reserva> call, Response<Reserva> response) {
                // Ocultar el ProgressBar
                progressBar.setVisibility(View.GONE);
                
                // Verificar que la petición fue exitosa
                if (response.isSuccessful() && response.body() != null) {
                    // Reserva actualizada exitosamente
                    Toast.makeText(AdminActivity.this, "✅ ACCESO CONCEDIDO: " + nombre, Toast.LENGTH_LONG).show();
                    // Recargar las reservas para actualizar la UI
                    cargarReservasDelDia();
                } else {
                    // La petición no fue exitosa
                    String mensaje = response.code() == 403
                            ? "❌ QR válido, pero no tienes permiso para confirmar"
                            : "❌ QR válido, pero no se confirmó la reserva (" + response.code() + ")";
                    Toast.makeText(AdminActivity.this, mensaje, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Reserva> call, Throwable t) {
                // Ocultar el ProgressBar
                progressBar.setVisibility(View.GONE);
                
                // Mostrar error de conexión al usuario
                Toast.makeText(AdminActivity.this, "❌ Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Muestra un diálogo de confirmación para cerrar sesión
     * Si el usuario confirma, limpia las preferencias y vuelve al login
     */
    private void hacerLogout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Cerrar sesión", (dialog, which) -> {
                // Limpiar las preferencias compartidas
                getSharedPreferences("WishPortPrefs", MODE_PRIVATE).edit().clear().apply();
                
                // Limpiar el token JWT
                tokenManager.clearToken();
                
                // Ir a la pantalla de login
                startActivity(new Intent(this, LoginActivity.class));
                
                // Finalizar esta Activity
                finish();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    /**
     * Muestra un diálogo para crear un nuevo administrador
     * El diálogo contiene campos para nombre, email, contraseña y teléfono
     */
    private void mostrarDialogoCrearAdmin() {
        // Crear el layout del diálogo
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);

        // Campo de nombre
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

        // Campo de email
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

        // Campo de contraseña
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

        // Campo de teléfono
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

        // Crear el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Crear Nuevo Administrador");
        builder.setView(layout);
        builder.setPositiveButton("Crear", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Configurar el listener del botón positivo para validar y crear el admin
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Obtener los valores de los campos
            String nombre = etNombre.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            String telefono = etTelefono.getText().toString().trim();

            // Validar el nombre
            if (nombre.isEmpty()) {
                etNombre.setError("El nombre es obligatorio");
                etNombre.requestFocus();
                return;
            }
            
            // Validar el email
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
            
            // Validar la contraseña
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
            
            // Validar el teléfono
            if (telefono.isEmpty()) {
                etTelefono.setError("El teléfono es obligatorio");
                etTelefono.requestFocus();
                return;
            }

            // Crear objeto Usuario con los datos del formulario
            Usuario nuevoAdmin = new Usuario();
            nuevoAdmin.setNombre(nombre);
            nuevoAdmin.setEmail(email);
            nuevoAdmin.setPassword(password);
            nuevoAdmin.setTelefono(telefono);
            nuevoAdmin.setRol("ADMIN");

            // Llamar al método para crear el administrador
            crearAdmin(nuevoAdmin, dialog);
        });
    }

    /**
     * Envía la petición al backend para crear un nuevo administrador
     * 
     * @param usuario Objeto Usuario con los datos del nuevo admin
     * @param dialog Diálogo que se cerrará si la creación es exitosa
     */
    private void crearAdmin(Usuario usuario, AlertDialog dialog) {
        // Obtener el token JWT
        String token = tokenManager.getToken();
        
        // Mostrar el ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        // Realizar petición asíncrona al backend para crear el administrador
        RetrofitClient.getApiService(token).crearAdmin(usuario).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                // Ocultar el ProgressBar
                progressBar.setVisibility(View.GONE);
                
                // Verificar que la petición fue exitosa
                if (response.isSuccessful() && response.body() != null) {
                    // Administrador creado exitosamente
                    Toast.makeText(AdminActivity.this, "✅ Administrador creado exitosamente", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    cargarReservasDelDia();
                } else {
                    // La petición no fue exitosa
                    String errorMsg = response.code() == 403 ? 
                        "No tienes permiso para crear administradores" : 
                        "Error al crear administrador";
                    Toast.makeText(AdminActivity.this, "❌ " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                // Ocultar el ProgressBar
                progressBar.setVisibility(View.GONE);
                
                // Mostrar error de conexión al usuario
                Toast.makeText(AdminActivity.this, "❌ Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
