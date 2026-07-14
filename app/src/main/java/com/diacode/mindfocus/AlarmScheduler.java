package com.diacode.mindfocus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmScheduler {
    private static final long AVISO_ANTES_MILLIS = 3 * 60 * 1000L; // 3 minutos
    public static void programarRecordatorio(Context context, int tareaId, String nombreTarea, long horaInicio) {
        long horaAviso = horaInicio - AVISO_ANTES_MILLIS;

        // Si la hora de aviso ya pasó (por ejemplo, tarea creada con poco margen), no programar
        if (horaAviso <= System.currentTimeMillis()) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, TareaAlarmReceiver.class);
        intent.putExtra("tareaId", tareaId);
        intent.putExtra("nombreTarea", nombreTarea);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, tareaId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, horaAviso, pendingIntent);
            }
            // Si no tiene el permiso, simplemente no se programa
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, horaAviso, pendingIntent);
        }
    }
    public static void cancelarRecordatorio(Context context, int tareaId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TareaAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, tareaId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
