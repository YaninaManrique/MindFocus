package com.diacode.mindfocus.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diacode.mindfocus.R;
import com.diacode.mindfocus.data.Prioridad;
import com.diacode.mindfocus.data.Tarea;
import com.diacode.mindfocus.data.TipoTarea;

import java.util.ArrayList;
import java.util.List;

public class TareasAdapter extends RecyclerView.Adapter<TareasAdapter.ViewHolder>{
    private List<Tarea> lista = new ArrayList<>();
    private OnTareaClickListener listener;
    public TareasAdapter(){}
    public TareasAdapter(OnTareaClickListener listener){
        this.listener = listener;
    }
    public void setLista(List<Tarea> lista){
        this.lista = lista;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tareas,parent,false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tarea tarea = lista.get(position);
        holder.tvNombre.setText(tarea.getNombre());
        String estado = tarea.isCompletada()
                ? "✅ Completada"
                : "⏳ Pendiente";
        holder.tvInfo.setText(
                obtenerTipo(tarea.getTipo())
                        + " • "
                        + obtenerPrioridad(tarea.getPrioridad())
                        + " • "
                        + estado
        );
        holder.cbCompleted.setChecked(tarea.isCompletada());
        holder.itemView.setOnClickListener(v -> {
            listener.onClick(tarea);
        });
    }
    @Override
    public int getItemCount() {
        return lista.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder{
        CheckBox cbCompleted;
        TextView tvNombre;
        TextView tvInfo;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvInfo = itemView.findViewById(R.id.tvInfo);
        }
    }
    private String obtenerTipo(TipoTarea tipo){
        switch (tipo){
            case ESTUDIO:
                return "📚 Estudio";
            case EJERCICIO:
                return "🏃 Ejercicio";
            case HOGAR:
                return "🏠 Hogar";
            case TRABAJO:
                return "💼 Trabajo";
            case CREATIVO:
                return "🎨 Creativo";
        }
        return "";
    }
    private String obtenerPrioridad(Prioridad prioridad){
        switch (prioridad){
            case BAJA:
                return "Baja";
            case MEDIA:
                return "Media";
            case ALTA:
                return "Alta";
        }
        return "";
    }
    public interface OnTareaClickListener{
        void onClick(Tarea tarea);
    }
}
