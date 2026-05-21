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

/**
 * Adapter para mostrar una lista de reservas en un RecyclerView
 * Muestra deporte, fecha y hora de cada reserva
 * Permite hacer clic en una reserva para ver su detalle
 */
public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder> {

    /**
     * Interfaz para manejar los clics en las reservas
     */
    public interface OnReservaClickListener {
        void onReservaClick(Reserva reserva);
    }

    /**
     * Lista de reservas a mostrar en el RecyclerView
     */
    private List<Reserva> reservas = new ArrayList<>();
    
    /**
     * Listener para manejar los clics en las reservas
     */
    private OnReservaClickListener listener;

    /**
     * Constructor del adapter
     * 
     * @param reservas Lista de reservas a mostrar
     */
    public ReservaAdapter(List<Reserva> reservas) {
        if (reservas != null) this.reservas = reservas;
    }

    /**
     * Establece el listener para manejar los clics en las reservas
     * 
     * @param listener Implementación de OnReservaClickListener
     */
    public void setOnReservaClickListener(OnReservaClickListener listener) {
        this.listener = listener;
    }

    /**
     * Actualiza la lista de reservas y notifica al RecyclerView
     * Limpia la lista actual y añade las nuevas reservas
     * 
     * @param nuevasReservas Nueva lista de reservas a mostrar
     */
    public void actualizarLista(List<Reserva> nuevasReservas) {
        this.reservas.clear();
        if (nuevasReservas != null) this.reservas.addAll(nuevasReservas);
        notifyDataSetChanged();
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
     * Muestra deporte, fecha y hora de la reserva
     * Configura el listener de clic en el item
     * 
     * @param holder ViewHolder que recibirá los datos
     * @param position Posición de la reserva en la lista
     */
    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);
        
        // Obtener y formatear los datos de la reserva
        String deporte = reserva.getIdPista() != null ? reserva.getIdPista().getDeporte() : "Desconocido";
        String fecha = reserva.getFecha() != null ? reserva.getFecha().toLocalDate().toString() : "";
        String hora = reserva.getHoraInicio() != null ? reserva.getHoraInicio() : "";
        String info = fecha + " · " + hora;
        
        // Mostrar los datos en las vistas
        holder.tvDeporte.setText(deporte);
        holder.tvInfoReserva.setText(info);

        // Configurar el listener de clic en el item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onReservaClick(reserva);
        });
    }

    /**
     * Retorna el número total de reservas en la lista
     * 
     * @return Número de reservas
     */
    @Override
    public int getItemCount() {
        return reservas.size();
    }

    /**
     * ViewHolder para mostrar una reserva en el RecyclerView
     * Contiene las vistas para mostrar el deporte, fecha y hora de la reserva
     */
    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        /**
         * TextView para mostrar el deporte de la pista
         */
        TextView tvDeporte;
        
        /**
         * TextView para mostrar la información de la reserva (fecha y hora)
         */
        TextView tvInfoReserva;
        
        /**
         * ImageView para mostrar un icono de la reserva
         */
        ImageView ivIcono;

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
