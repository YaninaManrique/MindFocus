package com.diacode.mindfocus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

public class InicioFragment extends Fragment {

    private MaterialCardView cardObjetivos;
    private TextView etTextoInicial;
    private SharedPreferences prefs;
    //constructor vacio
    public InicioFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        prefs = requireActivity().getSharedPreferences("MindFocusPrefs", requireActivity().MODE_PRIVATE);
        //obtenemos el nombre
        String nombre = prefs.getString("nombre", "Usuario");//Usuario es el texto por defecto
        cardObjetivos = view.findViewById(R.id.cardEnfoque);
        etTextoInicial = view.findViewById(R.id.etTextoInicial);
        etTextoInicial.setText("¡Bienvenid@, " + nombre + "!");
        cardObjetivos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new EnfoqueFragment())
                        .commit();
            }
        });

        return view;
    }
}
