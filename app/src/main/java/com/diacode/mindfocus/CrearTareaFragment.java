package com.diacode.mindfocus;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.diacode.mindfocus.data.AppDatabase;
import com.diacode.mindfocus.data.EstadoTarea;
import com.diacode.mindfocus.data.Paso;
import com.diacode.mindfocus.data.Prioridad;
import com.diacode.mindfocus.data.Tarea;
import com.diacode.mindfocus.data.TipoTarea;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CrearTareaFragment extends Fragment{
    private LinearLayout layoutSteps;
    private Button btnAddStep;
    private EditText etNombre;
    private EditText etNota;
    private CardView btnGuardar;
    private AppDatabase db;
    private SharedPreferences prefs;
    private Executor executor = Executors.newSingleThreadExecutor();
    private TipoTarea tipoSeleccionado = TipoTarea.ESTUDIO;
    private LinearLayout priorityNormal;
    private LinearLayout priorityMedia;
    private LinearLayout priorityAlta;
    private Prioridad prioridadSeleccionada = Prioridad.MEDIA;
    private CardView chipStudy;
    private CardView chipExercise;
    private CardView chipHome;
    private CardView chipWork;
    private CardView chipCreative;
    private CardView btnBack;
    private MaterialButton btnPickDate, btnPickStart, btnPickEnd;
    private final Calendar fechaSeleccionada = Calendar.getInstance();
    private Long horaInicioMillis = null;
    private Long horaFinMillis = null;
    public CrearTareaFragment(){}
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_tarea, container, false);
        layoutSteps = view.findViewById(R.id.layout_steps);
        btnAddStep = view.findViewById(R.id.btn_add_step);
        btnAddStep.setOnClickListener(v -> agregarPaso());
        //primer paso por defecto
        agregarPaso();
        db = AppDatabase.getInstance(requireContext());
        prefs = requireActivity().getSharedPreferences(
                "MindFocusPrefs",
                Context.MODE_PRIVATE
        );
        etNombre = view.findViewById(R.id.et_task_name);
        etNota = view.findViewById(R.id.et_note);
        btnGuardar = view.findViewById(R.id.btn_save);
        btnGuardar.setOnClickListener(v -> guardarTarea());

        chipStudy = view.findViewById(R.id.chip_study);
        chipExercise = view.findViewById(R.id.chip_exercise);
        chipHome = view.findViewById(R.id.chip_home);
        chipWork = view.findViewById(R.id.chip_work);
        chipCreative = view.findViewById(R.id.chip_creative);

        priorityNormal = view.findViewById(R.id.priority_normal);
        priorityMedia = view.findViewById(R.id.priority_media);
        priorityAlta = view.findViewById(R.id.priority_alta);

        chipStudy.setOnClickListener(v -> seleccionarTipo(TipoTarea.ESTUDIO));
        chipExercise.setOnClickListener(v -> seleccionarTipo(TipoTarea.EJERCICIO));
        chipHome.setOnClickListener(v -> seleccionarTipo(TipoTarea.HOGAR));
        chipWork.setOnClickListener(v -> seleccionarTipo(TipoTarea.TRABAJO));
        chipCreative.setOnClickListener(v -> seleccionarTipo(TipoTarea.CREATIVO));

        priorityNormal.setOnClickListener(v -> seleccionarPrioridad(Prioridad.BAJA));
        priorityMedia.setOnClickListener(v -> seleccionarPrioridad(Prioridad.MEDIA));
        priorityAlta.setOnClickListener(v -> seleccionarPrioridad(Prioridad.ALTA));

        btnPickDate = view.findViewById(R.id.btn_pick_date);
        btnPickStart = view.findViewById(R.id.btn_pick_start);
        btnPickEnd = view.findViewById(R.id.btn_pick_end);

        btnPickDate.setOnClickListener(v -> mostrarSelectorFecha());
        btnPickStart.setOnClickListener(v -> mostrarSelectorHora(true));
        btnPickEnd.setOnClickListener(v -> mostrarSelectorHora(false));

        btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> volverATareas());

        seleccionarTipo(TipoTarea.ESTUDIO);
        seleccionarPrioridad(Prioridad.MEDIA);
        return view;
    }
    private void agregarPaso() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View paso = inflater.inflate(R.layout.item_step, layoutSteps, false);
        ImageButton btnDelete = paso.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> layoutSteps.removeView(paso));
        layoutSteps.addView(paso);
    }
    private void guardarTarea() {
        String nombre = etNombre.getText().toString().trim();
        String nota = etNota.getText().toString().trim();
        if (nombre.isEmpty()) {
            Toast.makeText(requireContext(), "Ingrese un nombre", Toast.LENGTH_SHORT).show();
            return;
        }
        //validar campos de fecha y hora
        if (horaInicioMillis == null || horaFinMillis == null) {
            Toast.makeText(requireContext(), "Seleccione fecha y horario", Toast.LENGTH_SHORT).show();
            return;
        }
        if (horaFinMillis <= horaInicioMillis) {
            Toast.makeText(requireContext(), "La hora de fin debe ser mayor a la de inicio", Toast.LENGTH_SHORT).show();
            return;
        }
        int usuarioId = prefs.getInt("usuarioId", -1);
        executor.execute(() -> {
            Tarea tarea = new Tarea();
            tarea.setUsuarioId(usuarioId);
            tarea.setNombre(nombre);
            tarea.setNota(nota);
            tarea.setTipo(tipoSeleccionado);
            tarea.setPrioridad(prioridadSeleccionada);
            tarea.setCompletada(false);
            tarea.setEstado(EstadoTarea.PENDIENTE);
            tarea.setFecha(fechaSeleccionada.getTimeInMillis());
            tarea.setHoraInicio(horaInicioMillis);
            tarea.setHoraFin(horaFinMillis);

            long idTarea = db.tareaDao().insertar(tarea);
            List<Paso> pasos = new ArrayList<>();
            for (int i = 0; i < layoutSteps.getChildCount(); i++) {
                View item = layoutSteps.getChildAt(i);
                EditText etPaso = item.findViewById(R.id.etStep);
                String descripcion = etPaso.getText().toString().trim();
                if (descripcion.isEmpty())
                    continue;
                Paso paso = new Paso();
                paso.setTareaId((int) idTarea);
                paso.setDescripcion(descripcion);
                paso.setCompletado(false);
                pasos.add(paso);
            }
            db.pasoDao().insertarTodos(pasos);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(),
                        "Tarea registrada",
                        Toast.LENGTH_SHORT).show();
                volverATareas();
            });
        });
    }
    private void seleccionarTipo(TipoTarea tipo){
        tipoSeleccionado = tipo;
        restaurarChips();
        switch (tipo){
            case ESTUDIO:
                chipStudy.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.background_card_study));
                break;
            case EJERCICIO:
                chipExercise.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.background_exercise));
                break;
            case HOGAR:
                chipHome.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.card_background_home));
                break;
            case TRABAJO:
                chipWork.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.card_background_work));
                break;
            case CREATIVO:
                chipCreative.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.card_background_creative));
                break;
        }
    }
    private void seleccionarPrioridad(Prioridad prioridad){
        prioridadSeleccionada = prioridad;
        restaurarPrioridades();
        switch (prioridad){
            case BAJA:
                priorityNormal.setBackgroundResource(R.drawable.bg_prioridad_seleccionada);
                break;
            case MEDIA:
                priorityMedia.setBackgroundResource(R.drawable.bg_prioridad_seleccionada);
                break;
            case ALTA:
                priorityAlta.setBackgroundResource(R.drawable.bg_prioridad_seleccionada);
                break;
        }
    }
    private void restaurarChips(){
        int color = ContextCompat.getColor(requireContext(), R.color.white);
        chipStudy.setCardBackgroundColor(color);
        chipExercise.setCardBackgroundColor(color);
        chipHome.setCardBackgroundColor(color);
        chipWork.setCardBackgroundColor(color);
        chipCreative.setCardBackgroundColor(color);
    }
    private void restaurarPrioridades(){
        priorityNormal.setBackgroundResource(R.drawable.bg_prioridad1);
        priorityMedia.setBackgroundResource(R.drawable.bg_prioridad1);
        priorityAlta.setBackgroundResource(R.drawable.bg_prioridad1);
    }
    private void volverATareas() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new TareasFragment())
                .commit();
    }
    private void mostrarSelectorFecha() {
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            fechaSeleccionada.set(year, month, day, 0, 0, 0);
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM/yyyy", new Locale("es"));
            btnPickDate.setText("📅 " + sdf.format(fechaSeleccionada.getTime()));
            //si ya se habían elegido horas, hay que reajustarlas al nuevo dia
            horaInicioMillis = null;
            horaFinMillis = null;
            btnPickStart.setText("⏱ Inicio");
            btnPickEnd.setText("⏱ Fin");
        }, fechaSeleccionada.get(Calendar.YEAR),
                fechaSeleccionada.get(Calendar.MONTH),
                fechaSeleccionada.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void mostrarSelectorHora(boolean esInicio) {
        Calendar ahora = Calendar.getInstance();
        new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            Calendar horaCal = (Calendar) fechaSeleccionada.clone();
            horaCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            horaCal.set(Calendar.MINUTE, minute);
            horaCal.set(Calendar.SECOND, 0);
            SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
            if (esInicio) {
                horaInicioMillis = horaCal.getTimeInMillis();
                btnPickStart.setText("⏱ Inicio: " + sdfHora.format(horaCal.getTime()));
            } else {
                horaFinMillis = horaCal.getTimeInMillis();
                btnPickEnd.setText("⏱ Fin: " + sdfHora.format(horaCal.getTime()));
            }
        }, ahora.get(Calendar.HOUR_OF_DAY), ahora.get(Calendar.MINUTE), true).show();
    }
}