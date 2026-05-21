package com.wishport.frontend.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.wishport.frontend.R;
import com.wishport.frontend.api.ApiService;
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

        String fotoUrl = pista.getFotoUrl();
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            String fullUrl = ApiService.IMAGES_BASE_URL + (fotoUrl.startsWith("/") ? fotoUrl : "/" + fotoUrl);
            Glide.with(holder.itemView.getContext())
                    .load(fullUrl)
                    .placeholder(R.drawable.placeholder_pista)
                    .error(R.drawable.error_pista)
                    .into(holder.ivPista);
        } else {
            holder.ivPista.setImageResource(R.drawable.placeholder_pista);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPistaClick(pista);
        });
    }

    @Override
    public int getItemCount() {
        return pistas.size();
    }

    static class PistaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPista;
        TextView tvNombre, tvDeporte, tvEstado;

        public PistaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPista = itemView.findViewById(R.id.ivPista);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDeporte = itemView.findViewById(R.id.tvDeporte);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}
