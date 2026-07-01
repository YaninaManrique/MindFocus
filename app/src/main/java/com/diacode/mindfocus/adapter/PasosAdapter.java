package com.diacode.mindfocus.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diacode.mindfocus.R;
import com.diacode.mindfocus.data.Paso;

import java.util.ArrayList;
import java.util.List;
// adaptador que muestra la lista de pasos en el RecyclerView
public class PasosAdapter extends RecyclerView.Adapter<PasosAdapter.ViewHolder>{
    private List<Paso> lista = new ArrayList<>();
    // notifica cuando un paso es marcado como completado
    public interface OnPasoChanged{
        void onChecked(Paso paso, boolean checked);
    }
    private final OnPasoChanged listener;
    public PasosAdapter(OnPasoChanged listener){
        this.listener = listener;
    }
    // actualiza la lista de pasos y refresca la vista
    public void setLista(List<Paso> lista){
        this.lista = lista;
        notifyDataSetChanged();
    }
    // crea la vista de cada elemento de la lista
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paso,parent,false);
        return new ViewHolder(view);
    }
    // asigna los datos del paso y configura el CheckBox
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Paso paso = lista.get(position);
        holder.tvPaso.setText(paso.getDescripcion());
        holder.cbPaso.setOnCheckedChangeListener(null);
        holder.cbPaso.setChecked(paso.isCompletado());
        // si ya está completado, no puede volver a modificarse
        holder.cbPaso.setEnabled(!paso.isCompletado());
        // notifica cuando el usuario completa el paso
        holder.cbPaso.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                listener.onChecked(paso, true);
                // inmediatamente queda bloqueado
                holder.cbPaso.setEnabled(false);
            }
        });
    }
    // devuelve la cantidad de pasos
    @Override
    public int getItemCount() {
        return lista.size();
    }
    // mantiene las referencias a los controles de cada elemento
    static class ViewHolder extends RecyclerView.ViewHolder{
        CheckBox cbPaso;
        TextView tvPaso;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbPaso=itemView.findViewById(R.id.cbPaso);
            tvPaso=itemView.findViewById(R.id.tvPaso);
        }
    }
}
