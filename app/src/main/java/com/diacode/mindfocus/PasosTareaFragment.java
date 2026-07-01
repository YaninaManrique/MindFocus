package com.diacode.mindfocus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diacode.mindfocus.adapter.PasosAdapter;
import com.diacode.mindfocus.data.AppDatabase;
import com.diacode.mindfocus.data.Paso;
import com.diacode.mindfocus.data.Tarea;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PasosTareaFragment extends Fragment {
    private int tareaId;
    private AppDatabase db;
    private Executor executor = Executors.newSingleThreadExecutor();
    private RecyclerView rvPasos;
    private PasosAdapter adapter;
    private TextView tvNombre;
    private TextView tvCantidad;
    private TextView tvCompletados;
    private TextView tvFaltantes;
    private ImageView btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pasostarea,
                container,
                false);
        db = AppDatabase.getInstance(requireContext());
        if(getArguments()!=null){
            tareaId=getArguments().getInt("tareaId");
        }
        tvNombre=view.findViewById(R.id.tvNombreTarea);
        tvCantidad=view.findViewById(R.id.tvCantidad);
        tvCompletados=view.findViewById(R.id.tvCompletados);
        tvFaltantes=view.findViewById(R.id.tvFaltantes);
        rvPasos=view.findViewById(R.id.rvPasos);
        // inicializa el adaptador
        adapter = new PasosAdapter((paso, checked) -> {// cuando se marca el check se ejecuta este lambda
            paso.setCompletado(checked);
            // guarda el cambio en la base de datos y refresca la interfaz
            executor.execute(() -> {
                db.pasoDao().actualizar(paso);
                comprobarEstadoTarea();
                requireActivity().runOnUiThread(() -> {
                    cargarDatos();
                });
            });
        });
        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
        // asocia el adaptador al RecyclerView
        rvPasos.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPasos.setAdapter(adapter);
        cargarDatos();
        return view;
    }

    private void cargarDatos(){
        executor.execute(()->{
            Tarea tarea=db.tareaDao().buscarPorId(tareaId);
            List<Paso> pasos=db.pasoDao().listarPorTarea(tareaId);
            int completados= (int) pasos.stream().filter(Paso::isCompletado).count();
            int faltantes=pasos.size()-completados;
            requireActivity().runOnUiThread(()->{
                tvNombre.setText(tarea.getNombre());
                tvCantidad.setText(completados+"/"+pasos.size());
                tvCompletados.setText(
                        "Has completado "+completados+" pasos"
                );
                tvFaltantes.setText(
                        "Te faltan "+faltantes+" pasos"
                );
                adapter.setLista(pasos);
            });

        });
    }
    private void comprobarEstadoTarea(){
        List<Paso> pasos = db.pasoDao().listarPorTarea(tareaId);
        boolean completa = true;
        for(Paso paso : pasos){
            if(!paso.isCompletado()){
                completa = false;
                break;
            }
        }
        Tarea tarea = db.tareaDao().buscarPorId(tareaId);
        tarea.setCompletada(completa);
        db.tareaDao().actualizar(tarea);
    }
}