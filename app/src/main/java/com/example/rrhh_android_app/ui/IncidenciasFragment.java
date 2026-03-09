package com.example.rrhh_android_app.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rrhh_android_app.R;
import com.example.rrhh_android_app.api.RetrofitClient;
import com.example.rrhh_android_app.model.IncidenciaRequest;
import com.example.rrhh_android_app.model.IncidenciaResponse;
import com.example.rrhh_android_app.ui.adapter.IncidenciasAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncidenciasFragment extends Fragment {

    private EditText etDescripcion;
    private Button btnEnviar;
    private RecyclerView rvIncidencias;
    private IncidenciasAdapter adapter;
    private String token;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incidencias, container, false);

        etDescripcion = view.findViewById(R.id.etDescripcion);
        btnEnviar = view.findViewById(R.id.btnEnviarIncidencia);
        rvIncidencias = view.findViewById(R.id.rvIncidencias);

        SharedPreferences pref = getActivity().getSharedPreferences("RRHH_PREFS", Context.MODE_PRIVATE);
        token = "Bearer " + pref.getString("token", "");

        adapter = new IncidenciasAdapter(new ArrayList<>());
        rvIncidencias.setLayoutManager(new LinearLayoutManager(getContext()));
        rvIncidencias.setAdapter(adapter);

        cargarIncidencias();

        btnEnviar.setOnClickListener(v -> {
            String desc = etDescripcion.getText().toString().trim();
            if (desc.isEmpty()) {
                Toast.makeText(getContext(), "Escribe una descripción", Toast.LENGTH_SHORT).show();
                return;
            }
            enviarIncidencia(desc);
        });

        return view;
    }

    private void cargarIncidencias() {
        RetrofitClient.getApiService().getMisIncidencias(token).enqueue(new Callback<List<IncidenciaResponse>>() {
            @Override
            public void onResponse(Call<List<IncidenciaResponse>> call, Response<List<IncidenciaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.actualizar(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<IncidenciaResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarIncidencia(String descripcion) {
        RetrofitClient.getApiService().crearIncidencia(token, new IncidenciaRequest(descripcion))
                .enqueue(new Callback<IncidenciaResponse>() {
                    @Override
                    public void onResponse(Call<IncidenciaResponse> call, Response<IncidenciaResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Incidencia registrada", Toast.LENGTH_SHORT).show();
                            etDescripcion.setText("");
                            cargarIncidencias();
                        } else {
                            Toast.makeText(getContext(), "Error al registrar", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<IncidenciaResponse> call, Throwable t) {
                        Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}