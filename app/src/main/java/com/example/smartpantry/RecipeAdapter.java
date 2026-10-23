package com.example.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.VH> {

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
            h.tvInfo.setText("Missing: " + m.missing.get(0));
        } else {
            h.tvInfo.setText(m.recipe.getIngredients().size() + " ingredients - you have them all");
        }

        h.itemView.setOnClickListener(v -> listener.onRecipeClick(m.recipe));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName, tvInfo;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            tvInfo = itemView.findViewById(R.id.tvRecipeInfo);
        }
    }
}
