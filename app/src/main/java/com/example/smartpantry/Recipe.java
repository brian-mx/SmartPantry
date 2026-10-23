package com.example.smartpantry;

import java.util.ArrayList;
import java.util.List;

/**
 * Recipe is the MODEL class for one row of the "recipes" table, together with
 * the ingredients that belong to it.
 *
 * The database stores recipes and their ingredients in two tables
 * (recipes and recipe_ingredients). DatabaseHelper joins them back together
 * into a single Recipe object so the rest of the app can work with one object.
 */
public class Recipe {

    private final long id;        // primary key in the recipes table
    private final String name;    // e.g. "Tomato Pasta"
    private final String steps;   // the preparation method as plain text

    // Every ingredient this recipe needs. The strict-matching rule checks ALL of them.
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    /** Creates a recipe with no ingredients yet; DatabaseHelper fills the list. */
    public Recipe(long id, String name, String steps) {
        this.id = id;
        this.name = name;
        this.steps = steps;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getSteps() { return steps; }

    /** Returns the live list so DatabaseHelper can add ingredients to it. */
    public List<RecipeIngredient> getIngredients() { return ingredients; }
}
