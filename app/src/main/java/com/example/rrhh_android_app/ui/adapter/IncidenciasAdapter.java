package com.example.rrhh_android_app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rrhh_android_app.R;
import com.example.rrhh_android_app.model.IncidenciaResponse;

import java.util.List;

public class IncidenciasAdapter extends RecyclerView.Adapter<IncidenciasAdapter.ViewHolder> {

    private List<IncidenciaResponse> lista;

    public IncidenciasAdapter(List<IncidenciaResponse> lista) {
        this.lista = lista;
    }

    public void actualizar(List<IncidenciaResponse> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_incidencia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IncidenciaResponse inc = lista.get(position);
        holder.tvFecha.setText(inc.getFechaHora());
        holder.tvDescripcion.setText(inc.getDescripcion());
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvDescripcion;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
        }
    }
}