package com.example.smartpantry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecipeMatcher {

    public static final String[] UNITS = {"g", "kg", "ml", "l", "cup", "tbsp", "tsp", "pcs"};

    public static class MatchResult {
        public final Recipe recipe;
        public final List<String> missing; // names of missing / insufficient ingredients

        public MatchResult(Recipe recipe, List<String> missing) {
            this.recipe = recipe;
            this.missing = missing;
        }
    }

    public static String normalizeName(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
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

    private static double toBase(double qty, String unit) {
        switch (unit) {
            case "kg":   return qty * 1000.0;
            case "l":    return qty * 1000.0;
            case "cup":  return qty * 240.0;
            case "tbsp": return qty * 15.0;
            case "tsp":  return qty * 5.0;
            default:     return qty; // g, ml, pcs
        }
    }

    private static String key(String name, String unit) {
        return normalizeName(name) + "|" + family(unit);
    }

    public static List<MatchResult> evaluateAll(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem p : pantry) {
            totals.merge(key(p.getName(), p.getUnit()), toBase(p.getQuantity(), p.getUnit()),
                    Double::sum);
        }

        List<MatchResult> results = new ArrayList<>();
        for (Recipe r : recipes) {
            List<String> missing = new ArrayList<>();
            for (RecipeIngredient ing : r.getIngredients()) {
                Double have = totals.get(key(ing.getName(), ing.getUnit()));
                double need = toBase(ing.getQuantity(), ing.getUnit());
                if (have == null || have + 1e-9 < need) {
                    missing.add(ing.getName());
                }
            }
            results.add(new MatchResult(r, missing));
        }
        return results;
    }

    public static List<MatchResult> strictMatches(List<MatchResult> all) {
        List<MatchResult> out = new ArrayList<>();
        for (MatchResult m : all) {
            if (m.missing.isEmpty()) out.add(m);
        }
        return out;
    }

    public static List<MatchResult> almostThere(List<MatchResult> all) {
        List<MatchResult> out = new ArrayList<>();
        for (MatchResult m : all) {
            if (m.missing.size() == 1) out.add(m);
        }
        return out;
    }

    public static String formatQty(double q) {
        if (q == Math.floor(q)) return String.valueOf((long) q);
        return String.valueOf(q);
    }
}
