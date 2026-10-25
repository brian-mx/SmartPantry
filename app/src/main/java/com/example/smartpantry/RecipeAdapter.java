package com.example.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom adapter for the recipe lists (both the strict list and the optional
 * "Almost There" list). When showMissing is true it also prints what is missing.
 *
 * One adapter class serves both lists: the constructor flag decides which
 * second line of text each card shows.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.VH> {

    /** The adapter reports taps back to the Activity through this interface. */
    public interface Listener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<RecipeMatcher.MatchResult> data = new ArrayList<>();
    private final Listener listener;
    private final boolean showMissing;

    public RecipeAdapter(boolean showMissing, Listener listener) {
        this.showMissing = showMissing;
        this.listener = listener;
    }

    /** Replaces the displayed results and redraws the list. */
    public void setData(List<RecipeMatcher.MatchResult> results) {
        data.clear();
        data.addAll(results);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        final RecipeMatcher.MatchResult m = data.get(position);
        h.tvName.setText(m.recipe.getName());

        if (showMissing) {
            // "Almost There" list: exactly one ingredient is missing, so show it.
            h.tvInfo.setText("Missing: " + m.missing.get(0));
        } else {
            // Strict list: the user has everything.
            h.tvInfo.setText(m.recipe.getIngredients().size() + " ingredients - you have them all");
        }

        h.itemView.setOnClickListener(v -> listener.onRecipeClick(m.recipe));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /** Holds the views of one row so they can be reused. */
    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName, tvInfo;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            tvInfo = itemView.findViewById(R.id.tvRecipeInfo);
        }
    }
}
