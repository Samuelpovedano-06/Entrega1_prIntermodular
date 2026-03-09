package com.example.rrhh_android_app.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class FichajeAlarmManager {

    private static final String TAG = "FichajeAlarmManager";
    private static final long INTERVALO_5MIN = 5 * 60 * 1000L;
    private static final int ID_ALARMA_SALIDA = 200;
    private static final int ID_ALARMA_ENTRADA = 201;

    /**
     * Activa la alarma repetitiva de recordatorio de salida cada 5 minutos.
     */
    public static void activarRecordatorioSalida(Context context, String horaSalida) {
        Log.d(TAG, "Activando recordatorio salida: " + horaSalida);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, FichajeAlarmReceiver.class);
        intent.setAction(FichajeAlarmReceiver.ACTION_RECORDAR_SALIDA);
        intent.putExtra("hora", horaSalida);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                ID_ALARMA_SALIDA,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Lanzar inmediatamente y luego cada 5 minutos
        long ahora = System.currentTimeMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ahora, pendingIntent);
        } else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, ahora, INTERVALO_5MIN, pendingIntent);
        }
    }

    /**
     * Activa la alarma repetitiva de recordatorio de entrada cada 5 minutos.
     */
    public static void activarRecordatorioEntrada(Context context, String horaEntrada) {
        Log.d(TAG, "Activando recordatorio entrada: " + horaEntrada);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, FichajeAlarmReceiver.class);
        intent.setAction(FichajeAlarmReceiver.ACTION_RECORDAR_ENTRADA);
        intent.putExtra("hora", horaEntrada);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                ID_ALARMA_ENTRADA,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long ahora = System.currentTimeMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ahora, pendingIntent);
        } else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, ahora, INTERVALO_5MIN, pendingIntent);
        }
    }

    /**
     * Cancela el recordatorio de salida (llamar cuando el usuario ficha salida).
     */
    public static void cancelarRecordatorioSalida(Context context) {
        Log.d(TAG, "Cancelando recordatorio salida");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, FichajeAlarmReceiver.class);
        intent.setAction(FichajeAlarmReceiver.ACTION_RECORDAR_SALIDA);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                ID_ALARMA_SALIDA,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    /**
     * Cancela el recordatorio de entrada (llamar cuando el usuario ficha entrada).
     */
    public static void cancelarRecordatorioEntrada(Context context) {
        Log.d(TAG, "Cancelando recordatorio entrada");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, FichajeAlarmReceiver.class);
        intent.setAction(FichajeAlarmReceiver.ACTION_RECORDAR_ENTRADA);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                ID_ALARMA_ENTRADA,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    /**
     * Reprograma la siguiente alarma exacta a los 5 minutos.
     * Llamar desde FichajeAlarmReceiver después de cada notificación (API >= M).
     */
    public static void reprogramarSalida(Context context, String horaSalida) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, FichajeAlarmReceiver.class);
        intent.setAction(FichajeAlarmReceiver.ACTION_RECORDAR_SALIDA);
        intent.putExtra("hora", horaSalida);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                ID_ALARMA_SALIDA,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long en5min = System.currentTimeMillis() + INTERVALO_5MIN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, en5min, pendingIntent);
        }
    }

    public static void reprogramarEntrada(Context context, String horaEntrada) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, FichajeAlarmReceiver.class);
        intent.setAction(FichajeAlarmReceiver.ACTION_RECORDAR_ENTRADA);
        intent.putExtra("hora", horaEntrada);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                ID_ALARMA_ENTRADA,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long en5min = System.currentTimeMillis() + INTERVALO_5MIN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, en5min, pendingIntent);
        }
    }
}