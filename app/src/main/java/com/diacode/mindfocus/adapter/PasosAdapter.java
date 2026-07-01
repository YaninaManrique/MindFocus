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

public class PasosAdapter extends RecyclerView.Adapter<PasosAdapter.ViewHolder>{
    private List<Paso> lista = new ArrayList<>();
    public interface OnPasoChanged{
        void onChecked(Paso paso, boolean checked);
    }
    private final OnPasoChanged listener;
    public PasosAdapter(OnPasoChanged listener){
        this.listener = listener;
    }
    public void setLista(List<Paso> lista){
        this.lista = lista;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paso,parent,false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Paso paso = lista.get(position);
        holder.tvPaso.setText(paso.getDescripcion());
        holder.cbPaso.setOnCheckedChangeListener(null);
        holder.cbPaso.setChecked(paso.isCompletado());
        holder.cbPaso.setOnCheckedChangeListener((buttonView,isChecked)->{
            listener.onChecked(paso,isChecked);
        });
    }
    @Override
    public int getItemCount() {
        return lista.size();
    }
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
