package com.wishport.frontend.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente Retrofit para realizar peticiones HTTP al backend
 * Configura Retrofit con Gson para serializar/deserializar objetos Java
 * Añade el token JWT en el header Authorization de las peticiones
 */
public class RetrofitClient {

    /**
     * Construye y configura una instancia de Gson
     * Configura los serializadores y deserializadores para LocalDateTime
     * 
     * @return Instancia de Gson configurada
     */
    private static Gson buildGson() {
        // Formateador de fecha y hora (ISO 8601)
        DateTimeFormatter dateTimeFmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        // Serializador de LocalDateTime a String
        JsonSerializer<LocalDateTime> dtSer = (src, typeOfSrc, ctx) ->
                new com.google.gson.JsonPrimitive(src.format(dateTimeFmt));
        
        // Deserializador de String a LocalDateTime
        // Tolerante con diferentes formatos de fecha
        JsonDeserializer<LocalDateTime> dtDes = (json, typeOfT, ctx) -> {
            String s = json.getAsString();
            // Tolerar "yyyy-MM-dd" como medianoche
            if (s.length() == 10) s = s + "T00:00:00";
            // Quitar zona/offset si viene
            if (s.endsWith("Z")) s = s.substring(0, s.length() - 1);
            return LocalDateTime.parse(s);
        };

        // Crear y retornar la instancia de Gson con los adaptadores configurados
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, dtSer)
                .registerTypeAdapter(LocalDateTime.class, dtDes)
                .create();
    }

    /**
     * Obtiene una instancia de Retrofit configurada
     * Si se proporciona un token, lo añade al header Authorization
     * 
     * @param token Token JWT para autenticación (puede ser null)
     * @return Instancia de Retrofit configurada
     */
    public static Retrofit getClient(String token) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        // Si se proporciona un token, añadir interceptor para incluirlo en las peticiones
        if (token != null) {
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                
                // Crear nueva petición con el header Authorization
                Request request = original.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .method(original.method(), original.body())
                        .build();
                
                return chain.proceed(request);
            });
        }

        // Crear y retornar la instancia de Retrofit
        return new Retrofit.Builder()
                .baseUrl(ApiService.BASE_URL)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create(buildGson()))
                .build();
    }

    /**
     * Obtiene una instancia de ApiService configurada
     * Usa el método getClient para obtener la instancia de Retrofit
     * y crea la implementación de la interfaz ApiService
     * 
     * @param token Token JWT para autenticación (puede ser null)
     * @return Instancia de ApiService configurada
     */
    public static ApiService getApiService(String token) {
        return getClient(token).create(ApiService.class);
    }
}