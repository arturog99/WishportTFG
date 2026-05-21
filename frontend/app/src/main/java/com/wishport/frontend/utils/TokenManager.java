package com.wishport.frontend.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gestor de tokens JWT y datos de usuario
 * Almacena el token JWT y el ID del usuario en SharedPreferences
 * Permite persistir la sesión del usuario entre cierres de la aplicación
 */
public class TokenManager {
    /**
     * Nombre del archivo de preferencias compartidas
     */
    private static final String PREF_NAME = "WishPortPrefs";
    
    /**
     * Clave para almacenar el token JWT
     */
    private static final String KEY_TOKEN = "token";
    
    /**
     * Clave para almacenar el ID del usuario
     */
    private static final String KEY_USER_ID = "user_id";

    /**
     * Instancia de SharedPreferences para almacenar datos persistentes
     */
    private SharedPreferences preferences;

    /**
     * Constructor del TokenManager
     * Inicializa SharedPreferences con el contexto proporcionado
     * 
     * @param context Contexto de la aplicación
     */
    public TokenManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Guarda el token JWT en SharedPreferences
     * 
     * @param token Token JWT a guardar
     */
    public void saveToken(String token) {
        preferences.edit().putString(KEY_TOKEN, token).apply();
    }

    /**
     * Obtiene el token JWT almacenado
     * 
     * @return Token JWT almacenado, o null si no existe
     */
    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    /**
     * Elimina el token JWT de SharedPreferences
     * Se usa al cerrar sesión
     */
    public void clearToken() {
        preferences.edit().remove(KEY_TOKEN).apply();
    }

    /**
     * Guarda el ID del usuario en SharedPreferences
     * 
     * @param userId ID del usuario a guardar
     */
    public void saveUserId(Integer userId) {
        preferences.edit().putInt(KEY_USER_ID, userId).apply();
    }

    /**
     * Obtiene el ID del usuario almacenado
     * 
     * @return ID del usuario almacenado, o -1 si no existe
     */
    public Integer getUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }

    /**
     * Elimina todos los datos almacenados en SharedPreferences
     * Se usa al cerrar sesión para limpiar todos los datos del usuario
     */
    public void clearAll() {
        preferences.edit().clear().apply();
    }
}