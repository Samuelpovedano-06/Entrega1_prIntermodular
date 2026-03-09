package com.example.rrhh_android_app.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.rrhh_android_app.R;
import com.example.rrhh_android_app.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private TextView tvNombre, tvRol;
    private Button btnCambiarPassword, btnLogout;
    private String token;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        tvNombre = view.findViewById(R.id.tvNombre);
        tvRol = view.findViewById(R.id.tvRol);
        btnCambiarPassword = view.findViewById(R.id.btnCambiarPassword);
        btnLogout = view.findViewById(R.id.btnLogout);

        SharedPreferences pref = getActivity().getSharedPreferences("RRHH_PREFS", Context.MODE_PRIVATE);
        token = "Bearer " + pref.getString("token", "");
        String nombre = pref.getString("nombre", "Usuario");
        String rol = pref.getString("rol", "");

        tvNombre.setText(nombre);
        tvRol.setText(rol);

        btnCambiarPassword.setOnClickListener(v -> solicitarCambioPassword());

        btnLogout.setOnClickListener(v -> {
            pref.edit().clear().apply();
            Navigation.findNavController(view).navigate(R.id.loginFragment);
            Toast.makeText(getContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void solicitarCambioPassword() {
        RetrofitClient.getApiService().solicitarCambioPassword(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(getContext(),
                        "Se le ha enviado un enlace para restablecer su contraseña a su correo electrónico.",
                        Toast.LENGTH_LONG).show();
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}