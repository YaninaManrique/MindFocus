package com.diacode.mindfocus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast; //mensajes en pantalla

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class ActivityPrincipal extends AppCompatActivity {

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            Toast.makeText(this,
                                    "✅ Notificaciones activadas",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this,
                                    "❌ Sin notificaciones no podrás ver el timer en background",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_principal);
        pedirPermisoNotificaciones();
        //cargamos el fragmento inicial
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new InicioFragment())
                .commit();

        LinearLayout navHome     = findViewById(R.id.nav_home);
        LinearLayout navTasks    = findViewById(R.id.nav_tasks);
        LinearLayout navFocus    = findViewById(R.id.nav_focus);
        LinearLayout navProgress = findViewById(R.id.nav_progress);
        LinearLayout navProfile = findViewById(R.id.nav_perfil);

        navHome.setOnClickListener(v ->
                cargarFragment(new InicioFragment()));

        navTasks.setOnClickListener(v ->
                cargarFragment(new TareasFragment()));

        navFocus.setOnClickListener(v ->
                cargarFragment(new EnfoqueFragment()));

//        navProgress.setOnClickListener(v ->
//                cargarFragment(new LogrosFragment()));
        navProfile.setOnClickListener(v ->
                cargarFragment(new PerfilFragment()));
    }

    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                //sino tiene permiso lo pide
                requestPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS);
            }
            //si ya lo tiene no hace nada
        }
        //API menor a 33 no necesita pedirlo porque funciona automaticamente
    }

}