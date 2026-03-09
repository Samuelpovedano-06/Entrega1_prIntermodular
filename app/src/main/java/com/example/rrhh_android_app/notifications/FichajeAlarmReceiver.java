package com.example.rrhh_android_app.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class FichajeAlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_RECORDAR_SALIDA = "com.example.rrhh_android_app.RECORDAR_SALIDA";
    public static final String ACTION_RECORDAR_ENTRADA = "com.example.rrhh_android_app.RECORDAR_ENTRADA";
    private static final String TAG = "FichajeAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String hora = intent.getStringExtra("hora");
        Log.d(TAG, "Alarma recibida: " + action + " hora: " + hora);

        if (ACTION_RECORDAR_SALIDA.equals(action)) {
            NotificationHelper.enviarNotificacion(context, 2,
                    "⏰ Recuerda fichar la salida",
                    "Tu hora de salida era las " + hora + ". ¡No olvides fichar!");
            // Reprogramar la siguiente en 5 minutos
            FichajeAlarmManager.reprogramarSalida(context, hora);

        } else if (ACTION_RECORDAR_ENTRADA.equals(action)) {
            NotificationHelper.enviarNotificacion(context, 1,
                    "⏰ Recuerda fichar la entrada",
                    "Tu hora de entrada era las " + hora + ". ¡No olvides fichar!");
            // Reprogramar la siguiente en 5 minutos
            FichajeAlarmManager.reprogramarEntrada(context, hora);
        }
    }
}