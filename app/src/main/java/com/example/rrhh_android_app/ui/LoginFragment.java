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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.rrhh_android_app.R;
import com.example.rrhh_android_app.api.RetrofitClient;
import com.example.rrhh_android_app.model.LoginRequest;
import com.example.rrhh_android_app.model.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
    private EditText etNif, etPass;
    private Button btnLogin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        etNif = view.findViewById(R.id.etNif);
        etPass = view.findViewById(R.id.etPass);
        btnLogin = view.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String nif = etNif.getText().toString().trim();
            String pass = etPass.getText().toString().trim();
            if(!nif.isEmpty() && !pass.isEmpty()) login(nif, pass);
        });
        return view;
    }

    private void login(String nif, String pass) {
        LoginRequest req = new LoginRequest(nif, pass);
        RetrofitClient.getApiService().login(req).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SharedPreferences pref = getActivity().getSharedPreferences("RRHH_PREFS", Context.MODE_PRIVATE);
                    pref.edit()
                            .putString("token", response.body().getToken())
                            .putString("nombre", response.body().getNombre())
                            .putString("rol", response.body().getRol())
                            .apply();
                    Navigation.findNavController(getView()).navigate(R.id.homeFragment);

                } else {
                    Toast.makeText(getContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}