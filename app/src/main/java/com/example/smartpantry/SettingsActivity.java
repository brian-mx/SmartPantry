package com.example.smartpantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

/**
 * Screen 5: Settings.
 *
 * Two toggles stored in SharedPreferences (small key/value storage that
 * survives app restarts) plus a button that clears the pantry, which is handy
 * for testing the strict-matching rule.
 */
public class SettingsActivity extends AppCompatActivity {

    // Name of the preferences file and the keys inside it. They are public so the
    // other screens read the same settings.
    public static final String PREFS = "smart_pantry_prefs";
    public static final String KEY_EXPIRY = "expiry_alerts";
    public static final String KEY_ALMOST = "almost_there";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        SwitchCompat swExpiry = findViewById(R.id.swExpiry);
        SwitchCompat swAlmost = findViewById(R.id.swAlmost);

        // Show the saved values (defaults: expiry alerts on, almost-there off).
        swExpiry.setChecked(prefs.getBoolean(KEY_EXPIRY, true));
        swAlmost.setChecked(prefs.getBoolean(KEY_ALMOST, false));

        // Save a toggle the moment the user flips it. apply() writes in the background.
        swExpiry.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean(KEY_EXPIRY, checked).apply());
        swAlmost.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean(KEY_ALMOST, checked).apply());

        // Confirmation dialog first, so the pantry is never wiped by accident.
        findViewById(R.id.btnClearPantry).setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Clear pantry")
                        .setMessage("This removes ALL pantry items. Continue?")
                        .setPositiveButton("Clear", (d, w) -> {
                            new DatabaseHelper(this).clearPantry();
                            Toast.makeText(this, "Pantry cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show());

        NavHelper.setup(this, R.id.nav_settings);
    }
}
