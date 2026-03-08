package com.example.rrhh_android_app.ui;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
    private TextView tvCoords;
    private Button btnFichar, btnLogout;
    private FusedLocationProviderClient fusedLocationClient;
    private String token;
    private boolean estaFichado = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvCoords = view.findViewById(R.id.tvCoords);
        map = view.findViewById(R.id.mapview);
        btnFichar = view.findViewById(R.id.btnFichar);
        btnLogout = view.findViewById(R.id.btnLogout);

        map.setMultiTouchControls(true);
        map.getController().setZoom(18.0);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        SharedPreferences pref = getActivity().getSharedPreferences("RRHH_PREFS", Context.MODE_PRIVATE);
        token = "Bearer " + pref.getString("token", "");

        actualizarEstado();
        centrarMapaEnUsuario();

        btnFichar.setOnClickListener(v -> {
            if (!estaFichado) obtenerUbicacionYFichar();
            else realizarSalida();
        });

        btnLogout.setOnClickListener(v -> cerrarSesion());

        return view;
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

                tvCoords.setText(String.format("Lat: %.5f\nLon: %.5f", location.getLatitude(), location.getLongitude()));

                Marker startMarker = new Marker(map);
                startMarker.setPosition(startPoint);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                startMarker.setTitle("Tú estás aquí");
                map.getOverlays().clear();
                map.getOverlays().add(startMarker);
            }
        });
    }

    private void cerrarSesion() {
        SharedPreferences pref = getActivity().getSharedPreferences("RRHH_PREFS", Context.MODE_PRIVATE);
        pref.edit().clear().apply();

        Navigation.findNavController(getView()).navigate(R.id.loginFragment);
        Toast.makeText(getContext(), "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
    }

    private void actualizarEstado() {
        RetrofitClient.getApiService().getEstado(token).enqueue(new Callback<EstadoResponse>() {
            @Override
            public void onResponse(Call<EstadoResponse> call, Response<EstadoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    estaFichado = response.body().isFichado();
                } else {
                    // 404 u otro error = no fichado hoy
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
        } else {
            btnFichar.setText("FICHAR ENTRADA");
            btnFichar.setBackgroundColor(Color.GREEN);
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
                            Toast.makeText(getContext(), "¡Entrada registrada con éxito!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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