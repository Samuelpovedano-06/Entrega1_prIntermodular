package com.example.rrhh_android_app.ui;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.rrhh_android_app.R;
import com.example.rrhh_android_app.api.RetrofitClient;
import com.example.rrhh_android_app.model.EstadoResponse;
import com.example.rrhh_android_app.model.LocationRequest;
import com.example.rrhh_android_app.notifications.FichajeAlarmManager;
import com.example.rrhh_android_app.notifications.FichajeScheduler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private MapView map;
    private TextView tvCoords, tvNombre;
    private Button btnFichar, btnNfc;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private String token;
    private boolean estaFichado = false;
    private NfcAdapter nfcAdapter;
    private double latActual = 0, lonActual = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvCoords = view.findViewById(R.id.tvCoords);
        tvNombre = view.findViewById(R.id.tvNombre);
        map = view.findViewById(R.id.mapview);
        btnFichar = view.findViewById(R.id.btnFichar);
        btnNfc = view.findViewById(R.id.btnNfc);

        map.setMultiTouchControls(true);
        map.getController().setZoom(18.0);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        nfcAdapter = NfcAdapter.getDefaultAdapter(getContext());

        SharedPreferences pref = getActivity().getSharedPreferences("RRHH_PREFS", Context.MODE_PRIVATE);
        token = "Bearer " + pref.getString("token", "");
        String nombre = pref.getString("nombre", "");
        if (tvNombre != null) tvNombre.setText("Hola, " + nombre);

        FichajeScheduler.programarWorker(getContext());
        actualizarEstado();
        iniciarActualizacionUbicacion();

        btnFichar.setOnClickListener(v -> {
            if (!estaFichado) ficharEntradaConUbicacionActual();
            else realizarSalida();
        });

        btnNfc.setOnClickListener(v -> {
            if (nfcAdapter == null) {
                Toast.makeText(getContext(), "Este dispositivo no tiene NFC", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!nfcAdapter.isEnabled()) {
                Toast.makeText(getContext(), "Activa el NFC en ajustes", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getContext(), "Acerca la tarjeta NFC para fichar", Toast.LENGTH_LONG).show();
        });

        return view;
    }

    private void iniciarActualizacionUbicacion() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Configurar solicitud de ubicación en tiempo real
        com.google.android.gms.location.LocationRequest locationRequest = new com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(3000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null || getContext() == null) return;
                android.location.Location location = locationResult.getLastLocation();
                if (location != null) {
                    latActual = location.getLatitude();
                    lonActual = location.getLongitude();

                    // Actualizar texto de coordenadas
                    tvCoords.setText(String.format("Lat: %.5f  Lon: %.5f", latActual, lonActual));

                    // Actualizar marcador en el mapa
                    GeoPoint punto = new GeoPoint(latActual, lonActual);
                    map.getController().setCenter(punto);
                    Marker marker = new Marker(map);
                    marker.setPosition(punto);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    marker.setTitle("Tú estás aquí");
                    map.getOverlays().clear();
                    map.getOverlays().add(marker);
                    map.invalidate();
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void ficharEntradaConUbicacionActual() {
        if (latActual == 0 && lonActual == 0) {
            Toast.makeText(getContext(), "Obteniendo ubicación GPS...", Toast.LENGTH_SHORT).show();
            return;
        }
        com.example.rrhh_android_app.model.LocationRequest loc =
                new com.example.rrhh_android_app.model.LocationRequest(latActual, lonActual);
        RetrofitClient.getApiService().ficharEntrada(token, loc).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "¡Entrada registrada!", Toast.LENGTH_SHORT).show();
                    FichajeAlarmManager.cancelarRecordatorioEntrada(getContext());
                    actualizarEstado();
                } else {
                    mostrarErrorServidor(response);
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Sin conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void procesarNfc() {
        if (!estaFichado) {
            com.example.rrhh_android_app.model.LocationRequest loc =
                    new com.example.rrhh_android_app.model.LocationRequest(latActual, lonActual);
            RetrofitClient.getApiService().ficharEntrada(token, loc)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "¡Entrada NFC registrada!", Toast.LENGTH_SHORT).show();
                                FichajeAlarmManager.cancelarRecordatorioEntrada(getContext());
                                actualizarEstado();
                            } else {
                                mostrarErrorServidor(response);
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(getContext(), "Sin conexión", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            realizarSalida();
        }
    }

    private void actualizarEstado() {
        RetrofitClient.getApiService().getEstado(token).enqueue(new Callback<EstadoResponse>() {
            @Override
            public void onResponse(Call<EstadoResponse> call, Response<EstadoResponse> response) {
                estaFichado = response.isSuccessful() && response.body() != null && response.body().isFichado();
                actualizarBoton();
            }
            @Override
            public void onFailure(Call<EstadoResponse> call, Throwable t) {
                estaFichado = false;
                actualizarBoton();
            }
        });
    }

    private void actualizarBoton() {
        if (estaFichado) {
            btnFichar.setText("FICHAR SALIDA");
            btnFichar.setBackgroundColor(Color.RED);
            btnNfc.setText("SALIDA POR NFC");
        } else {
            btnFichar.setText("FICHAR ENTRADA");
            btnFichar.setBackgroundColor(Color.GREEN);
            btnNfc.setText("ENTRADA POR NFC");
        }
    }

    private void realizarSalida() {
        RetrofitClient.getApiService().ficharSalida(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Salida registrada", Toast.LENGTH_SHORT).show();
                    FichajeAlarmManager.cancelarRecordatorioSalida(getContext());
                    actualizarEstado();
                } else {
                    mostrarErrorServidor(response);
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Sin conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarErrorServidor(Response<?> response) {
        try {
            String body = response.errorBody().string();
            String mensaje = "Operación no permitida";

            // Flask-smorest devuelve "message" o "errors"
            if (body.contains("\"message\"")) {
                mensaje = body.split("\"message\"\\s*:\\s*\"")[1].split("\"")[0];
            } else if (body.contains("\"msg\"")) {
                mensaje = body.split("\"msg\"\\s*:\\s*\"")[1].split("\"")[0];
            } else if (body.contains("\"mensaje\"")) {
                mensaje = body.split("\"mensaje\"\\s*:\\s*\"")[1].split("\"")[0];
            }

            String mensajeFinal = decodificarUnicode(mensaje);
            if (getContext() != null) {
                Toast.makeText(getContext(), mensajeFinal, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Operación no permitida", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String decodificarUnicode(String texto) {
        try {
            return new String(texto.getBytes("ISO-8859-1"), "UTF-8");
        } catch (Exception e) {
            // Fallback: reemplazar secuencias \\uXXXX manualmente
            return texto
                    .replace("\\u00e1", "á").replace("\\u00e9", "é")
                    .replace("\\u00ed", "í").replace("\\u00f3", "ó")
                    .replace("\\u00fa", "ú").replace("\\u00f1", "ñ")
                    .replace("\\u00c1", "Á").replace("\\u00c9", "É")
                    .replace("\\u00cd", "Í").replace("\\u00d3", "Ó")
                    .replace("\\u00da", "Ú").replace("\\u00d1", "Ñ");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
        iniciarActualizacionUbicacion();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
        // Detener actualizaciones para ahorrar batería
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}