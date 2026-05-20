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

    public interface OnReservaClickListener {
        void onReservaClick(Reserva reserva);
    }

    private List<Reserva> reservas = new ArrayList<>();
    private OnReservaClickListener listener;

    public ReservaAdapter(List<Reserva> reservas) {
        if (reservas != null) this.reservas = reservas;
    }

    public void setOnReservaClickListener(OnReservaClickListener listener) {
        this.listener = listener;
    }

    public void actualizarLista(List<Reserva> nuevasReservas) {
        this.reservas.clear();
        if (nuevasReservas != null) this.reservas.addAll(nuevasReservas);
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
        
        String deporte = reserva.getIdPista() != null ? reserva.getIdPista().getDeporte() : "Desconocido";
        String fecha = reserva.getFecha() != null ? reserva.getFecha().toLocalDate().toString() : "";
        String hora = reserva.getHoraInicio() != null ? reserva.getHoraInicio() : "";
        String info = fecha + " · " + hora;
        
        holder.tvDeporte.setText(deporte);
        holder.tvInfoReserva.setText(info);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onReservaClick(reserva);
        });
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
