package com.wishport.frontend.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wishport.frontend.R;
import com.wishport.frontend.models.Pista;

import java.util.ArrayList;
import java.util.List;

public class PistaAdapter extends RecyclerView.Adapter<PistaAdapter.PistaViewHolder> {

    public interface OnPistaClickListener {
        void onPistaClick(Pista pista);
    }

    private List<Pista> pistas = new ArrayList<>();
    private OnPistaClickListener listener;

    public PistaAdapter(List<Pista> pistas) {
        if (pistas != null) {
            this.pistas = pistas;
        }
    }

    public void setOnPistaClickListener(OnPistaClickListener listener) {
        this.listener = listener;
    }

    public void actualizarLista(List<Pista> nuevasPistas) {
        this.pistas.clear();
        if (nuevasPistas != null) {
            this.pistas.addAll(nuevasPistas);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pista, parent, false);
        return new PistaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PistaViewHolder holder, int position) {
        Pista pista = pistas.get(position);
        holder.tvNombre.setText(pista.getNombre() != null ? pista.getNombre() : "Sin nombre");
        holder.tvDeporte.setText(pista.getDeporte() != null ? pista.getDeporte() : "");
        holder.tvEstado.setText(pista.getEstado() != null ? pista.getEstado() : "");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPistaClick(pista);
        });
    }

    @Override
    public int getItemCount() {
        return pistas.size();
    }

    static class PistaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDeporte, tvEstado;

        public PistaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDeporte = itemView.findViewById(R.id.tvDeporte);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}
