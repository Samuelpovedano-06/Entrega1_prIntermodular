package com.example.rrhh_android_app.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.rrhh_android_app.R;

public class NotificationHelper {

    public static final String CHANNEL_ID = "rrhh_fichaje";

    public static void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Avisos de fichaje",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones cuando no has fichado a tu hora");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static void enviarNotificacion(Context context, int id, String titulo, String mensaje) {
        crearCanal(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(id, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}