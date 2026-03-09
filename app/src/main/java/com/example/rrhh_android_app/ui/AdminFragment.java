package com.example.rrhh_android_app.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rrhh_android_app.R;
import com.example.rrhh_android_app.api.RetrofitClient;
import com.example.rrhh_android_app.model.EmpleadoResponse;
import com.example.rrhh_android_app.ui.adapter.EmpleadosAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminFragment extends Fragment {

    private RecyclerView rvEmpleados;
    private EmpleadosAdapter adapter;
    private String token;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        rvEmpleados = view.findViewById(R.id.rvEmpleados);

        SharedPreferences pref = getActivity().getSharedPreferences("RRHH_PREFS", Context.MODE_PRIVATE);
        token = "Bearer " + pref.getString("token", "");

        adapter = new EmpleadosAdapter(new ArrayList<>());
        rvEmpleados.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEmpleados.setAdapter(adapter);

        cargarEmpleados();

        return view;
    }

    private void cargarEmpleados() {
        RetrofitClient.getApiService().getEmpleados(token).enqueue(new Callback<List<EmpleadoResponse>>() {
            @Override
            public void onResponse(Call<List<EmpleadoResponse>> call, Response<List<EmpleadoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.actualizar(response.body());
                } else {
                    Toast.makeText(getContext(), "Sin permisos o error", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<EmpleadoResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}