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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diacode.mindfocus.adapter.TareasAdapter;
import com.diacode.mindfocus.data.AppDatabase;
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
        adapter = new TareasAdapter(tarea -> {
            Bundle bundle = new Bundle();
            bundle.putInt("tareaId", tarea.getId());
            PasosTareaFragment fragment = new PasosTareaFragment();
            fragment.setArguments(bundle);
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvTareas.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTareas.setAdapter(adapter);
        cargarTareas();
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
    @Override
    public void onResume() {
        super.onResume();
        cargarTareas();
    }
}