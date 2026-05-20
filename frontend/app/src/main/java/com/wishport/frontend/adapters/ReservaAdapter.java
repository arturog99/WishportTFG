package com.wishport.frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.wishport.frontend.R;
import com.wishport.frontend.models.Reserva;
import java.util.List;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder> {
    private List<Reserva> reservas;

    public ReservaAdapter(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);
        holder.text1.setText(reserva.getFecha() != null ? reserva.getFecha().toString() : "Sin fecha");
        holder.text2.setText(reserva.getEstadoReserva() != null ? reserva.getEstadoReserva() : "Sin estado");
    }

    @Override
    public int getItemCount() {
        return reservas != null ? reservas.size() : 0;
    }

    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
