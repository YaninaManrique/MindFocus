package com.diacode.mindfocus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.diacode.mindfocus.data.EstadoTarea;
import com.diacode.mindfocus.data.Paso;
import com.diacode.mindfocus.data.Tarea;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
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
    private final String[] nombresDias = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
    private List<MaterialButton> botonesDias = new ArrayList<>();
    private List<Calendar> fechasSemana = new ArrayList<>();
    private int diaSeleccionado = 0; // índice dentro de fechasSemana
    private LinearLayout layoutDias;
    public TareasFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tareas, container, false);
        db = AppDatabase.getInstance(requireContext());
        prefs = requireActivity().getSharedPreferences("MindFocusPrefs", Context.MODE_PRIVATE);
        rvTareas = view.findViewById(R.id.rvTareas);
        layoutDias = view.findViewById(R.id.layout_dias);
        //adapter
        adapter = new TareasAdapter(new TareasAdapter.OnTareaClickListener() {
            @Override
            public void onClick(Tarea tarea) {
                Bundle bundle = new Bundle();
                bundle.putInt("tareaId", tarea.getId());
                bundle.putBoolean("soloLectura", esDiaPasado(fechasSemana.get(diaSeleccionado)));
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
                if (esDiaPasado(fechasSemana.get(diaSeleccionado))) {
                    Toast.makeText(requireContext(), "No puedes editar tareas de días anteriores", Toast.LENGTH_SHORT).show();
                    return;
                }
                executor.execute(() -> {
                    tarea.setCompletada(true);
                    tarea.setEstado(EstadoTarea.COMPLETADA);
                    db.tareaDao().actualizar(tarea);
                    db.pasoDao().completarTodos(tarea.getId());
                    requireActivity().runOnUiThread(this::cargarTareasDelDiaSeleccionado);
                });
            }
            private void cargarTareasDelDiaSeleccionado() {
                TareasFragment.this.cargarTareasDelDiaSeleccionado();
            }
        });
        rvTareas.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTareas.setAdapter(adapter);
        generarChipsSemana();
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
    //generar los 7 buttons de los dias de la semana
    private void generarChipsSemana() {
        layoutDias.removeAllViews();
        botonesDias.clear();
        fechasSemana.clear();
        Calendar hoy = Calendar.getInstance();
        int diaSemanaHoy = hoy.get(Calendar.DAY_OF_WEEK);//1=Dom, 2=Lun ... 7=Sab
        //indice 0=Lunes ... 6=Domingo
        int offsetHastaLunes = (diaSemanaHoy == Calendar.SUNDAY) ? -6 : -(diaSemanaHoy - Calendar.MONDAY);
        Calendar lunes = (Calendar) hoy.clone();
        lunes.add(Calendar.DAY_OF_MONTH, offsetHastaLunes);
        lunes.set(Calendar.HOUR_OF_DAY, 0);
        lunes.set(Calendar.MINUTE, 0);
        lunes.set(Calendar.SECOND, 0);
        lunes.set(Calendar.MILLISECOND, 0);
        int indiceHoy = 0;
        for (int i = 0; i < 7; i++) {
            Calendar diaCal = (Calendar) lunes.clone();
            diaCal.add(Calendar.DAY_OF_MONTH, i);
            fechasSemana.add(diaCal);

            if (esMismoDia(diaCal, hoy)) indiceHoy = i;

            MaterialButton btn = new MaterialButton(requireContext(), null,
                    com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btn.setText(nombresDias[i] + "\n" + diaCal.get(Calendar.DAY_OF_MONTH));
            btn.setTextSize(10);
            btn.setLineSpacing(0, 1f);
            btn.setAllCaps(false);

            btn.setInsetTop(0);
            btn.setInsetBottom(0);
            btn.setPadding(0, 0, 0, 0);
            btn.setMinWidth(0);
            btn.setMinimumWidth(0);
            btn.setCornerRadius(dpToPx(10));

            // reparte el ancho en partes iguales dentro del LinearLayout con weightSum=7
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, dpToPx(52), 1f);
            // margen pequeño entre botones (menos en el primero/último si quieres pegado a los bordes)
            int margin = dpToPx(3);
            lp.setMarginStart(margin);
            lp.setMarginEnd(margin);
            btn.setLayoutParams(lp);

            final int index = i;
            btn.setOnClickListener(v -> seleccionarDia(index));

            layoutDias.addView(btn);
            botonesDias.add(btn);
        }

        seleccionarDia(indiceHoy);
    }

    private void seleccionarDia(int index) {
        diaSeleccionado = index;
        for (int i = 0; i < botonesDias.size(); i++) {
            MaterialButton b = botonesDias.get(i);
            if (i == index) {
                b.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.filtro1_activityTar));
                b.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            } else {
                b.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.bg2_tint_activityTar));
                b.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_mid));
            }
        }
        cargarTareasDelDiaSeleccionado();
    }

    private void cargarTareasDelDiaSeleccionado() {
        int usuarioId = prefs.getInt("usuarioId", -1);
        Calendar diaCal = fechasSemana.get(diaSeleccionado);
        long inicio = diaCal.getTimeInMillis();
        long fin = inicio + (24L * 60 * 60 * 1000) - 1; // fin del día 23:59:59.999

        boolean pasado = esDiaPasado(diaCal);

        executor.execute(() -> {
            List<Tarea> tareas = db.tareaDao().listarPorFecha(usuarioId, inicio, fin);

            // Si el día ya pasó y alguna tarea quedó "pendiente", la marcamos como incompleta
            if (pasado) {
                for (Tarea t : tareas) {
                    if (t.getEstado() == EstadoTarea.PENDIENTE || !t.isCompletada() && t.getEstado() != EstadoTarea.COMPLETADA) {
                        t.setEstado(EstadoTarea.INCOMPLETA);
                        db.tareaDao().actualizar(t);
                    }
                }
            }
            requireActivity().runOnUiThread(() -> {
                adapter.setLista(tareas);
                adapter.setSoloLectura(pasado); // ver adapter abajo
            });
        });
    }
    private boolean esDiaPasado(Calendar dia) {
        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0);
        hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0);
        hoy.set(Calendar.MILLISECOND, 0);
        Calendar diaMedianoche = (Calendar) dia.clone();
        diaMedianoche.set(Calendar.HOUR_OF_DAY, 0);
        diaMedianoche.set(Calendar.MINUTE, 0);
        diaMedianoche.set(Calendar.SECOND, 0);
        diaMedianoche.set(Calendar.MILLISECOND, 0);
        return diaMedianoche.before(hoy);
    }
    private boolean esMismoDia(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    @Override
    public void onResume() {
        super.onResume();
        if (!fechasSemana.isEmpty()) {
            cargarTareasDelDiaSeleccionado();
        }
    }
}