package com.example.rrhh_android_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.rrhh_android_app.notifications.FichajeScheduler;
import com.example.rrhh_android_app.ui.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Ocultar bottom nav en login
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.loginFragment) {
                bottomNav.setVisibility(View.GONE);
            } else {
                // Solo mostrar admin si el usuario es admin
                SharedPreferences pref = getSharedPreferences("RRHH_PREFS", MODE_PRIVATE);
                String rol = pref.getString("rol", "");
                if (rol.equalsIgnoreCase("admin")) {
                    bottomNav.getMenu().findItem(R.id.adminFragment).setVisible(true);
                } else {
                    bottomNav.getMenu().findItem(R.id.adminFragment).setVisible(false);
                }
                bottomNav.setVisibility(View.VISIBLE);
            }
        });

        // Arrancar notificaciones
        FichajeScheduler.programarWorker(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Manejar tag NFC
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {

            // Delegar al HomeFragment si está activo
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                androidx.fragment.app.Fragment current = navHostFragment.getChildFragmentManager()
                        .getPrimaryNavigationFragment();
                if (current instanceof HomeFragment) {
                    ((HomeFragment) current).procesarNfc();
                }
            }
        }
    }
}