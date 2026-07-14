package com.diacode.mindfocus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class TareaAlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "tarea_recordatorio_channel";
    @Override
    public void onReceive(Context context, Intent intent) {
        int tareaId = intent.getIntExtra("tareaId", -1);
        String nombreTarea = intent.getStringExtra("nombreTarea");
        if (nombreTarea == null) nombreTarea = "tu tarea";

        crearCanal(context);

        Intent openAppIntent = new Intent(context, ActivityPrincipal.class);
        openAppIntent.putExtra("tareaId", tareaId); // por si quieres abrir directo el detalle
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, tareaId, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("⏰ Tu tarea está por comenzar")
                .setContentText("\"" + nombreTarea + "\" empieza en 3 minutos")
                .setSmallIcon(R.drawable.ic_brain)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(tareaId, notification); // usamos tareaId como ID único por tarea
    }
    private void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Recordatorios de tareas", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Avisa antes de que empiece una tarea");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
