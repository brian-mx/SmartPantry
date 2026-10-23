package com.example.smartpantry;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.VH> {

    public interface Listener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;
    private boolean showExpiryAlerts = true;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setData(List<PantryItem> newItems, boolean showExpiryAlerts) {
        items.clear();
        items.addAll(newItems);
        this.showExpiryAlerts = showExpiryAlerts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        final PantryItem item = items.get(position);

        h.tvName.setText(item.getName());
        h.tvQty.setText(RecipeMatcher.formatQty(item.getQuantity()) + " " + item.getUnit());

        String expiry = item.getExpiry();
        if (expiry == null || expiry.isEmpty()) {
            h.tvExpiry.setVisibility(View.GONE);
        } else {
            h.tvExpiry.setVisibility(View.VISIBLE);
            h.tvExpiry.setTextColor(Color.parseColor("#616161"));
            h.tvExpiry.setText("Expires: " + expiry);

            if (showExpiryAlerts) {
                int days = daysUntil(expiry);
                if (days < 0) {
                    h.tvExpiry.setText("EXPIRED: " + expiry);
                    h.tvExpiry.setTextColor(Color.parseColor("#C62828"));
                } else if (days <= 3) {
                    h.tvExpiry.setText("Expires soon: " + expiry);
                    h.tvExpiry.setTextColor(Color.parseColor("#EF6C00"));
                }
            }
        }

        h.itemView.setOnClickListener(v -> listener.onEdit(item));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int daysUntil(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date d = sdf.parse(date);
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            long diff = d.getTime() - today.getTimeInMillis();
            return (int) Math.round(diff / 86400000.0);
        } catch (ParseException e) {
            return Integer.MAX_VALUE;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName, tvQty, tvExpiry;
        final ImageButton btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvQty = itemView.findViewById(R.id.tvItemQty);
            tvExpiry = itemView.findViewById(R.id.tvItemExpiry);
            btnDelete = itemView.findViewById(R.id.btnDeleteItem);
        }
    }
}
