package com.diacode.mindfocus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diacode.mindfocus.adapter.TareasAdapter;
import com.diacode.mindfocus.data.AppDatabase;
import com.diacode.mindfocus.data.Paso;
import com.diacode.mindfocus.data.Tarea;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TareasFragment extends Fragment {
    private MaterialButton btnAdd;
    private RecyclerView rvTareas;
    private TareasAdapter adapter;
    private AppDatabase db;
    private SharedPreferences prefs;
    private MaterialButton btnFilterAll;
    private MaterialButton btnFilterPending;
    private MaterialButton btnFilterDone;
    private int filtroActual = 0;
    private static final int TODAS = 0;
    private static final int PENDIENTES = 1;
    private static final int COMPLETADAS = 2;
    private Executor executor = Executors.newSingleThreadExecutor();
    public TareasFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tareas, container, false);
        db = AppDatabase.getInstance(requireContext());
        prefs = requireActivity().getSharedPreferences(
                "MindFocusPrefs",
                Context.MODE_PRIVATE
        );
        rvTareas = view.findViewById(R.id.rvTareas);
        //adapter
        adapter = new TareasAdapter(new TareasAdapter.OnTareaClickListener() {
            @Override
            public void onClick(Tarea tarea) {
                Bundle bundle = new Bundle();
                bundle.putInt("tareaId", tarea.getId());
                PasosTareaFragment fragment = new PasosTareaFragment();
                fragment.setArguments(bundle);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }
            @Override
            public void onCompletarTarea(Tarea tarea) {
                //completar todos los pasos con el pk de la tarea
                executor.execute(() -> {
                    tarea.setCompletada(true);
                    db.tareaDao().actualizar(tarea);
                    db.pasoDao().completarTodos(tarea.getId());
                    requireActivity().runOnUiThread(() -> {
                        cargarTareas();
                    });
                });
            }
        });
        rvTareas.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTareas.setAdapter(adapter);
        btnFilterAll = view.findViewById(R.id.btn_filter_all);
        btnFilterPending = view.findViewById(R.id.btn_filter_pending);
        btnFilterDone = view.findViewById(R.id.btn_filter_done);
        cargarTareas();
        seleccionarFiltro(btnFilterAll);
        btnFilterAll.setOnClickListener(v -> {
            filtroActual = TODAS;
            seleccionarFiltro(btnFilterAll);
            cargarTareas();
        });
        btnFilterPending.setOnClickListener(v -> {
            filtroActual = PENDIENTES;
            seleccionarFiltro(btnFilterPending);
            cargarPendientes();
        });
        btnFilterDone.setOnClickListener(v -> {
            filtroActual = COMPLETADAS;
            seleccionarFiltro(btnFilterDone);
            cargarCompletadas();
        });
        //ir a crear tareas
        btnAdd = view.findViewById(R.id.btn_add_task);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new CrearTareaFragment())
                        .commit();
            }
        });
        return view;
    }
    private void cargarTareas(){
        int usuarioId = prefs.getInt("usuarioId",-1);
        executor.execute(() ->{
            List<Tarea> tareas = db.tareaDao().listarTodas(usuarioId);
            requireActivity().runOnUiThread(() ->{
                adapter.setLista(tareas);
            });
        });
    }
    private void cargarPendientes(){
        int usuarioId = prefs.getInt("usuarioId",-1);
        executor.execute(() -> {
            List<Tarea> tareas = db.tareaDao().listarPendientes(usuarioId);
            requireActivity().runOnUiThread(() -> {
                adapter.setLista(tareas);
            });
        });
    }
    private void cargarCompletadas(){
        int usuarioId = prefs.getInt("usuarioId",-1);
        executor.execute(() -> {
            List<Tarea> tareas = db.tareaDao().listarCompletadas(usuarioId);
            requireActivity().runOnUiThread(() -> {
                adapter.setLista(tareas);
            });
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        switch (filtroActual){
            case TODAS:
                cargarTareas();
                break;
            case PENDIENTES:
                cargarPendientes();
                break;
            case COMPLETADAS:
                cargarCompletadas();
                break;
        }
    }
    //para cambiar de color al seleccionado
    private void seleccionarFiltro(MaterialButton seleccionado){
        MaterialButton[] botones = {
                btnFilterAll,
                btnFilterPending,
                btnFilterDone
        };
        for(MaterialButton boton : botones){
            if(boton == seleccionado){
                boton.setBackgroundTintList(
                        ContextCompat.getColorStateList(requireContext(),
                                R.color.filtro1_activityTar));
                boton.setTextColor(
                        ContextCompat.getColor(requireContext(),
                                android.R.color.white));
            }else{
                boton.setBackgroundTintList(
                        ContextCompat.getColorStateList(requireContext(),
                                R.color.bg2_tint_activityTar));
                boton.setTextColor(
                        ContextCompat.getColor(requireContext(),
                                R.color.text_mid));
            }
        }
    }
}