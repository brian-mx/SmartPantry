package com.example.smartpantry;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Wires up the bottom navigation bar shared by the three main screens
 * (Pantry, Recipes, Settings) using explicit Intents.
 *
 * Keeping this in one helper class means the navigation code is written once
 * instead of being copied into every Activity.
 */
public class NavHelper {

    public static void setup(final Activity activity, final int selectedId) {
        BottomNavigationView nav = activity.findViewById(R.id.bottom_nav);
        // Highlight the tab that belongs to the current screen.
        nav.setSelectedItemId(selectedId);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) {
                return true; // already on this screen, nothing to do
            }

            Intent intent;
            if (id == R.id.nav_pantry) {
                intent = new Intent(activity, MainActivity.class);
                // Reuse the existing Pantry screen instead of stacking a new copy on top.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else if (id == R.id.nav_recipes) {
                intent = new Intent(activity, SuggestedRecipesActivity.class);
            } else {
                intent = new Intent(activity, SettingsActivity.class);
            }
            activity.startActivity(intent);

            // Pantry is the home screen and stays in the back stack; the others are replaced.
            if (!(activity instanceof MainActivity)) {
                activity.finish();
            }
            return true;
        });
    }
}
