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

/**
 * Adapter para mostrar una lista de reservas en un RecyclerView
 * Muestra información básica de cada reserva: deporte y fecha
 */
public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder> {
    /**
     * Lista de reservas a mostrar en el RecyclerView
     */
    private List<Reserva> reservas;

    /**
     * Constructor del adapter
     * 
     * @param reservas Lista de reservas a mostrar
     */
    public ReservaAdapter(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    /**
     * Crea un nuevo ViewHolder cuando el RecyclerView necesita uno
     * Infla el layout item_reserva para cada reserva
     * 
     * @param parent ViewGroup padre donde se añadirá la vista
     * @param viewType Tipo de vista (no usado en este caso)
     * @return Nuevo ReservaViewHolder con la vista inflada
     */
    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reserva, parent, false);
        return new ReservaViewHolder(view);
    }

    /**
     * Vincula los datos de una reserva a un ViewHolder específico
     * Muestra el deporte y la fecha de la reserva
     * 
     * @param holder ViewHolder que recibirá los datos
     * @param position Posición de la reserva en la lista
     */
    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);
        holder.tvDeporte.setText(reserva.getIdPista() != null ? reserva.getIdPista().getDeporte() : "Desconocido");
        holder.tvInfoReserva.setText(reserva.getFecha() != null ? reserva.getFecha().toString() : "Sin fecha");
    }

    /**
     * Retorna el número total de reservas en la lista
     * 
     * @return Número de reservas, o 0 si la lista es nula
     */
    @Override
    public int getItemCount() {
        return reservas != null ? reservas.size() : 0;
    }

    /**
     * ViewHolder para mostrar una reserva en el RecyclerView
     * Contiene las vistas para mostrar el deporte y la fecha de la reserva
     */
    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        /**
         * TextView para mostrar el deporte de la pista
         */
        TextView tvDeporte;
        
        /**
         * TextView para mostrar la información de la reserva (fecha)
         */
        TextView tvInfoReserva;

        /**
         * Constructor del ViewHolder
         * Encuentra las referencias a las vistas del layout
         * 
         * @param itemView Vista del item inflado
         */
        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeporte = itemView.findViewById(R.id.tvDeporte);
            tvInfoReserva = itemView.findViewById(R.id.tvInfoReserva);
        }
    }
}
