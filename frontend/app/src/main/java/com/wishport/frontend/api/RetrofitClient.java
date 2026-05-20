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

public class RetrofitClient {

    private static Gson buildGson() {
        DateTimeFormatter dateTimeFmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        JsonSerializer<LocalDateTime> dtSer = (src, typeOfSrc, ctx) ->
                new com.google.gson.JsonPrimitive(src.format(dateTimeFmt));
        JsonDeserializer<LocalDateTime> dtDes = (json, typeOfT, ctx) -> {
            String s = json.getAsString();
            // Tolerar "yyyy-MM-dd" como medianoche
            if (s.length() == 10) s = s + "T00:00:00";
            // Quitar zona/offset si viene
            if (s.endsWith("Z")) s = s.substring(0, s.length() - 1);
            return LocalDateTime.parse(s);
        };

        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, dtSer)
                .registerTypeAdapter(LocalDateTime.class, dtDes)
                .create();
    }

    public static Retrofit getClient(String token) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        if (token != null) {
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            });
        }

        return new Retrofit.Builder()
                .baseUrl(ApiService.BASE_URL)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create(buildGson()))
                .build();
    }

    public static ApiService getApiService(String token) {
        return getClient(token).create(ApiService.class);
    }
}