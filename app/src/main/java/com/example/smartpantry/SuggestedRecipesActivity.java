package com.example.smartpantry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.Listener {

    private DatabaseHelper db;
    private RecipeAdapter strictAdapter;
    private RecipeAdapter almostAdapter;
    private TextView tvEmpty, tvAlmostHeader;
    private RecyclerView rvAlmost, rvStrict;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        db = new DatabaseHelper(this);

        tvEmpty = findViewById(R.id.tvNoMatches);
        tvAlmostHeader = findViewById(R.id.tvAlmostHeader);
        rvStrict = findViewById(R.id.rvStrict);
        rvAlmost = findViewById(R.id.rvAlmost);

        rvStrict.setLayoutManager(new LinearLayoutManager(this));
        rvAlmost.setLayoutManager(new LinearLayoutManager(this));

        strictAdapter = new RecipeAdapter(false, this);
        almostAdapter = new RecipeAdapter(true, this);
        rvStrict.setAdapter(strictAdapter);
        rvAlmost.setAdapter(almostAdapter);

        NavHelper.setup(this, R.id.nav_recipes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<PantryItem> pantry = db.getAllItems();
        List<Recipe> recipes = db.getAllRecipes();

        // Core logic - see RecipeMatcher
        List<RecipeMatcher.MatchResult> all = RecipeMatcher.evaluateAll(recipes, pantry);
        List<RecipeMatcher.MatchResult> strict = RecipeMatcher.strictMatches(all);

        strictAdapter.setData(strict);
        tvEmpty.setVisibility(strict.isEmpty() ? View.VISIBLE : View.GONE);
        rvStrict.setVisibility(strict.isEmpty() ? View.GONE : View.VISIBLE);

        // Optional stretch: clearly separated "Almost There" list
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS, Context.MODE_PRIVATE);
        boolean showAlmost = prefs.getBoolean(SettingsActivity.KEY_ALMOST, false);
        List<RecipeMatcher.MatchResult> almost = RecipeMatcher.almostThere(all);

        if (showAlmost && !almost.isEmpty()) {
            almostAdapter.setData(almost);
            tvAlmostHeader.setVisibility(View.VISIBLE);
            rvAlmost.setVisibility(View.VISIBLE);
        } else {
            tvAlmostHeader.setVisibility(View.GONE);
            rvAlmost.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
