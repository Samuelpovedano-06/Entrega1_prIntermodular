package com.example.rrhh_android_app.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FichajeAlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_RECORDAR_SALIDA  = "com.example.rrhh_android_app.RECORDAR_SALIDA";
    public static final String ACTION_RECORDAR_ENTRADA = "com.example.rrhh_android_app.RECORDAR_ENTRADA";
    private static final String TAG = "FichajeAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String hora   = intent.getStringExtra("hora");
        Log.d(TAG, "Alarma recibida: " + action + " hora: " + hora);

        SharedPreferences pref = context.getSharedPreferences("RRHH_PREFS", Context.MODE_PRIVATE);
        String token = pref.getString("token", null);
        if (token == null) {
            FichajeAlarmManager.cancelarRecordatorioEntrada(context);
            FichajeAlarmManager.cancelarRecordatorioSalida(context);
            return;
        }

        // goAsync para poder hacer red fuera del hilo principal
        PendingResult pendingResult = goAsync();
        String bearerToken = "Bearer " + token;

        new Thread(() -> {
            try {
                boolean salidaHoy = comprobarSalidaHoy(bearerToken);
                boolean fichado   = comprobarFichado(bearerToken);

                Log.d(TAG, "salidaHoy=" + salidaHoy + " fichado=" + fichado);

                if (ACTION_RECORDAR_ENTRADA.equals(action)) {
                    // Si ya hay salida hoy o ya está fichado → no notificar
                    if (salidaHoy || fichado) {
                        Log.d(TAG, "Entrada innecesaria, cancelando.");
                        FichajeAlarmManager.cancelarRecordatorioEntrada(context);
                    } else {
                        NotificationHelper.enviarNotificacion(context, 1,
                                "⏰ Recuerda fichar la entrada",
                                "Tu hora de entrada era las " + hora + ". ¡No olvides fichar!");
                        FichajeAlarmManager.reprogramarEntrada(context, hora);
                    }

                } else if (ACTION_RECORDAR_SALIDA.equals(action)) {
                    // Si ya hay salida hoy o ya NO está fichado → no notificar
                    if (salidaHoy || !fichado) {
                        Log.d(TAG, "Salida innecesaria, cancelando.");
                        FichajeAlarmManager.cancelarRecordatorioSalida(context);
                    } else {
                        NotificationHelper.enviarNotificacion(context, 2,
                                "⏰ Recuerda fichar la salida",
                                "Tu hora de salida era las " + hora + ". ¡No olvides fichar!");
                        FichajeAlarmManager.reprogramarSalida(context, hora);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error en receiver: " + e.getMessage(), e);
            } finally {
                pendingResult.finish();
            }
        }).start();
    }

    private boolean comprobarSalidaHoy(String bearerToken) {
        try {
            Calendar cal = Calendar.getInstance();
            String hoy = String.format("%04d-%02d-%02d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH));

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://samuelillo777.eu.pythonanywhere.com/api/presencia/mis-registros?desde=" + hoy + "&hasta=" + hoy)
                    .addHeader("Authorization", bearerToken)
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) return false;

            JSONArray arr = new JSONArray(response.body().string());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject reg = arr.getJSONObject(i);
                String salida = reg.optString("hora_salida", "");
                if (!salida.isEmpty() && !salida.equals("null")) {
                    Log.d(TAG, "Salida hoy encontrada: " + salida);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error comprobarSalidaHoy: " + e.getMessage());
        }
        return false;
    }

    private boolean comprobarFichado(String bearerToken) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://samuelillo777.eu.pythonanywhere.com/api/presencia/estado")
                    .addHeader("Authorization", bearerToken)
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) return false;

            JSONObject obj = new JSONObject(response.body().string());
            // Fichado = tiene hora_entrada y NO tiene hora_salida (igual que EstadoResponse)
            String entrada = obj.optString("hora_entrada", "null");
            String salida  = obj.optString("hora_salida",  "null");
            return !entrada.equals("null") && !entrada.isEmpty()
                    && (salida.equals("null") || salida.isEmpty());

        } catch (Exception e) {
            Log.e(TAG, "Error comprobarFichado: " + e.getMessage());
        }
        return false;
    }
}