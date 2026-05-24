package com.diacode.mindfocus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class TimerService extends Service {

    private static final String CHANNEL_ID = "timer_channel";
    private static final int NOTIFICATION_ID = 1;

    //timer
    private CountDownTimer countDownTimer;
    private long timeLeftMillis;
    private boolean isRunning = false;

    //permite comunicar el fragment con el service
    private final IBinder binder = new TimerBinder();

    public class TimerBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        }
    }
    //interfaz para notificar al fragment de los cambios
    public interface TimerListener {
        void onTimerTick(long millisLeft);
        void onTimerFinish();
    }

    private TimerListener timerListener;

    public void setTimerListener(TimerListener listener) {
        this.timerListener = listener;
    }

    //ciclo de vida del service
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel(); // ✅ Crear canal al iniciar
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Recuperar la duración enviada desde el Fragment
        timeLeftMillis = intent.getLongExtra("duration", 25 * 60 * 1000L);
        createNotificationChannel();
        // Mostrar notificación e iniciar en primer plano
        startForeground(NOTIFICATION_ID, buildNotification("Iniciando timer..."));

        startTimer();

        // START_STICKY → si el sistema mata el servicio, lo reinicia solo
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    //logica del timer
    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;

                // Actualizar notificación con el tiempo restante
                String timeText = formatTime(millisUntilFinished);
                updateNotification("🔥 Enfocado — " + timeText);

                // Notificar al Fragment si está escuchando
                if (timerListener != null) {
                    timerListener.onTimerTick(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                isRunning = false;

                // Notificación de fin
                showFinishNotification();

                if (timerListener != null) {
                    timerListener.onTimerFinish();
                }

                stopSelf(); // El servicio se detiene solo
            }
        }.start();

        isRunning = true;
    }

    public void pauseTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        isRunning = false;
        updateNotification("⏸ Timer pausado");
    }

    public void resumeTimer() {
        if (!isRunning && timeLeftMillis > 0) {
            startTimer();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public long getTimeLeftMillis() {
        return timeLeftMillis;
    }

    //notificaciones
    //creamos el canal
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer de Enfoque",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Muestra el progreso del timer");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    //construnimos la notificacion base
    private Notification buildNotification(String text) {
        //al tocal la notificacion volveremos a la aplicacion
        Intent openAppIntent = new Intent(this, ActivityPrincipal.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MindFocus")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_brain) //icono
                .setContentIntent(pendingIntent)
                .setOngoing(true) //no se puede descartar mientras corre
                .setSilent(true)  //sin sonico
                .build();
    }

    //actualizar el texto de la notificación
    private void updateNotification(String text) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, buildNotification(text));
    }

    private void showFinishNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel finishChannel = new NotificationChannel(
                    "finish_channel",
                    "Timer Completado",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(finishChannel);
        }
        Notification finishNotification = new NotificationCompat.Builder(this, "finish_channel")
                .setContentTitle("¡Tiempo completado!")
                .setContentText("Tu sesión de enfoque ha terminado. ¡Buen trabajo!")
                .setSmallIcon(R.drawable.ic_brain)
                .setAutoCancel(true)
                .build();
        manager.notify(2, finishNotification);
    }

    //helper
    private String formatTime(long millis) {
        long minutes = millis / 1000 / 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

}
