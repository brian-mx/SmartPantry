package com.example.smartpantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

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

        swExpiry.setChecked(prefs.getBoolean(KEY_EXPIRY, true));
        swAlmost.setChecked(prefs.getBoolean(KEY_ALMOST, false));

        swExpiry.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean(KEY_EXPIRY, checked).apply());
        swAlmost.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean(KEY_ALMOST, checked).apply());

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
