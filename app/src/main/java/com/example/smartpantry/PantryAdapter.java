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

/**
 * Custom adapter that binds the pantry rows from the database to item_pantry.xml.
 *
 * HOW A RECYCLERVIEW ADAPTER WORKS
 *  - getItemCount()       tells the RecyclerView how many rows exist.
 *  - onCreateViewHolder() inflates the row layout (done only for the few rows that
 *                         fit on screen; scrolled-off rows are recycled).
 *  - onBindViewHolder()   fills a recycled row with the data of one PantryItem.
 *  - ViewHolder (VH)      keeps references to the row's views so findViewById is
 *                         not repeated on every scroll.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.VH> {

    /** The adapter reports taps back to the Activity through this interface. */
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

    /** Replaces the displayed data and redraws the list. */
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
            h.tvExpiry.setVisibility(View.GONE); // no expiry date set
        } else {
            h.tvExpiry.setVisibility(View.VISIBLE);
            h.tvExpiry.setTextColor(Color.parseColor("#616161"));
            h.tvExpiry.setText("Expires: " + expiry);

            // Expiry highlighting is controlled by the Settings toggle.
            if (showExpiryAlerts) {
                int days = daysUntil(expiry);
                if (days < 0) {
                    h.tvExpiry.setText("EXPIRED: " + expiry);
                    h.tvExpiry.setTextColor(Color.parseColor("#C62828")); // red
                } else if (days <= 3) {
                    h.tvExpiry.setText("Expires soon: " + expiry);
                    h.tvExpiry.setTextColor(Color.parseColor("#EF6C00")); // orange
                }
            }
        }

        // Tap the card to edit, tap the bin to delete; the Activity decides what to do.
        h.itemView.setOnClickListener(v -> listener.onEdit(item));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Whole days from today until the given yyyy-MM-dd date (negative = already past). */
    private int daysUntil(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date d = sdf.parse(date);
            // Compare against midnight today so the time of day does not change the result.
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            long diff = d.getTime() - today.getTimeInMillis();
            return (int) Math.round(diff / 86400000.0); // milliseconds in one day
        } catch (ParseException e) {
            return Integer.MAX_VALUE; // unreadable date: treat as "far in the future"
        }
    }

    /** Holds the views of one row so they can be reused. */
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
