package com.example.smartpantry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Local unit tests for the strict-matching rule in RecipeMatcher.
 * They run on the normal JVM (no phone or emulator needed):
 * right-click this file in Android Studio and choose "Run RecipeMatcherTest".
 */
public class RecipeMatcherTest {

    // ---- helpers to keep each test short ----

    /** Builds a recipe from "name:quantity:unit" strings, like the database seed data. */
    private Recipe recipe(String name, String... ingredients) {
        Recipe r = new Recipe(1, name, "");
        for (String ing : ingredients) {
            String[] p = ing.split(":");
            r.getIngredients().add(new RecipeIngredient(p[0], Double.parseDouble(p[1]), p[2]));
        }
        return r;
    }

    private PantryItem item(String name, double qty, String unit) {
        return new PantryItem(0, name, qty, unit, "");
    }

    private int strictCount(Recipe r, PantryItem... pantry) {
        List<PantryItem> list = new ArrayList<>(Arrays.asList(pantry));
        return RecipeMatcher.strictMatches(
                RecipeMatcher.evaluateAll(Arrays.asList(r), list)).size();
    }

    // ---- the strict rule itself ----

    @Test
    public void allIngredientsPresent_recipeIsSuggested() {
        Recipe r = recipe("Scrambled Eggs", "egg:3:pcs", "bread:2:pcs", "butter:10:g");
        assertEquals(1, strictCount(r,
                item("egg", 3, "pcs"), item("bread", 2, "pcs"), item("butter", 10, "g")));
    }

    @Test
    public void oneIngredientMissing_recipeIsNotSuggested() {
        Recipe r = recipe("Scrambled Eggs", "egg:3:pcs", "bread:2:pcs", "butter:10:g");
        // 2 of the 3 ingredients: must NOT appear in the strict list.
        assertEquals(0, strictCount(r, item("egg", 3, "pcs"), item("bread", 2, "pcs")));
    }

    @Test
    public void oneIngredientMissing_recipeAppearsInAlmostThere() {
        Recipe r = recipe("Scrambled Eggs", "egg:3:pcs", "bread:2:pcs", "butter:10:g");
        List<RecipeMatcher.MatchResult> all = RecipeMatcher.evaluateAll(
                Arrays.asList(r), Arrays.asList(item("egg", 3, "pcs"), item("bread", 2, "pcs")));
        assertEquals(1, RecipeMatcher.almostThere(all).size());
        assertEquals("butter", RecipeMatcher.almostThere(all).get(0).missing.get(0));
    }

    @Test
    public void twoIngredientsMissing_isNeitherStrictNorAlmostThere() {
        Recipe r = recipe("Scrambled Eggs", "egg:3:pcs", "bread:2:pcs", "butter:10:g");
        List<RecipeMatcher.MatchResult> all = RecipeMatcher.evaluateAll(
                Arrays.asList(r), Arrays.asList(item("egg", 3, "pcs")));
        assertEquals(0, RecipeMatcher.strictMatches(all).size());
        assertEquals(0, RecipeMatcher.almostThere(all).size());
    }

    @Test
    public void notEnoughQuantity_recipeIsNotSuggested() {
        Recipe r = recipe("Tomato Pasta", "tomato:4:pcs", "pasta:200:g");
        // Only 3 tomatoes but 4 are needed.
        assertEquals(0, strictCount(r, item("tomato", 3, "pcs"), item("pasta", 200, "g")));
    }

    @Test
    public void emptyPantry_noRecipeIsSuggested() {
        Recipe r = recipe("Scrambled Eggs", "egg:3:pcs");
        assertEquals(0, strictCount(r));
    }

    // ---- robustness: the "real-world messiness" the brief asks for ----

    @Test
    public void pluralAndUpperCaseNames_stillMatch() {
        Recipe r = recipe("Tomato Pasta", "tomato:4:pcs", "pasta:200:g");
        assertEquals(1, strictCount(r, item("Tomatoes", 4, "pcs"), item("PASTA", 200, "g")));
    }

    @Test
    public void unitConversion_kilogramsSatisfyGrams() {
        Recipe r = recipe("Pasta", "pasta:200:g");
        assertEquals(1, strictCount(r, item("pasta", 0.25, "kg")));
    }

    @Test
    public void unitConversion_litresSatisfyMillilitres() {
        Recipe r = recipe("Pancakes", "milk:300:ml");
        assertEquals(1, strictCount(r, item("milk", 1, "l")));
    }

    @Test
    public void duplicatePantryRows_areAddedTogether() {
        Recipe r = recipe("Pancakes", "flour:200:g");
        // Two separate rows of 100 g add up to the 200 g needed.
        assertEquals(1, strictCount(r, item("flour", 100, "g"), item("flour", 100, "g")));
    }

    @Test
    public void differentUnitFamilies_doNotMatch() {
        Recipe r = recipe("Omelette", "egg:3:pcs");
        // Eggs entered in grams cannot satisfy a recipe that counts eggs in pieces.
        assertEquals(0, strictCount(r, item("egg", 3, "g")));
    }

    // ---- small helper methods ----

    @Test
    public void normalizeName_handlesCaseSpacesAndPlurals() {
        assertEquals("tomato", RecipeMatcher.normalizeName("Tomatoes"));
        assertEquals("egg", RecipeMatcher.normalizeName("  EGGS "));
        assertEquals("olive oil", RecipeMatcher.normalizeName("Olive   Oil"));
        assertEquals("", RecipeMatcher.normalizeName(null));
    }

    @Test
    public void formatQty_dropsTrailingZero() {
        assertEquals("2", RecipeMatcher.formatQty(2.0));
        assertEquals("0.5", RecipeMatcher.formatQty(0.5));
        assertTrue(RecipeMatcher.formatQty(12).equals("12"));
    }
}
