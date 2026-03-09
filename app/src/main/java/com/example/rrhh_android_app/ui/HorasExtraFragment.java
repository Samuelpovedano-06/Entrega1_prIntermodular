package com.example.rrhh_android_app.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.rrhh_android_app.R;
import com.example.rrhh_android_app.api.RetrofitClient;
import com.example.rrhh_android_app.model.ResumenMensualResponse;


import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HorasExtraFragment extends Fragment {

    private EditText etMes;
    private Button btnConsultar;
    private LinearLayout layoutResultado;
    private TextView tvMes, tvHorasTrabajadas, tvHorasTeoicas, tvHorasExtra, tvRegistros;
    private String token;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_horas_extra, container, false);

        etMes = view.findViewById(R.id.etMes);
        btnConsultar = view.findViewById(R.id.btnConsultar);
        layoutResultado = view.findViewById(R.id.layoutResultado);
        tvMes = view.findViewById(R.id.tvMes);
        tvHorasTrabajadas = view.findViewById(R.id.tvHorasTrabajadas);
        tvHorasTeoicas = view.findViewById(R.id.tvHorasTeoicas);
        tvHorasExtra = view.findViewById(R.id.tvHorasExtra);
        tvRegistros = view.findViewById(R.id.tvRegistros);

        SharedPreferences pref = getActivity().getSharedPreferences("RRHH_PREFS", Context.MODE_PRIVATE);
        token = "Bearer " + pref.getString("token", "");

        // Poner mes actual por defecto
        Calendar cal = Calendar.getInstance();
        String mesActual = String.format("%d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
        etMes.setText(mesActual);

        btnConsultar.setOnClickListener(v -> {
            String mes = etMes.getText().toString().trim();
            if (mes.isEmpty()) {
                Toast.makeText(getContext(), "Introduce un mes (YYYY-MM)", Toast.LENGTH_SHORT).show();
                return;
            }
            consultarResumen(mes);
        });

        return view;
    }

    private void consultarResumen(String mes) {
        RetrofitClient.getApiService().getResumenMensual(token, mes).enqueue(new Callback<ResumenMensualResponse>() {
            @Override
            public void onResponse(Call<ResumenMensualResponse> call, Response<ResumenMensualResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResumenMensualResponse r = response.body();
                    layoutResultado.setVisibility(View.VISIBLE);
                    tvMes.setText("Mes: " + r.getMes());
                    tvHorasTrabajadas.setText("Horas trabajadas: " + r.getHorasTrabajadas() + "h");
                    tvHorasTeoicas.setText("Horas teóricas: " + r.getHorasTeoicas() + "h");
                    double extra = r.getHorasExtra();
                    tvHorasExtra.setText("Horas extra: " + extra + "h");
                    tvHorasExtra.setTextColor(extra >= 0 ? Color.parseColor("#2E7D32") : Color.RED);
                    tvRegistros.setText("Registros completados: " + r.getRegistros());
                } else {
                    Toast.makeText(getContext(), "Error al consultar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResumenMensualResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}