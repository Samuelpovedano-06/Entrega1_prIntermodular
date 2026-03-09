package com.example.rrhh_android_app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rrhh_android_app.R;
import com.example.rrhh_android_app.model.EmpleadoResponse;

import java.util.List;

public class EmpleadosAdapter extends RecyclerView.Adapter<EmpleadosAdapter.ViewHolder> {

    private List<EmpleadoResponse> lista;

    public EmpleadosAdapter(List<EmpleadoResponse> lista) {
        this.lista = lista;
    }

    public void actualizar(List<EmpleadoResponse> nuevaLista) {
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
        EmpleadoResponse emp = lista.get(position);
        holder.tvNombre.setText(emp.getNombre() + " " + emp.getApellidos());
        holder.tvNif.setText(emp.getNif());
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