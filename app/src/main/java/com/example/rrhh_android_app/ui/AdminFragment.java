package com.example.rrhh_android_app.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rrhh_android_app.R;
import com.example.rrhh_android_app.ui.adapter.EmpleadosAdapter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminFragment extends Fragment {

    // Vistas sección empleados
    private RecyclerView rvEmpleados;
    private EmpleadosAdapter adapter;

    // Vistas sección registros
    private androidx.cardview.widget.CardView layoutRegistros;
    private TextView tvEmpleadoSeleccionado;
    private RecyclerView rvRegistros;
    private RegistrosAdapter registrosAdapter;
    private int idEmpleadoSeleccionado = -1;

    // Vistas sección empresa/mapa
    private MapView mapEmpresa;
    private TextView tvRadioActual;
    private com.google.android.material.textfield.TextInputEditText etNuevoRadio;
    private Button btnGuardarRadio;

    private String token;
    private double latEmpresa = 0, lonEmpresa = 0;
    private int radioActual = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Configuration.getInstance().load(getContext(),
                android.preference.PreferenceManager.getDefaultSharedPreferences(getContext()));

        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        SharedPreferences pref = getActivity().getSharedPreferences("RRHH_PREFS", Context.MODE_PRIVATE);
        token = "Bearer " + pref.getString("token", "");

        // Registros (findViewById antes del listener)
        layoutRegistros = view.findViewById(R.id.layoutRegistros);
        tvEmpleadoSeleccionado = view.findViewById(R.id.tvEmpleadoSeleccionado);

        // Empleados
        rvEmpleados = view.findViewById(R.id.rvEmpleados);
        adapter = new EmpleadosAdapter(new ArrayList<EmpleadosAdapter.EmpleadoSimple>());
        adapter.setOnEmpleadoClickListener((EmpleadosAdapter.EmpleadoSimple empleado) -> {
            idEmpleadoSeleccionado = empleado.getIdTrabajador();
            tvEmpleadoSeleccionado.setText(empleado.getNombre() + " " + empleado.getApellidos());
            layoutRegistros.setVisibility(View.VISIBLE);
            cargarRegistros(idEmpleadoSeleccionado);
        });
        rvEmpleados.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEmpleados.setAdapter(adapter);

        // Registros (RecyclerView)
        rvRegistros = view.findViewById(R.id.rvRegistros);
        registrosAdapter = new RegistrosAdapter(new ArrayList<>());
        rvRegistros.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRegistros.setAdapter(registrosAdapter);

        // Empresa / mapa
        mapEmpresa = view.findViewById(R.id.mapEmpresa);
        tvRadioActual = view.findViewById(R.id.tvRadioActual);
        etNuevoRadio = view.findViewById(R.id.etNuevoRadio);
        btnGuardarRadio = view.findViewById(R.id.btnGuardarRadio);

        mapEmpresa.setMultiTouchControls(true);
        mapEmpresa.getController().setZoom(16.0);

        btnGuardarRadio.setOnClickListener(v -> guardarRadio());

        cargarEmpleados();
        cargarDatosEmpresa();

        return view;
    }

    private void cargarEmpleados() {
        // Usar endpoint admin para filtrar por empresa
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://samuelillo777.eu.pythonanywhere.com/api/admin/trabajadores")
                        .addHeader("Authorization", token)
                        .build();
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                org.json.JSONArray arr = new org.json.JSONArray(body);
                List<EmpleadosAdapter.EmpleadoSimple> lista = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject obj = arr.getJSONObject(i);
                    lista.add(new EmpleadosAdapter.EmpleadoSimple(
                            obj.getInt("id_trabajador"),
                            obj.optString("nombre", ""),
                            obj.optString("apellidos", ""),
                            obj.optString("nif", "")
                    ));
                }
                if (getActivity() != null) {
                    final List<EmpleadosAdapter.EmpleadoSimple> finalLista = lista;
                    getActivity().runOnUiThread(() -> adapter.actualizar(finalLista));
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error cargando empleados", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    private void cargarRegistros(int idTrabajador) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://samuelillo777.eu.pythonanywhere.com/api/admin/registros?id_trabajador=" + idTrabajador)
                        .addHeader("Authorization", token)
                        .build();
                Response response = client.newCall(request).execute();
                String body = response.body().string();

                List<String[]> registros = new ArrayList<>();
                JSONArray arr = new JSONArray(body);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    String entrada = obj.optString("hora_entrada", "-");
                    String salida = obj.optString("hora_salida", "En curso");
                    registros.add(new String[]{entrada, salida});
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> registrosAdapter.actualizar(registros));
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error cargando registros", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    private void cargarDatosEmpresa() {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://samuelillo777.eu.pythonanywhere.com/api/admin/empresa")
                        .addHeader("Authorization", token)
                        .build();
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                JSONObject obj = new JSONObject(body);

                latEmpresa = obj.getDouble("lat");
                lonEmpresa = obj.getDouble("longi");
                radioActual = obj.getInt("radio");
                String nombre = obj.optString("nombre", "Empresa");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvRadioActual.setText("Radio actual: " + radioActual + "m");
                        etNuevoRadio.setHint(String.valueOf(radioActual));
                        actualizarMapa(latEmpresa, lonEmpresa, radioActual, nombre);
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error cargando empresa", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    private void actualizarMapa(double lat, double lon, int radio, String nombre) {
        GeoPoint punto = new GeoPoint(lat, lon);
        mapEmpresa.getController().setCenter(punto);

        // Marcador empresa
        Marker marker = new Marker(mapEmpresa);
        marker.setPosition(punto);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(nombre);
        mapEmpresa.getOverlays().clear();
        mapEmpresa.getOverlays().add(marker);

        // Círculo radio
        List<GeoPoint> circulo = Polygon.pointsAsCircle(punto, radio);
        Polygon polygon = new Polygon(mapEmpresa);
        polygon.setPoints(circulo);
        polygon.getFillPaint().setColor(0x220000FF);
        polygon.getOutlinePaint().setColor(0xFF1A73E8);
        polygon.getOutlinePaint().setStrokeWidth(3f);
        mapEmpresa.getOverlays().add(polygon);
        mapEmpresa.invalidate();
    }

    private void guardarRadio() {
        String nuevoRadioStr = etNuevoRadio.getText().toString().trim();
        if (nuevoRadioStr.isEmpty()) {
            Toast.makeText(getContext(), "Introduce un radio", Toast.LENGTH_SHORT).show();
            return;
        }
        int nuevoRadio;
        try {
            nuevoRadio = Integer.parseInt(nuevoRadioStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Radio invalido", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject json = new JSONObject();
                json.put("radio", nuevoRadio);
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"), json.toString());
                Request request = new Request.Builder()
                        .url("https://samuelillo777.eu.pythonanywhere.com/api/admin/empresa/radio")
                        .addHeader("Authorization", token)
                        .put(body)
                        .build();
                Response response = client.newCall(request).execute();

                if (getActivity() != null) {
                    boolean ok = response.isSuccessful();
                    getActivity().runOnUiThread(() -> {
                        if (ok) {
                            radioActual = nuevoRadio;
                            tvRadioActual.setText("Radio actual: " + radioActual + "m");
                            etNuevoRadio.setText("");
                            actualizarMapa(latEmpresa, lonEmpresa, radioActual, "Empresa");
                            Toast.makeText(getContext(), "Radio actualizado a " + radioActual + "m", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error al guardar radio", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Sin conexion", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapEmpresa != null) mapEmpresa.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapEmpresa != null) mapEmpresa.onPause();
    }

    // ---- Adapter inline para registros ----
    public static class RegistrosAdapter extends RecyclerView.Adapter<RegistrosAdapter.VH> {
        private List<String[]> lista;

        public RegistrosAdapter(List<String[]> lista) { this.lista = lista; }

        public void actualizar(List<String[]> nueva) {
            this.lista = nueva;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout layout = new LinearLayout(parent.getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(32, 20, 32, 20);
            layout.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT));
            TextView tvEntrada = new TextView(parent.getContext());
            TextView tvSalida = new TextView(parent.getContext());
            tvEntrada.setTag("entrada");
            tvSalida.setTag("salida");
            layout.addView(tvEntrada);
            layout.addView(tvSalida);
            // Divider
            View divider = new View(parent.getContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(0xFFE4E9F0);
            layout.addView(divider);
            return new VH(layout);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            String[] reg = lista.get(position);
            holder.tvEntrada.setText("Entrada: " + reg[0]);
            holder.tvSalida.setText("Salida: " + reg[1]);
            holder.tvSalida.setTextColor(reg[1].equals("En curso")
                    ? 0xFF00C853 : 0xFF5C6B7A);
        }

        @Override
        public int getItemCount() { return lista.size(); }

        public static class VH extends RecyclerView.ViewHolder {
            TextView tvEntrada, tvSalida;
            public VH(@NonNull View itemView) {
                super(itemView);
                tvEntrada = (TextView) ((LinearLayout) itemView).getChildAt(0);
                tvSalida = (TextView) ((LinearLayout) itemView).getChildAt(1);
            }
        }
    }
}