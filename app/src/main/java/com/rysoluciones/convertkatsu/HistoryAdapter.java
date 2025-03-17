// HistoryAdapter.java
package com.rysoluciones.convertkatsu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private List<ConversionModel> conversionList;
    private OnItemDeleteListener onItemDeleteListener;

    public interface OnItemDeleteListener {
        void onItemDelete(int id);
    }

    public HistoryAdapter(Context context, List<ConversionModel> conversionList, OnItemDeleteListener listener) {
        this.context = context;
        this.conversionList = conversionList;
        this.onItemDeleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversion_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConversionModel conversion = conversionList.get(position);

        holder.tvAmountFrom.setText(String.valueOf(conversion.getAmount()));
        holder.tvCurrencyFrom.setText(conversion.getFromCurrency());
        holder.tvAmountTo.setText(String.valueOf(conversion.getResult()));
        holder.tvCurrencyTo.setText(conversion.getToCurrency());
        holder.tvDateTime.setText(conversion.getTimestamp());

        // Evento para eliminar un ítem individual
        holder.btnDelete.setOnClickListener(v -> {
            if (onItemDeleteListener != null) {
                onItemDeleteListener.onItemDelete(conversion.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmountFrom, tvCurrencyFrom, tvAmountTo, tvCurrencyTo, tvDateTime;
        ImageButton btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvAmountFrom = itemView.findViewById(R.id.tvAmountFrom);
            tvCurrencyFrom = itemView.findViewById(R.id.tvCurrencyFrom);
            tvAmountTo = itemView.findViewById(R.id.tvAmountTo);
            tvCurrencyTo = itemView.findViewById(R.id.tvCurrencyTo);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
