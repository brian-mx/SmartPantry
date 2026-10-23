package com.example.smartpantry;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        long id = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = new DatabaseHelper(this).getRecipe(id);

        TextView tvTitle = findViewById(R.id.tvRecipeTitle);
        TextView tvIngredients = findViewById(R.id.tvIngredients);
        TextView tvSteps = findViewById(R.id.tvSteps);

        if (recipe == null) {
            tvTitle.setText("Recipe not found");
            return;
        }

        tvTitle.setText(recipe.getName());

        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            sb.append("• ")
              .append(RecipeMatcher.formatQty(ing.getQuantity()))
              .append(" ")
              .append(ing.getUnit())
              .append(" ")
              .append(ing.getName())
              .append("\n");
        }
        tvIngredients.setText(sb.toString().trim());
        tvSteps.setText(recipe.getSteps());
    }
}
