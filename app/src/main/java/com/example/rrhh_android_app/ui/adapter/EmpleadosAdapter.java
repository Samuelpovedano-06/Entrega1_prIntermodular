package com.example.rrhh_android_app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rrhh_android_app.R;

import java.util.List;

public class EmpleadosAdapter extends RecyclerView.Adapter<EmpleadosAdapter.ViewHolder> {

    public static class EmpleadoSimple {
        public int idTrabajador;
        public String nombre;
        public String apellidos;
        public String nif;

        public EmpleadoSimple(int id, String nombre, String apellidos, String nif) {
            this.idTrabajador = id;
            this.nombre = nombre;
            this.apellidos = apellidos;
            this.nif = nif;
        }

        public int getIdTrabajador() { return idTrabajador; }
        public String getNombre() { return nombre; }
        public String getApellidos() { return apellidos; }
        public String getNif() { return nif; }
    }

    public interface OnEmpleadoClickListener {
        void onEmpleadoClick(EmpleadoSimple empleado);
    }

    private List<EmpleadoSimple> lista;
    private OnEmpleadoClickListener listener;

    public EmpleadosAdapter(List<EmpleadoSimple> lista) {
        this.lista = lista;
    }

    public void setOnEmpleadoClickListener(OnEmpleadoClickListener listener) {
        this.listener = listener;
    }

    public void actualizar(List<EmpleadoSimple> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_empleado, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmpleadoSimple emp = lista.get(position);
        holder.tvNombre.setText(emp.getNombre() + " " + emp.getApellidos());
        holder.tvNif.setText(emp.getNif());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEmpleadoClick(emp);
        });
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvNif;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreEmpleado);
            tvNif = itemView.findViewById(R.id.tvNifEmpleado);
        }
    }
}