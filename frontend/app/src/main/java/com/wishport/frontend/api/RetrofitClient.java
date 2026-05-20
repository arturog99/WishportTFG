package com.wishport.frontend.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Gson buildGson() {
        DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

        JsonSerializer<LocalDate> dateSer = (src, typeOfSrc, ctx) ->
                new com.google.gson.JsonPrimitive(src.format(dateFmt));
        JsonDeserializer<LocalDate> dateDes = (json, typeOfT, ctx) ->
                LocalDate.parse(json.getAsString(), dateFmt);

        JsonSerializer<LocalTime> timeSer = (src, typeOfSrc, ctx) ->
                new com.google.gson.JsonPrimitive(src.format(timeFmt));
        JsonDeserializer<LocalTime> timeDes = (json, typeOfT, ctx) -> {
            String s = json.getAsString();
            // Acepta "HH:mm" y "HH:mm:ss"
            if (s.length() == 5) s = s + ":00";
            return LocalTime.parse(s, timeFmt);
        };

        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, dateSer)
                .registerTypeAdapter(LocalDate.class, dateDes)
                .registerTypeAdapter(LocalTime.class, timeSer)
                .registerTypeAdapter(LocalTime.class, timeDes)
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