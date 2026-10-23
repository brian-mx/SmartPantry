package com.example.smartpantry;

/**
 * RecipeIngredient is the MODEL class for one row of the "recipe_ingredients"
 * table: a single ingredient line that a recipe needs.
 *
 * Example: "tomato", 4, "pcs" means the recipe needs 4 tomatoes.
 *
 * The fields are final because a recipe's requirements never change while the
 * app is running; they are only read from the database.
 */
public class RecipeIngredient {

    private final String name;      // ingredient name, e.g. "olive oil"
    private final double quantity;  // amount the recipe needs
    private final String unit;      // unit of that amount, e.g. "tbsp"

    /** Creates one required ingredient for a recipe. */
    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
}
