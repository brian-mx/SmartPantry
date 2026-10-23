package com.example.smartpantry;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavHelper {

    public static void setup(final Activity activity, final int selectedId) {
        BottomNavigationView nav = activity.findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(selectedId);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) {
                return true;
            }

            Intent intent;
            if (id == R.id.nav_pantry) {
                intent = new Intent(activity, MainActivity.class);
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
