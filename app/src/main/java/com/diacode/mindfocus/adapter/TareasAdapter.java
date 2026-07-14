package com.diacode.mindfocus.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diacode.mindfocus.R;
import com.diacode.mindfocus.data.EstadoTarea;
import com.diacode.mindfocus.data.Prioridad;
import com.diacode.mindfocus.data.Tarea;
import com.diacode.mindfocus.data.TipoTarea;

import java.util.ArrayList;
import java.util.List;

public class TareasAdapter extends RecyclerView.Adapter<TareasAdapter.ViewHolder>{
    private List<Tarea> lista = new ArrayList<>();
    private OnTareaClickListener listener;
    private boolean soloLectura = false;
    public TareasAdapter(){}
    public TareasAdapter(OnTareaClickListener listener){
        this.listener = listener;
    }
    public void setLista(List<Tarea> lista){
        this.lista = lista;
        notifyDataSetChanged();
    }
    public void setSoloLectura(boolean soloLectura) {
        this.soloLectura = soloLectura;
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
        //determinar estado de la tarea
        //si estado es null que sean completadas
        EstadoTarea estado = tarea.getEstado();
        if (estado == null) {
            estado = tarea.isCompletada() ? EstadoTarea.COMPLETADA : EstadoTarea.PENDIENTE;
        }
        String estadoTexto;
        int estadoColor;
        switch (estado) {
            case COMPLETADA:
                estadoTexto = "✅ Completada";
                estadoColor = Color.parseColor("#2E7D32"); // verde
                break;
            case INCOMPLETA:
                estadoTexto = "❌ Incompleta";
                estadoColor = Color.parseColor("#C62828"); // rojo
                break;
            case PENDIENTE:
            default:
                estadoTexto = "⏳ Pendiente";
                estadoColor = Color.parseColor("#F9A825"); // amarillo
                break;
        }
        holder.tvInfo.setText(
                obtenerTipo(tarea.getTipo())
                        + " • "
                        + obtenerPrioridad(tarea.getPrioridad())
        );
        holder.tvEstado.setText(estadoTexto);
        holder.tvEstado.setTextColor(estadoColor);
        // --- Checkbox ---
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(estado == EstadoTarea.COMPLETADA);
        holder.cbCompleted.setEnabled(false);

        // No se puede tocar si: ya está completada, O el día es de solo lectura (pasado)
//        boolean puedeEditar = !soloLectura && estado != EstadoTarea.COMPLETADA;
//        holder.cbCompleted.setEnabled(puedeEditar);
//
//        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked) {
//                listener.onCompletarTarea(tarea);
//            }
//        });

        // Si es de solo lectura, opcionalmente bajamos la opacidad para que se note visualmente
        holder.itemView.setAlpha(soloLectura ? 0.7f : 1f);

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
        TextView tvEstado;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            tvEstado = itemView.findViewById(R.id.tvEstado);
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
        void onCompletarTarea(Tarea tarea);
    }
}
