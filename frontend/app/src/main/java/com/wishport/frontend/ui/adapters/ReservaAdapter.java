package com.wishport.frontend.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wishport.frontend.R;
import com.wishport.frontend.models.Reserva;

import java.util.ArrayList;
import java.util.List;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder> {

    private List<Reserva> reservas = new ArrayList<>();

    public ReservaAdapter(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    public void actualizarLista(List<Reserva> nuevasReservas) {
        this.reservas.clear();
        this.reservas.addAll(nuevasReservas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reserva, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);
        
        String deporte = reserva.getPista() != null ? reserva.getPista().getDeporte() : "Desconocido";
        String info = reserva.getFecha() + " - " + reserva.getHora();
        
        holder.tvDeporte.setText(deporte);
        holder.tvInfoReserva.setText(info);
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeporte, tvInfoReserva;
        ImageView ivIcono;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeporte = itemView.findViewById(R.id.tvDeporte);
            tvInfoReserva = itemView.findViewById(R.id.tvInfoReserva);
        }
    }
}
