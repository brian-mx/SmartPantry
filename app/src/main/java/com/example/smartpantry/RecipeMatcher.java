package com.example.smartpantry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * THE CORE LOGIC OF THE APP: the strict-matching rule.
 *
 * A recipe is "suggested" only if EVERY ingredient it needs is in the pantry
 * in at least the required quantity. If a recipe needs 5 ingredients and the
 * pantry has 4, the recipe is NOT suggested.
 *
 * To be robust against everyday messiness the matcher also:
 *  - normalises names (case, extra spaces, simple plurals: "Tomatoes" == "tomato")
 *  - converts units to a base unit (kg -> g, l -> ml, cup/tbsp/tsp -> ml)
 *  - adds up several pantry rows of the same ingredient (2 rows of 100 g = 200 g)
 *
 * This class has no Android code, so it can be unit tested on a normal JVM.
 *
 * WORKED EXAMPLE
 *   Recipe "Tomato Pasta" needs: tomato 4 pcs, pasta 200 g, olive oil 2 tbsp
 *   Pantry has:                  "Tomatoes" 4 pcs, "pasta" 0.25 kg
 *   Step 1  totals: tomato|count = 4,  pasta|mass = 250
 *   Step 2  tomato 4 >= 4 OK, pasta 250 >= 200 OK, olive oil not found -> missing
 *   Result  missing = [olive oil]  -> NOT strict, but it IS an "Almost There" recipe
 */
public class RecipeMatcher {

    /** The units offered in the Add/Edit spinner. */
    public static final String[] UNITS = {"g", "kg", "ml", "l", "cup", "tbsp", "tsp", "pcs"};

    /** Result of checking one recipe against the pantry. */
    public static class MatchResult {
        public final Recipe recipe;
        public final List<String> missing; // names of missing / insufficient ingredients

        public MatchResult(Recipe recipe, List<String> missing) {
            this.recipe = recipe;
            this.missing = missing;
        }
    }

    // ------------------------------------------------------------------
    // Normalisation helpers
    // ------------------------------------------------------------------

    /**
     * Lower-cases, trims and reduces a simple plural to its singular form so that
     * "Tomatoes", "tomato " and "TOMATO" all become "tomato".
     * This is deliberately simple (no full NLP), which the brief allows.
     */
    public static String normalizeName(String raw) {
        if (raw == null) return "";
        // Lower-case, trim the ends and collapse repeated spaces into one.
        String s = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        // Words of 3 letters or fewer ("egg", "oat") are left alone to avoid damaging them.
        if (s.length() > 3) {
            if (s.endsWith("ies")) {
                s = s.substring(0, s.length() - 3) + "y";          // berries -> berry
            } else if (s.endsWith("oes")) {
                s = s.substring(0, s.length() - 2);                // tomatoes -> tomato
            } else if (s.endsWith("ches") || s.endsWith("shes")
                    || s.endsWith("sses") || s.endsWith("xes")) {
                s = s.substring(0, s.length() - 2);                // peaches -> peach
            } else if (s.endsWith("s") && !s.endsWith("ss") && !s.endsWith("us")) {
                s = s.substring(0, s.length() - 1);                // eggs -> egg
            }
            if (s.endsWith("ie")) {
                s = s.substring(0, s.length() - 2) + "y";          // cookie -> cooky (matches cookies)
            }
        }
        return s;
    }

    /**
     * Unit family: mass, volume or count. Quantities can only be compared inside
     * the same family (grams with kilograms, but never grams with pieces).
     */
    private static String family(String unit) {
        switch (unit) {
            case "g":
            case "kg":
                return "mass";
            case "ml":
            case "l":
            case "cup":
            case "tbsp":
            case "tsp":
                return "volume";
            default:
                return "count";
        }
    }

    /** Converts a quantity to the base unit of its family (g, ml or pcs). */
    private static double toBase(double qty, String unit) {
        switch (unit) {
            case "kg":   return qty * 1000.0;
            case "l":    return qty * 1000.0;
            case "cup":  return qty * 240.0;
            case "tbsp": return qty * 15.0;
            case "tsp":  return qty * 5.0;
            default:     return qty; // g, ml, pcs are already base units
        }
    }

    /** Builds the lookup key "normalised name|unit family", e.g. "tomato|count". */
    private static String key(String name, String unit) {
        return normalizeName(name) + "|" + family(unit);
    }

    // ------------------------------------------------------------------
    // Matching
    // ------------------------------------------------------------------

    /** Checks every recipe against the pantry and records what is missing. */
    public static List<MatchResult> evaluateAll(List<Recipe> recipes, List<PantryItem> pantry) {
        // Step 1: total up the pantry by (normalised name + unit family), in base units.
        // merge() adds to an existing total, or starts a new one, so duplicates combine.
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem p : pantry) {
            totals.merge(key(p.getName(), p.getUnit()), toBase(p.getQuantity(), p.getUnit()),
                    Double::sum);
        }

        // Step 2: for each recipe, list the ingredients the pantry cannot cover.
        List<MatchResult> results = new ArrayList<>();
        for (Recipe r : recipes) {
            List<String> missing = new ArrayList<>();
            for (RecipeIngredient ing : r.getIngredients()) {
                Double have = totals.get(key(ing.getName(), ing.getUnit()));
                double need = toBase(ing.getQuantity(), ing.getUnit());
                // Missing if the pantry has none of it, or not enough of it.
                // The tiny 1e-9 guards against floating point rounding (0.1 + 0.2).
                if (have == null || have + 1e-9 < need) {
                    missing.add(ing.getName());
                }
            }
            results.add(new MatchResult(r, missing));
        }
        return results;
    }

    /** STRICT list: recipes with nothing missing. This is the main suggestions list. */
    public static List<MatchResult> strictMatches(List<MatchResult> all) {
        List<MatchResult> out = new ArrayList<>();
        for (MatchResult m : all) {
            if (m.missing.isEmpty()) out.add(m);
        }
        return out;
    }

    /** OPTIONAL stretch list: recipes missing exactly one ingredient. */
    public static List<MatchResult> almostThere(List<MatchResult> all) {
        List<MatchResult> out = new ArrayList<>();
        for (MatchResult m : all) {
            if (m.missing.size() == 1) out.add(m);
        }
        return out;
    }

    /** Prints 2 instead of 2.0, but keeps 0.5 as 0.5. */
    public static String formatQty(double q) {
        if (q == Math.floor(q)) return String.valueOf((long) q);
        return String.valueOf(q);
    }
}
