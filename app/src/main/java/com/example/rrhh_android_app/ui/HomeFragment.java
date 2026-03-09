package com.example.rrhh_android_app.ui;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
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
import androidx.navigation.Navigation;

import com.example.rrhh_android_app.R;
import com.example.rrhh_android_app.api.RetrofitClient;
import com.example.rrhh_android_app.model.EstadoResponse;
import com.example.rrhh_android_app.model.LocationRequest;
import com.example.rrhh_android_app.notifications.FichajeScheduler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

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
    private String token;
    private boolean estaFichado = false;
    private NfcAdapter nfcAdapter;

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

        // Arrancar worker de notificaciones
        FichajeScheduler.programarWorker(getContext());

        actualizarEstado();
        centrarMapaEnUsuario();

        btnFichar.setOnClickListener(v -> {
            if (!estaFichado) obtenerUbicacionYFichar();
            else realizarSalida();
        });

        // NFC: mostrar diálogo para acercar tarjeta
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

    // Llamado desde MainActivity cuando se detecta una tag NFC
    public void procesarNfc() {
        if (!estaFichado) {
            // Fichar entrada por NFC (sin GPS)
            RetrofitClient.getApiService().ficharEntrada(token, new LocationRequest(0, 0))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "¡Entrada NFC registrada!", Toast.LENGTH_SHORT).show();
                                actualizarEstado();
                            } else {
                                try {
                                    String err = response.errorBody().string();
                                    Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), "Error al fichar", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            realizarSalida();
        }
    }

    private void centrarMapaEnUsuario() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                map.getController().setCenter(startPoint);
                tvCoords.setText(String.format("Lat: %.5f  Lon: %.5f", location.getLatitude(), location.getLongitude()));
                Marker startMarker = new Marker(map);
                startMarker.setPosition(startPoint);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                startMarker.setTitle("Tú estás aquí");
                map.getOverlays().clear();
                map.getOverlays().add(startMarker);
            }
        });
    }

    private void actualizarEstado() {
        RetrofitClient.getApiService().getEstado(token).enqueue(new Callback<EstadoResponse>() {
            @Override
            public void onResponse(Call<EstadoResponse> call, Response<EstadoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    estaFichado = response.body().isFichado();
                } else {
                    estaFichado = false;
                }
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

    private void obtenerUbicacionYFichar() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LocationRequest loc = new LocationRequest(location.getLatitude(), location.getLongitude());
                RetrofitClient.getApiService().ficharEntrada(token, loc).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "¡Entrada registrada!", Toast.LENGTH_SHORT).show();
                            actualizarEstado();
                        } else {
                            try {
                                String errorMsg = response.errorBody().string();
                                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(getContext(), "Error en el servidor", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "No se pudo obtener la ubicación GPS", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void realizarSalida() {
        RetrofitClient.getApiService().ficharSalida(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Salida registrada", Toast.LENGTH_SHORT).show();
                    actualizarEstado();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }
}