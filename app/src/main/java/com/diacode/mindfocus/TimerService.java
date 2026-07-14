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
    private CountDownTimer countDownTimer; //para el temporizador regresivo
    private long timeLeftMillis;
    private boolean isRunning = false;
    //permite comunicar el fragment con el service
    private final IBinder binder = new TimerBinder(); //Para poder conectar el Fragment con el service
    private static final String WARNING_CHANNEL_ID = "warning_channel";
    private static final int WARNING_NOTIFICATION_ID = 3;
    private static final long WARNING_THRESHOLD = 15 * 60 * 1000L; // 15 min
    private static final long HOLGURA_MILLIS = 10 * 60 * 1000L;    // 10 min
    private boolean warningSent = false;
    private boolean finalizadoManualmente = false;
    // contexto de la tarea
    private int tareaId = -1;
    private String nombreTarea = "";
    private boolean modoTarea = false;
    public class TimerBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        } //Retorna instanacia actual del service
    }
    //interfaz para notificar al fragment de los cambios
    public interface TimerListener { //comunicar el service con el fragment
        void onTimerTick(long millisLeft);
        void onTimerFinish(boolean finalizadoManualmente);
    }

    private TimerListener timerListener;

    public void setTimerListener(TimerListener listener) {
        this.timerListener = listener;
    } //Fragment aqui para escuchar eventos

    //ciclo de vida del service
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel(); // Crear canal al iniciar / notificaciones
        createWarningChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { //METODO PRINCIPAL
        tareaId = intent.getIntExtra("tareaId", -1);
        nombreTarea = intent.getStringExtra("nombreTarea");
        if (nombreTarea == null) nombreTarea = "tu tarea";
        // si viene "horaFin" (timestamp absoluto), calculamos la duración real
        // si no viene (modo Pomodoro clásico), usamos "duration" como antes
        long horaFin = intent.getLongExtra("horaFin", -1L);
        modoTarea = horaFin > 0;
        if (horaFin > 0) {
            timeLeftMillis = horaFin - System.currentTimeMillis();
            if (timeLeftMillis < 0) timeLeftMillis = 0;
        } else {
            timeLeftMillis = intent.getLongExtra("duration", 25 * 60 * 1000L);
        }
        warningSent = false;
        finalizadoManualmente = false;
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
    public void onDestroy() { //Cancela timer al destruir el service
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    //logica del timer - Comienza el contador
    private void startTimer() { //corre pomodoro
        countDownTimer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                // aviso de 15 minutos antes (una sola vez)
                if (modoTarea && !warningSent && millisUntilFinished <= WARNING_THRESHOLD) {
                    warningSent = true;
                    showWarningNotification();
                }
                // Actualizar notificación con el tiempo restante
                String timeText = formatTime(millisUntilFinished);
                updateNotification("🔥 Enfocado — " + timeText); //Actualizar notificación

                // Notificar al Fragment si está escuchando
                if (timerListener != null) {
                    timerListener.onTimerTick(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() { //cuando termina el timer
                isRunning = false;
                timeLeftMillis = 0;
                // Notificación de fin
                if (modoTarea) {
                    showFinishNotification(); // "completada" o "tiempo agotado sin finalizar"
                } else {
                    showPomodoroFinishNotification(); // mensaje genérico del Pomodoro
                }
                if (timerListener != null) {
                    timerListener.onTimerFinish(finalizadoManualmente);
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

    public void resumeTimer() { //reanuda el timer
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
    //el usuario presionaa finalizar manualmente
    public void finalizarManualmente() {
        finalizadoManualmente = true;
        if (countDownTimer != null) countDownTimer.cancel();
        isRunning = false;
        if (timerListener != null) {
            timerListener.onTimerFinish(true);
        }
        stopSelf();
    }
    //si faltan 10 minutos o menos
    public boolean puedeFinalizar() {
        return timeLeftMillis <= HOLGURA_MILLIS;
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
    private void createWarningChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    WARNING_CHANNEL_ID, "Avisos de tiempo", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Avisa cuando falta poco para vencer una tarea");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    //construnimos la notificacion base
    private Notification buildNotification(String text) {
        //al tocal la notificacion volveremos a la aplicacion
        Intent openAppIntent = new Intent(this, ActivityPrincipal.class);
        //cuando el usu toca la notificación vuelve a la app
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
    private void showWarningNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        Intent openAppIntent = new Intent(this, ActivityPrincipal.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification warningNotification = new NotificationCompat.Builder(this, WARNING_CHANNEL_ID)
                .setContentTitle("⏰ ¡Quedan 15 minutos!")
                .setContentText("\"" + nombreTarea + "\" está por vencer. ¡Vamos que se puede!")
                .setSmallIcon(R.drawable.ic_brain)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        manager.notify(WARNING_NOTIFICATION_ID, warningNotification);
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
        String mensaje = finalizadoManualmente
                ? "Marcaste \"" + nombreTarea + "\" como completada. ¡Buen trabajo!"
                : "Se acabó el tiempo de \"" + nombreTarea + "\" sin finalizar.";
        Notification finishNotification = new NotificationCompat.Builder(this, "finish_channel")
                .setContentTitle(finalizadoManualmente ? "¡Tarea completada!" : "⌛ Tiempo agotado")
                .setContentText(mensaje)
                .setSmallIcon(R.drawable.ic_brain)
                .setAutoCancel(true)
                .build();
        manager.notify(2, finishNotification);
    }
    private void showPomodoroFinishNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel finishChannel = new NotificationChannel(
                    "finish_channel", "Timer Completado", NotificationManager.IMPORTANCE_HIGH);
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
        if (millis < 0) millis = 0;
        long minutes = millis / 1000 / 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    public boolean isModoTarea() {
        return modoTarea;
    }
}
