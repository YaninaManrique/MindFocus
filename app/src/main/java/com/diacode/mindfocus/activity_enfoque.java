package com.diacode.mindfocus;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class activity_enfoque extends AppCompatActivity {

    // --- Timer modes (in milliseconds) ---
    private static final long POMODORO_DURATION = 25 * 60 * 1000L;
    private static final long CORTO_DURATION    = 10 * 60 * 1000L;
    private static final long MINI_DURATION     =  5 * 60 * 1000L;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enfoque);

        bindViews();
        setupTabListeners();
        setupButtonListeners();
        updateTimerDisplay(timeLeftMillis);
        startTimer(); // auto-start like the screenshot
    }

    private void bindViews() {
        timerCircleView      = findViewById(R.id.timerCircleView);
        tvTimer              = findViewById(R.id.tvTimer);
        tvFocusLabel         = findViewById(R.id.tvFocusLabel);
        tabPomodoro          = findViewById(R.id.tabPomodoro);
        tabCorto             = findViewById(R.id.tabCorto);
        tabMini              = findViewById(R.id.tabMini);
        btnReiniciar         = findViewById(R.id.btnReiniciar);
        btnPausar            = findViewById(R.id.btnPausar);
        btnCambiarDescanso   = findViewById(R.id.btnCambiarDescanso);
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
        selected.setTextColor(ContextCompat.getColor(this, R.color.white));

        other1.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        other1.setTextColor(ContextCompat.getColor(this, R.color.tab_text_unselected));

        other2.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        other2.setTextColor(ContextCompat.getColor(this, R.color.tab_text_unselected));
    }

    // -------------------------------------------------------------------------
    // Button listeners
    // -------------------------------------------------------------------------

    private void setupButtonListeners() {
        btnPausar.setOnClickListener(v -> {
            if (isRunning) pauseTimer();
            else startTimer();
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
                Toast.makeText(activity_enfoque.this,
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
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
