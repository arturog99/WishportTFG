package com.wishport.frontend.api;

import com.wishport.frontend.models.Usuario;
import com.wishport.frontend.models.Pista;
import com.wishport.frontend.models.Reserva;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Interfaz que define los endpoints de la API del backend
 * Usa Retrofit para realizar peticiones HTTP al servidor
 * Todos los endpoints están bajo la ruta base /api/
 */
public interface ApiService {
    /**
     * URL base del backend
     * Todas las peticiones se realizan a esta URL
     */
    String BASE_URL = "https://wishport-backend.onrender.com/api/";
    
    /**
     * URL base para las imágenes
     * Se usa para construir las URLs completas de las imágenes de las pistas
     */
    String IMAGES_BASE_URL = "https://wishport-backend.onrender.com";

    /**
     * Registra un nuevo usuario en el sistema
     * Endpoint: POST /api/usuarios/register
     * 
     * @param usuario Objeto Usuario con los datos del nuevo usuario
     * @return Call que retorna el usuario creado
     */
    @POST("usuarios/register")
    Call<Usuario> register(@Body Usuario usuario);

    /**
     * Inicia sesión de un usuario
     * Endpoint: POST /api/usuarios/login
     * 
     * @param credenciales Objeto Usuario con email y password
     * @return Call que retorna un mapa con el token JWT y datos del usuario
     */
    @POST("usuarios/login")
    Call<Map<String, Object>> login(@Body Usuario credenciales);

    /**
     * Crea un nuevo administrador
     * Endpoint: POST /api/usuarios/admin
     * Requiere autenticación y rol ADMIN
     * 
     * @param usuario Objeto Usuario con los datos del nuevo administrador
     * @return Call que retorna el usuario creado
     */
    @POST("usuarios/admin")
    Call<Usuario> crearAdmin(@Body Usuario usuario);

    /**
     * Obtiene un usuario por su ID
     * Endpoint: GET /api/usuarios/{id}
     * Requiere autenticación
     * 
     * @param id ID del usuario a obtener
     * @return Call que retorna el usuario solicitado
     */
    @GET("usuarios/{id}")
    Call<Usuario> getUsuario(@Path("id") Integer id);

    /**
     * Obtiene todas las pistas del sistema
     * Endpoint: GET /api/pistas
     * Requiere autenticación
     * 
     * @return Call que retorna la lista de todas las pistas
     */
    @GET("pistas")
    Call<List<Pista>> getPistas();

    /**
     * Obtiene todas las reservas del sistema
     * Endpoint: GET /api/reservas
     * Requiere autenticación y rol ADMIN
     * 
     * @return Call que retorna la lista de todas las reservas
     */
    @GET("reservas")
    Call<List<Reserva>> getReservas();

    /**
     * Obtiene todas las reservas de un usuario específico
     * Endpoint: GET /api/reservas/usuario/{id}
     * Requiere autenticación
     * 
     * @param id ID del usuario
     * @return Call que retorna la lista de reservas del usuario
     */
    @GET("reservas/usuario/{id}")
    Call<List<Reserva>> getReservasUsuario(@Path("id") Integer id);

    /**
     * Obtiene todas las reservas de una pista específica
     * Endpoint: GET /api/reservas/pista/{id}
     * Requiere autenticación
     * 
     * @param id ID de la pista
     * @return Call que retorna la lista de reservas de la pista
     */
    @GET("reservas/pista/{id}")
    Call<List<Reserva>> getReservasPista(@Path("id") Integer id);

    /**
     * Crea una nueva reserva
     * Endpoint: POST /api/reservas
     * Requiere autenticación
     * 
     * @param reserva Objeto Reserva con los datos de la reserva
     * @return Call que retorna la reserva creada con su código QR
     */
    @POST("reservas")
    Call<Reserva> crearReserva(@Body Reserva reserva);

    /**
     * Cancela una reserva existente
     * Endpoint: DELETE /api/reservas/{id}
     * Requiere autenticación
     * 
     * @param id ID de la reserva a cancelar
     * @return Call que no retorna datos (void)
     */
    @DELETE("reservas/{id}")
    Call<Void> cancelarReserva(@Path("id") Integer id);

    /**
     * Actualiza el estado de una reserva
     * Endpoint: PUT /api/reservas/{id}/estado
     * Requiere autenticación y rol ADMIN
     * 
     * @param id ID de la reserva a actualizar
     * @param estado Nuevo estado de la reserva (ej: "CONFIRMADA", "CANCELADA")
     * @return Call que retorna la reserva actualizada
     */
    @PUT("reservas/{id}/estado")
    Call<Reserva> actualizarEstadoReserva(@Path("id") Integer id, @Body Map<String, String> estado);
}