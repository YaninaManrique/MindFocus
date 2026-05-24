package com.diacode.mindfocus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class ActivityPrincipal extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_principal);

        //cargamos el fragmento inicial
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new InicioFragment())
                .commit();

        LinearLayout navHome     = findViewById(R.id.nav_home);
        LinearLayout navTasks    = findViewById(R.id.nav_tasks);
        LinearLayout navFocus    = findViewById(R.id.nav_focus);
        LinearLayout navProgress = findViewById(R.id.nav_progress);

        navHome.setOnClickListener(v ->
                cargarFragment(new InicioFragment()));

        navTasks.setOnClickListener(v ->
                cargarFragment(new TareasFragment()));

        navFocus.setOnClickListener(v ->
                cargarFragment(new EnfoqueFragment()));
//
//        navProgress.setOnClickListener(v ->
//                cargarFragment(new LogrosFragment()));
    }

    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

}