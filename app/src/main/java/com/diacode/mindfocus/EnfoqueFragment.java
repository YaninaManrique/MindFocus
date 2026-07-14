package com.diacode.mindfocus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class EnfoqueFragment extends Fragment {

    // --- Timer modes (in milliseconds) ---
    private static final long POMODORO_DURATION = 25 * 60 * 1000L;
    private static final long CORTO_DURATION    = 10 * 60 * 1000L;
    private static final long MINI_DURATION     =  10 * 1000L;//5 * 60 * 1000L
    private static final long BREAK_DURATION    =  5 * 60 * 1000L;

    // --- Views ---
    private TimerCircleView timerCircleView;
    private TextView tvTimer, tvFocusLabel;
    private TextView tabPomodoro, tabCorto, tabMini;
    private Button btnReiniciar, btnPausar;
    private TextView btnCambiarDescanso;

    // --- State ---
    private CountDownTimer countDownTimer;
    private boolean isRunning   = false;
    private boolean isBreakMode = false;
    private long totalDuration  = POMODORO_DURATION;
    private long timeLeftMillis = POMODORO_DURATION;

    // --- Gesture ---
    private GestureDetector gestureDetector;

    // --- Sensor ---
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private SensorEventListener proximityListener;
    private boolean timerStartedByUser = false;

    // tab actual: 0=Pomodoro, 1=Corto, 2=Mini
    private int currentTab = 0;

    private TimerService timerService;
    private boolean serviceBound = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enfoque, container, false);
        bindViews(view);
        setupTabListeners();
        setupButtonListeners();
        setupGestures(view);//gestures
        setupProximitySensor();//proximidad
        updateTimerDisplay(timeLeftMillis);
        btnPausar.setText("▶ Iniciar");
        //startTimer();
        selectTab(tabPomodoro, tabCorto, tabMini);
        switchMode(POMODORO_DURATION, false);
        return view; // auto-start like the screenshot
    }

    private void bindViews(View view) {
        timerCircleView      = view.findViewById(R.id.timerCircleView);
        tvTimer              = view.findViewById(R.id.tvTimer);
        tvFocusLabel         = view.findViewById(R.id.tvFocusLabel);
        tabPomodoro          = view.findViewById(R.id.tabPomodoro);
        tabCorto             = view.findViewById(R.id.tabCorto);
        tabMini              = view.findViewById(R.id.tabMini);
        btnReiniciar         = view.findViewById(R.id.btnReiniciar);
        btnPausar            = view.findViewById(R.id.btnPausar);
        btnCambiarDescanso   = view.findViewById(R.id.btnCambiarDescanso);
    }
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            TimerService.TimerBinder binder = (TimerService.TimerBinder) iBinder;
            timerService = binder.getService();
            serviceBound = true;

            //escuchas los ticks del timer desde el Service
            timerService.setTimerListener(new TimerService.TimerListener() {
                @Override
                public void onTimerTick(long millisLeft) {
                    //actualziar UI desde el hilo principal
                    requireActivity().runOnUiThread(() -> {
                        timeLeftMillis = millisLeft;
                        updateTimerDisplay(millisLeft);
                        float progress = millisLeft / (float) totalDuration;
                        timerCircleView.setProgress(progress);
                    });
                }
                @Override
                public void onTimerFinish(boolean finalizadoManualmente) {
                    requireActivity().runOnUiThread(() -> {
                        isRunning = false;
                        btnPausar.setText("▶ Iniciar");
                        timerCircleView.setProgress(0f);
                    });
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void startTimerService() {
        Intent intent = new Intent(requireContext(), TimerService.class);
        intent.putExtra("duration", timeLeftMillis);
        requireActivity().startService(intent);
        requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    //al deslizar el timer para la derecha o izqui se cambia al tipo de enfoque correspondiente (SEMANAA 6)
    private void setupGestures(View view) {
        gestureDetector = new GestureDetector(requireContext(),
                new GestureDetector.SimpleOnGestureListener() {

                    private static final int SWIPE_MIN_DISTANCE = 100;
                    private static final int SWIPE_MIN_VELOCITY = 100;
                    //e1 donde comenzo, e2 donde termino
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2,
                                           float velocityX, float velocityY) {
                        //la diferencia donde comenzo y donde termino
                        float diffX = e1.getX() - e2.getX();

                        //positivo izquierda
                        if (diffX > SWIPE_MIN_DISTANCE
                                && Math.abs(velocityX) > SWIPE_MIN_VELOCITY) {
                            goToNextTab();
                            return true;
                        }

                        //negativo derecha
                        if (-diffX > SWIPE_MIN_DISTANCE
                                && Math.abs(velocityX) > SWIPE_MIN_VELOCITY) {
                            goToPreviousTab();
                            return true;
                        }

                        return false;
                    }
                });
        //aca se conecto con el circulo
        timerCircleView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void goToNextTab() {
        if (currentTab < 2) {
            currentTab++;
            applyTab();
        }
    }

    private void goToPreviousTab() {
        if (currentTab > 0) {
            currentTab--;
            applyTab();
        }
    }

    private void applyTab() {
        switch (currentTab) {
            case 0:
                selectTab(tabPomodoro, tabCorto, tabMini);
                switchMode(POMODORO_DURATION, false);
                Toast.makeText(requireContext(), "Pomodoro — 25 min", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                selectTab(tabCorto, tabPomodoro, tabMini);
                switchMode(CORTO_DURATION, false);
                Toast.makeText(requireContext(), "Corto — 10 min", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                selectTab(tabMini, tabPomodoro, tabCorto);
                switchMode(MINI_DURATION, false);
                Toast.makeText(requireContext(), "Mini — 5 min", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    //SENSORES
    private void setupProximitySensor() {
        sensorManager = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);
        //obtenemos el sensor de proximidad
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (proximitySensor == null) {
            //el dispositivo no tiene sensor de proximidad
            return;
        }

        proximityListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float distance = event.values[0];
                float maxRange = proximitySensor.getMaximumRange();

                if (distance < maxRange) {
                    //mano cerca se pausa
                    if (isRunning) {
                        pauseTimer();
                        Toast.makeText(requireContext(),
                                "⏸ Timer pausado", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //mano alejada se reanuda
                    if (!isRunning && timeLeftMillis > 0 && timerStartedByUser) {//si el usuario ya inicio antes reanuda
                        startTimer();
                        Toast.makeText(requireContext(),
                                "▶ Timer reanudado", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
    }
    //ciclo de vida del sensor para que solo funciona en esta pantalla
    @Override
    public void onResume() {
        super.onResume();
        //activaar sensor cuando el fragment es visible
        if (sensorManager != null && proximitySensor != null) {
            sensorManager.registerListener(
                    proximityListener,
                    proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //desactivar sensor cuando el fragment no es visible para ahorrar bateria
        if (sensorManager != null) {
            sensorManager.unregisterListener(proximityListener);
        }
    }

    // -------------------------------------------------------------------------
    // Tab selection
    // -------------------------------------------------------------------------

    private void setupTabListeners() {
        View.OnClickListener tabClick = v -> {
            if (v.getId() == R.id.tabPomodoro) {
                selectTab(tabPomodoro, tabCorto, tabMini);
                switchMode(POMODORO_DURATION, false);
            } else if (v.getId() == R.id.tabCorto) {
                selectTab(tabCorto, tabPomodoro, tabMini);
                switchMode(CORTO_DURATION, false);
            } else {
                selectTab(tabMini, tabPomodoro, tabCorto);
                switchMode(MINI_DURATION, false);
            }
        };
        tabPomodoro.setOnClickListener(tabClick);
        tabCorto.setOnClickListener(tabClick);
        tabMini.setOnClickListener(tabClick);
    }

    private void selectTab(TextView selected, TextView other1, TextView other2) {
        selected.setBackgroundResource(R.drawable.bg_tab_selected);
        selected.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        other1.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        other1.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_text_unselected));

        other2.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        other2.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_text_unselected));
    }

    // -------------------------------------------------------------------------
    // Button listeners
    // -------------------------------------------------------------------------

    private void setupButtonListeners() {
        btnPausar.setOnClickListener(v -> {
            if (isRunning){
                pauseTimer();
                if (serviceBound && timerService != null) {
                    timerService.pauseTimer();
                }
            }
            else {
                timerStartedByUser = true;
                startTimer();
                startTimerService();
            }
        });

        btnReiniciar.setOnClickListener(v -> resetTimer());

        btnCambiarDescanso.setOnClickListener(v -> {
            isBreakMode = !isBreakMode;
            if (isBreakMode) {
                switchMode(BREAK_DURATION, true);
                btnCambiarDescanso.setText("🔥 Volver al foco");
                tvFocusLabel.setText("☕ Descansando");
            } else {
                switchMode(totalDuration, false);
                btnCambiarDescanso.setText("☕ Cambiar a descanso (5 min)");
                tvFocusLabel.setText("🔥 Enfocado");
            }
        });
    }

    // -------------------------------------------------------------------------
    // Timer logic
    // -------------------------------------------------------------------------

    private void startTimer() {
        if (timeLeftMillis <= 0) return;

        countDownTimer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                updateTimerDisplay(millisUntilFinished);
                float currentTotal = isBreakMode ? BREAK_DURATION : totalDuration;
                timerCircleView.setProgress(millisUntilFinished / currentTotal);
            }

            @Override
            public void onFinish() {
                isRunning = false;
                timeLeftMillis = 0;
                updateTimerDisplay(0);
                timerCircleView.setProgress(0f);
                btnPausar.setText("▶ Iniciar");
                Toast.makeText(requireContext(),
                        isBreakMode ? "¡Descanso terminado! 💪" : "¡Tiempo de enfoque completado! 🎉",
                        Toast.LENGTH_LONG).show();
            }
        }.start();

        isRunning = true;
        btnPausar.setText("⏸ Pausar");
    }

    private void pauseTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        isRunning = false;
        btnPausar.setText("▶ Continuar");
    }

    private void resetTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        isRunning = false;
        isBreakMode = false;
        timerStartedByUser = false;//se resetea el flag
        timeLeftMillis = totalDuration;
        btnPausar.setText("▶ Iniciar");
        tvFocusLabel.setText("🔥 Enfocado");
        btnCambiarDescanso.setText("☕ Cambiar a descanso (5 min)");
        updateTimerDisplay(totalDuration);
        timerCircleView.setProgress(1f);
    }

    private void switchMode(long duration, boolean isBreak) {
        if (countDownTimer != null) countDownTimer.cancel();
        isRunning = false;
        if (!isBreak) totalDuration = duration;
        timeLeftMillis = duration;
        btnPausar.setText("▶ Iniciar");
        updateTimerDisplay(duration);
        timerCircleView.setProgress(1f);
    }

    // -------------------------------------------------------------------------
    // Display helpers
    // -------------------------------------------------------------------------

    private void updateTimerDisplay(long millis) {
        long minutes = millis / 1000 / 60;
        long seconds = (millis / 1000) % 60;
        tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
        //desconectamos al salir del fragment
        if (serviceBound) {
            requireActivity().unbindService(serviceConnection);
            serviceBound = false;
        }
    }
}
