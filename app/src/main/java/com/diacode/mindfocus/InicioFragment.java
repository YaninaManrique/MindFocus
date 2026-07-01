package com.diacode.mindfocus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.diacode.mindfocus.data.AppDatabase;
import com.diacode.mindfocus.data.Tarea;
import com.google.android.material.card.MaterialCardView;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InicioFragment extends Fragment {

    private MaterialCardView cardObjetivos;
    private MaterialCardView cardTareas;
    private MaterialCardView cardCrearTareas;
    private MaterialCardView cardMiTiempo;
    private TextView etTextoInicial;
    private SharedPreferences prefs;
    private ProgressBar progressHoy;
    private TextView tvPorcentaje;
    private TextView tvTareaActual;
    private Button btnEmpezar;
    private AppDatabase db;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Tarea tareaActual;
    //constructor vacio
    public InicioFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        prefs = requireActivity().getSharedPreferences("MindFocusPrefs", requireActivity().MODE_PRIVATE);
        db = AppDatabase.getInstance(requireContext());
        progressHoy = view.findViewById(R.id.progressHoy);
        tvPorcentaje = view.findViewById(R.id.tvPorcentaje);
        tvTareaActual = view.findViewById(R.id.tvTareaActual);
        btnEmpezar = view.findViewById(R.id.btnEmpezar);
        //para que vaya al detalle de la tarea donde salen los pasos
        btnEmpezar.setOnClickListener(v -> {
            if(tareaActual == null)
                return;
            Bundle bundle = new Bundle();
            bundle.putInt("tareaId", tareaActual.getId());
            PasosTareaFragment fragment = new PasosTareaFragment();
            fragment.setArguments(bundle);
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        //obtenemos el nombre
        String nombre = prefs.getString("nombre", "Usuario");//Usuario es el texto por defecto
        etTextoInicial = view.findViewById(R.id.etTextoInicial);
        etTextoInicial.setText("¡Bienvenid@, " + nombre + "!");
        //ir objetivos
        cardObjetivos = view.findViewById(R.id.cardEnfoque);
        cardObjetivos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new EnfoqueFragment())
                        .commit();
            }
        });
        //ir tareas
        cardTareas = view.findViewById(R.id.cardTareas);
        cardTareas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new TareasFragment())
                        .commit();
            }
        });
        //ir crear tareas
        cardCrearTareas = view.findViewById(R.id.cardCrearTareas);
        cardCrearTareas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new CrearTareaFragment())
                        .commit();
            }
        });
        //ir mi tiempo
        cardMiTiempo = view.findViewById(R.id.cardMiTiempo);
        cardMiTiempo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new TimeFragment())
                        .commit();
            }
        });
        cargarResumen();
        return view;
    }
    private void cargarResumen(){
        int usuarioId = prefs.getInt("usuarioId",-1);
        executor.execute(() ->{
            int total = db.tareaDao().contarTodas(usuarioId);
            int completadas = db.tareaDao().contarCompletadas(usuarioId);
            tareaActual = db.tareaDao().obtenerTareaActual(usuarioId);
            int porcentaje = 0;
            if(total > 0){
                porcentaje = (completadas * 100) / total;
            }
            int finalPorcentaje = porcentaje;
            requireActivity().runOnUiThread(() ->{
                progressHoy.setProgress(finalPorcentaje);
                tvPorcentaje.setText(finalPorcentaje + "%");
                if(tareaActual != null){
                    tvTareaActual.setText(tareaActual.getNombre());
                    btnEmpezar.setEnabled(true);
                }else{
                    tvTareaActual.setText("No tienes tareas pendientes");
                    btnEmpezar.setEnabled(false);
                }
            });
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        cargarResumen();
    }
}
