package com.diacode.mindfocus;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //para ver si tiene una sesion activa
        SharedPreferences prefs = getSharedPreferences("MindFocusPrefs", MODE_PRIVATE);
        boolean sesionActiva = prefs.getBoolean("sesionActiva", false);

        // si ya hay sesion, saltamos directo sin mostrar la pantalla de bienvenida
        if (sesionActiva) {
            startActivity(new Intent(this, ActivityPrincipal.class));
            finish(); // importante: cierra MainActivity para que el botón "atrás" no vuelva aquí
            return;
        }
        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );
    }

}