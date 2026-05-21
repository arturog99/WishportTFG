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
import retrofit2.http.Path;

public interface ApiService {
    String BASE_URL = "https://wishport-backend.onrender.com/api/";
    String IMAGES_BASE_URL = "https://wishport-backend.onrender.com";

    @POST("usuarios/register")
    Call<Usuario> register(@Body Usuario usuario);

    @POST("usuarios/login")
    Call<Map<String, Object>> login(@Body Usuario credenciales);

    @GET("usuarios/{id}")
    Call<Usuario> getUsuario(@Path("id") Integer id);

    @GET("pistas")
    Call<List<Pista>> getPistas();

    @GET("reservas/usuario/{id}")
    Call<List<Reserva>> getReservasUsuario(@Path("id") Integer id);

    @GET("reservas/pista/{id}")
    Call<List<Reserva>> getReservasPista(@Path("id") Integer id);

    @POST("reservas")
    Call<Reserva> crearReserva(@Body Reserva reserva);

    @DELETE("reservas/{id}")
    Call<Void> cancelarReserva(@Path("id") Integer id);
}