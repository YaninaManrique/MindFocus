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

        //buscamos el button del xml
        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v -> {
            if (sesionActiva) {
                //si ya tiene sesion va directo al principal
                startActivity(new Intent(this, ActivityPrincipal.class));
            } else {
                //sino va al login
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
    }

}