package com.example.rrhh_android_app;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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
    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    private IntentFilter[] nfcFilters;

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
                SharedPreferences pref = getSharedPreferences("RRHH_PREFS", MODE_PRIVATE);
                String rol = pref.getString("rol", "");
                if (rol.equalsIgnoreCase("Administrador") || rol.equalsIgnoreCase("Superadministrador")) {
                    bottomNav.getMenu().findItem(R.id.adminFragment).setVisible(true);
                } else {
                    bottomNav.getMenu().findItem(R.id.adminFragment).setVisible(false);
                }
                bottomNav.setVisibility(View.VISIBLE);
            }
        });

        // Configurar NFC foreground dispatch
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            Intent nfcIntent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            nfcPendingIntent = PendingIntent.getActivity(this, 0, nfcIntent,
                    PendingIntent.FLAG_MUTABLE);
            nfcFilters = new IntentFilter[]{
                    new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                    new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
                    new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
            };
        }

        // Arrancar notificaciones
        FichajeScheduler.programarWorker(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Activar foreground dispatch: esta Activity intercepta NFC antes que cualquier otra app
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, nfcFilters, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Desactivar foreground dispatch al salir
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // Verificar que hay sesión activa
            SharedPreferences pref = getSharedPreferences("RRHH_PREFS", MODE_PRIVATE);
            String token = pref.getString("token", "");
            if (token.isEmpty()) {
                Toast.makeText(this, "Inicia sesion antes de usar NFC", Toast.LENGTH_SHORT).show();
                return;
            }

            // Navegar a Home si no estamos ahí
            if (navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() != R.id.homeFragment) {
                navController.navigate(R.id.homeFragment);
            }

            // Delegar al HomeFragment
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