package com.example.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Calendar;
import java.util.Locale;

/**
 * Screen 2: Add / Edit Ingredient.
 *
 * One form serves both jobs. If the Intent that started this screen carries an
 * item id (EXTRA_ITEM_ID) the form is in "edit" mode and is pre-filled from the
 * database; otherwise it creates a new item. Input is validated before saving.
 */
public class AddEditItemActivity extends AppCompatActivity {

    /** Key used to pass the item id between screens through an Intent extra. */
    public static final String EXTRA_ITEM_ID = "item_id";

    private EditText etName, etQty, etExpiry;
    private Spinner spUnit;
    private DatabaseHelper db;
    private long itemId = -1; // -1 means "no id was passed" = add mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        db = new DatabaseHelper(this);

        etName = findViewById(R.id.etName);
        etQty = findViewById(R.id.etQuantity);
        etExpiry = findViewById(R.id.etExpiry);
        spUnit = findViewById(R.id.spUnit);

        // Fill the unit drop-down from the shared UNITS list.
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, RecipeMatcher.UNITS);
        spUnit.setAdapter(unitAdapter);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish()); // back arrow closes the screen

        // Read the id sent by MainActivity. getLongExtra returns -1 if nothing was sent.
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            // EDIT MODE: load the existing row and pre-fill the form.
            toolbar.setTitle("Edit Ingredient");
            PantryItem item = db.getItem(itemId);
            if (item != null) {
                etName.setText(item.getName());
                etQty.setText(RecipeMatcher.formatQty(item.getQuantity()));
                etExpiry.setText(item.getExpiry());
                for (int i = 0; i < RecipeMatcher.UNITS.length; i++) {
                    if (RecipeMatcher.UNITS[i].equals(item.getUnit())) {
                        spUnit.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            toolbar.setTitle("Add Ingredient");
        }

        etExpiry.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btnClearDate).setOnClickListener(v -> etExpiry.setText(""));
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    /** Opens a calendar dialog and writes the chosen date as yyyy-MM-dd. */
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                etExpiry.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    /** Validates the form; if it is valid, inserts or updates the row and closes the screen. */
    private void save() {
        String name = etName.getText().toString().trim();
        String qtyText = etQty.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();
        String unit = (String) spUnit.getSelectedItem();

        // ---- Input validation ----
        // Each check shows an error on the field and stops, so the user fixes one thing at a time.
        etName.setError(null);
        etQty.setError(null);

        if (name.isEmpty()) {
            etName.setError("Ingredient name is required");
            etName.requestFocus();
            return;
        }
        if (name.length() > 40) {
            etName.setError("Name must be 40 characters or fewer");
            etName.requestFocus();
            return;
        }
        // Letters, spaces, apostrophes and hyphens only (no digits or symbols).
        if (!name.matches("[A-Za-z][A-Za-z '\\-]*")) {
            etName.setError("Use letters only (e.g. tomato, olive oil)");
            etName.requestFocus();
            return;
        }

        double qty;
        try {
            qty = Double.parseDouble(qtyText);
        } catch (NumberFormatException e) {
            // Empty text or something that is not a number.
            etQty.setError("Enter a valid number");
            etQty.requestFocus();
            return;
        }
        if (qty <= 0) {
            etQty.setError("Quantity must be greater than 0");
            etQty.requestFocus();
            return;
        }
        if (qty > 100000) {
            etQty.setError("Quantity is too large");
            etQty.requestFocus();
            return;
        }

        // ---- Create or Update ----
        if (itemId == -1) {
            db.insertItem(new PantryItem(0, name, qty, unit, expiry)); // id 0 = not saved yet
            Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
        } else {
            db.updateItem(new PantryItem(itemId, name, qty, unit, expiry));
            Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
        }
        finish(); // closing this screen returns to the list, whose onResume() reloads the data
    }
}
