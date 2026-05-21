package com.wishport.frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.wishport.frontend.R;

/**
 * Activity de Splash Screen (pantalla de bienvenida)
 * Es la primera pantalla que se muestra al abrir la aplicación
 * Muestra el logo de la app durante 2 segundos antes de ir al login
 */
public class SplashActivity extends AppCompatActivity {

    /**
     * Duración en milisegundos de la splash screen
     * 2000 milisegundos = 2 segundos
     */
    private static final int SPLASH_DURATION = 2000;

    /**
     * Método llamado al crear la Activity
     * Muestra el logo y programa la transición a LoginActivity
     * 
     * @param savedInstanceState Bundle con el estado guardado de la Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Esperar 2 segundos y luego ir a LoginActivity
        // Handler permite ejecutar código después de un retraso
        new Handler().postDelayed(() -> {
            // Crear Intent para navegar a LoginActivity
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            
            // Finalizar esta Activity para que el usuario no pueda volver atrás
            finish();
        }, SPLASH_DURATION);
    }
}
