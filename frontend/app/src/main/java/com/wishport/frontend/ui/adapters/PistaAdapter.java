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

/**
 * Adapter para mostrar una lista de pistas en un RecyclerView
 * Muestra nombre, deporte, estado e imagen de cada pista
 * Permite hacer clic en una pista para ver su detalle
 */
public class PistaAdapter extends RecyclerView.Adapter<PistaAdapter.PistaViewHolder> {

    /**
     * Interfaz para manejar los clics en las pistas
     */
    public interface OnPistaClickListener {
        void onPistaClick(Pista pista);
    }

    /**
     * Lista de pistas a mostrar en el RecyclerView
     */
    private List<Pista> pistas = new ArrayList<>();
    
    /**
     * Listener para manejar los clics en las pistas
     */
    private OnPistaClickListener listener;

    /**
     * Constructor del adapter
     * 
     * @param pistas Lista de pistas a mostrar
     */
    public PistaAdapter(List<Pista> pistas) {
        if (pistas != null) {
            this.pistas = pistas;
        }
    }

    /**
     * Establece el listener para manejar los clics en las pistas
     * 
     * @param listener Implementación de OnPistaClickListener
     */
    public void setOnPistaClickListener(OnPistaClickListener listener) {
        this.listener = listener;
    }

    /**
     * Actualiza la lista de pistas y notifica al RecyclerView
     * Limpia la lista actual y añade las nuevas pistas
     * 
     * @param nuevasPistas Nueva lista de pistas a mostrar
     */
    public void actualizarLista(List<Pista> nuevasPistas) {
        this.pistas.clear();
        if (nuevasPistas != null) {
            this.pistas.addAll(nuevasPistas);
        }
        notifyDataSetChanged();
    }

    /**
     * Crea un nuevo ViewHolder cuando el RecyclerView necesita uno
     * Infla el layout item_pista para cada pista
     * 
     * @param parent ViewGroup padre donde se añadirá la vista
     * @param viewType Tipo de vista (no usado en este caso)
     * @return Nuevo PistaViewHolder con la vista inflada
     */
    @NonNull
    @Override
    public PistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pista, parent, false);
        return new PistaViewHolder(view);
    }

    /**
     * Vincula los datos de una pista a un ViewHolder específico
     * Muestra nombre, deporte, estado e imagen de la pista
     * Configura el listener de clic en el item
     * 
     * @param holder ViewHolder que recibirá los datos
     * @param position Posición de la pista en la lista
     */
    @Override
    public void onBindViewHolder(@NonNull PistaViewHolder holder, int position) {
        Pista pista = pistas.get(position);
        holder.tvNombre.setText(pista.getNombre() != null ? pista.getNombre() : "Sin nombre");
        holder.tvDeporte.setText(pista.getDeporte() != null ? pista.getDeporte() : "");
        holder.tvEstado.setText(pista.getEstado() != null ? pista.getEstado() : "");

        // Cargar la imagen de la pista usando Glide
        String fotoUrl = pista.getFotoUrl();
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            // Construir la URL completa de la imagen
            String fullUrl = ApiService.IMAGES_BASE_URL + (fotoUrl.startsWith("/") ? fotoUrl : "/" + fotoUrl);
            Glide.with(holder.itemView.getContext())
                    .load(fullUrl)
                    .placeholder(R.drawable.placeholder_pista)
                    .error(R.drawable.error_pista)
                    .into(holder.ivPista);
        } else {
            // Si no hay URL, mostrar imagen placeholder
            holder.ivPista.setImageResource(R.drawable.placeholder_pista);
        }

        // Configurar el listener de clic en el item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPistaClick(pista);
        });
    }

    /**
     * Retorna el número total de pistas en la lista
     * 
     * @return Número de pistas
     */
    @Override
    public int getItemCount() {
        return pistas.size();
    }

    /**
     * ViewHolder para mostrar una pista en el RecyclerView
     * Contiene las vistas para mostrar la imagen, nombre, deporte y estado de la pista
     */
    static class PistaViewHolder extends RecyclerView.ViewHolder {
        /**
         * ImageView para mostrar la imagen de la pista
         */
        ImageView ivPista;
        
        /**
         * TextView para mostrar el nombre de la pista
         */
        TextView tvNombre;
        
        /**
         * TextView para mostrar el tipo de deporte
         */
        TextView tvDeporte;
        
        /**
         * TextView para mostrar el estado de la pista
         */
        TextView tvEstado;

        /**
         * Constructor del ViewHolder
         * Encuentra las referencias a las vistas del layout
         * 
         * @param itemView Vista del item inflado
         */
        public PistaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPista = itemView.findViewById(R.id.ivPista);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDeporte = itemView.findViewById(R.id.tvDeporte);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}
