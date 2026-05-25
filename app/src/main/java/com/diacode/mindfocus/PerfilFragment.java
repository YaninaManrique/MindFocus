package com.diacode.mindfocus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PerfilFragment extends Fragment {

    private TextView tvNombre;
    private TextView tvEmail;
    private Button btnCerrarSesion;

    public PerfilFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        tvNombre = view.findViewById(R.id.tvNombrePerfil);
        tvEmail  = view.findViewById(R.id.tvEmailPerfil);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);

        //leemos datos del usuario logueado
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("MindFocusPrefs", requireActivity().MODE_PRIVATE);

        tvNombre.setText(prefs.getString("nombre", "Usuario"));
        tvEmail.setText(prefs.getString("email", ""));

        //cerrar sesion
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());

        return view;
    }

    private void cerrarSesion() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("MindFocusPrefs", requireActivity().MODE_PRIVATE);

        //borramos los datos del xml del SharedPreferences que se creo
        prefs.edit().clear().apply();

        //regresamos a MainActivity y limpiamos el historial de pantallas
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}

