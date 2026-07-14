package com.diacode.mindfocus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.diacode.mindfocus.data.EstadoTarea;
import com.diacode.mindfocus.data.Paso;
import com.diacode.mindfocus.data.Tarea;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PasosTareaFragment extends Fragment {
    private int tareaId;
    private boolean soloLectura = false;
    private AppDatabase db;
    private Executor executor = Executors.newSingleThreadExecutor();
    private RecyclerView rvPasos;
    private PasosAdapter adapter;
    private TextView tvNombre;
    private TextView tvCantidad;
    private TextView tvCompletados;
    private TextView tvFaltantes;
    private ImageView btnBack;
    private LinearLayout layoutBloqueado;
    private TextView tvTimerTarea;
    private MaterialButton btnIniciarTarea, btnFinalizarTarea;
    private TimerService timerService;
    private boolean serviceBound = false;
    private Tarea tareaActual; //guardamos la tarea cargada para acceder a horaFin
    //para que revise cada cierto tiempo que ya llego a la hora
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable verificadorEstado;
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
            soloLectura = getArguments().getBoolean("soloLectura", false);
        }
        tvNombre=view.findViewById(R.id.tvNombreTarea);
        tvCantidad=view.findViewById(R.id.tvCantidad);
        tvCompletados=view.findViewById(R.id.tvCompletados);
        tvFaltantes=view.findViewById(R.id.tvFaltantes);
        rvPasos=view.findViewById(R.id.rvPasos);
        layoutBloqueado = view.findViewById(R.id.layout_bloqueado);
        layoutBloqueado.setVisibility(soloLectura ? View.VISIBLE : View.GONE);

        tvTimerTarea = view.findViewById(R.id.tvTimerTarea);
        btnIniciarTarea = view.findViewById(R.id.btnIniciarTarea);
        btnFinalizarTarea = view.findViewById(R.id.btnFinalizarTarea);

        btnIniciarTarea.setOnClickListener(v -> iniciarTemporizadorTarea());
        btnFinalizarTarea.setOnClickListener(v -> finalizarTarea());

        // si soloLectura=true, deshabilita ambos botones
         if (soloLectura) {
             btnIniciarTarea.setEnabled(false);
             btnFinalizarTarea.setEnabled(false);
         }

        // inicializa el adaptador
        adapter = new PasosAdapter((paso, checked) -> {// cuando se marca el check se ejecuta este lambda
            if (soloLectura) return;//ignora cualquier cambio si solo es lectura
            paso.setCompletado(checked);
            // guarda el cambio en la base de datos y refresca la interfaz
            executor.execute(() -> {
                db.pasoDao().actualizar(paso);
                //comprobarEstadoTarea();
                requireActivity().runOnUiThread(() -> {cargarDatos();});
            });
        });
        adapter.setSoloLectura(soloLectura);
        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
        // asocia el adaptador al RecyclerView
        rvPasos.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPasos.setAdapter(adapter);
        cargarDatos();
        iniciarVerificadorPeriodico();
        return view;
    }

    private void cargarDatos(){
        executor.execute(()->{
            Tarea tarea=db.tareaDao().buscarPorId(tareaId);
            tareaActual = tarea;
            List<Paso> pasos=db.pasoDao().listarPorTarea(tareaId);
            int completados= (int) pasos.stream().filter(Paso::isCompletado).count();
            int faltantes=pasos.size()-completados;
            requireActivity().runOnUiThread(()->{
                tvNombre.setText(tarea.getNombre());
                tvCantidad.setText(completados+"/"+pasos.size());
                tvCompletados.setText(
                        "Has completado "+completados+" pasos"
                );
                // si es solo lectura y quedaron pasos sin completar, aclaramos que ya no se pueden hacer
                if (soloLectura && faltantes > 0) {
                    tvFaltantes.setText("Quedaron " + faltantes + " pasos sin completar");
                } else {
                    tvFaltantes.setText("Te faltan " + faltantes + " pasos");
                }
                adapter.setLista(pasos);
                actualizarEstadoBotonIniciar();
            });

        });
    }
    private void comprobarEstadoTarea(){
        List<Paso> pasos = db.pasoDao().listarPorTarea(tareaId);
        boolean completa = !pasos.isEmpty();
        for(Paso paso : pasos){
            if(!paso.isCompletado()){
                completa = false;
                break;
            }
        }
        Tarea tarea = db.tareaDao().buscarPorId(tareaId);
        tarea.setCompletada(completa);
        tarea.setEstado(completa ? EstadoTarea.COMPLETADA : EstadoTarea.PENDIENTE);
        db.tareaDao().actualizar(tarea);
    }
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            TimerService.TimerBinder binder = (TimerService.TimerBinder) iBinder;
            timerService = binder.getService();
            serviceBound = true;
            timerService.setTimerListener(new TimerService.TimerListener() {
                @Override
                public void onTimerTick(long millisLeft) {
                    requireActivity().runOnUiThread(() -> {
                        actualizarTextoTimer(millisLeft);
                        btnFinalizarTarea.setEnabled(timerService.puedeFinalizar());
                    });
                }
                @Override
                public void onTimerFinish(boolean finalizadoManualmente) {
                    requireActivity().runOnUiThread(() -> {
                        btnIniciarTarea.setEnabled(false);
                        btnFinalizarTarea.setEnabled(false);
                        marcarResultadoTarea(finalizadoManualmente);
                    });
                }
            });
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };
    private void iniciarTemporizadorTarea() {
        if (tareaActual == null) return;
        Intent intent = new Intent(requireContext(), TimerService.class);
        intent.putExtra("tareaId", tareaActual.getId());
        intent.putExtra("nombreTarea", tareaActual.getNombre());
        intent.putExtra("horaFin", tareaActual.getHoraFin()); // timestamp absoluto
        requireActivity().startService(intent);
        requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        btnIniciarTarea.setEnabled(false);
    }
    private void finalizarTarea() {
        if (serviceBound && timerService != null && timerService.puedeFinalizar()) {
            timerService.finalizarManualmente();
        }
    }
    private void actualizarTextoTimer(long millis) {
        if (millis < 0) millis = 0;
        long horas = millis / 1000 / 3600;
        long minutos = (millis / 1000 / 60) % 60;
        long segundos = (millis / 1000) % 60;
        if (horas > 0) {
            tvTimerTarea.setText(String.format("%02d:%02d:%02d", horas, minutos, segundos));
        } else {
            tvTimerTarea.setText(String.format("%02d:%02d", minutos, segundos));
        }
    }
    //marca la tarea como completada o incompleta segun como termino el timer
    private void marcarResultadoTarea(boolean finalizadoManualmente) {
        executor.execute(() -> {
            Tarea tarea = db.tareaDao().buscarPorId(tareaId);
            if (finalizadoManualmente) {
                tarea.setCompletada(true);
                tarea.setEstado(EstadoTarea.COMPLETADA);
                db.pasoDao().completarTodos(tareaId);//completamos todos los pasos
            } else {
                tarea.setEstado(EstadoTarea.INCOMPLETA);
            }
            db.tareaDao().actualizar(tarea);
            requireActivity().runOnUiThread(this::cargarDatos);
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null && verificadorEstado != null) {
            handler.removeCallbacks(verificadorEstado); // NUEVO: detiene el chequeo
        }
        if (serviceBound) {
            requireActivity().unbindService(serviceConnection);
            serviceBound = false;
        }
    }
    private void actualizarEstadoBotonIniciar() {
        if (tareaActual == null || soloLectura) return;
        //si el timer ya esta corriendo, no tocar el texto ni el button
        if (serviceBound && timerService != null && timerService.isRunning()) {
            return;
        }
        long ahora = System.currentTimeMillis();
        boolean yaEmpezoElRango = ahora >= tareaActual.getHoraInicio();
        boolean noHaTerminado = ahora <= tareaActual.getHoraFin();
        boolean dentroDeRango = yaEmpezoElRango && noHaTerminado;
        boolean yaCompletada = tareaActual.getEstado() == EstadoTarea.COMPLETADA;
        btnIniciarTarea.setEnabled(dentroDeRango && !yaCompletada);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        if (yaCompletada) {
            tvTimerTarea.setText("✅ Completada");
        } else if (!yaEmpezoElRango) {
            tvTimerTarea.setText("Disponible desde las " + sdf.format(new Date(tareaActual.getHoraInicio())));
        } else if (!noHaTerminado) {
            tvTimerTarea.setText("⌛ Tiempo vencido");
        } else {
            tvTimerTarea.setText(sdf.format(new Date(tareaActual.getHoraFin())) + " es el límite");
        }
    }
    private void iniciarVerificadorPeriodico() {
        verificadorEstado = new Runnable() {
            @Override
            public void run() {
                actualizarEstadoBotonIniciar();
                handler.postDelayed(this, 1000); //revisa cada segundo
            }
        };
        handler.post(verificadorEstado);
    }
}