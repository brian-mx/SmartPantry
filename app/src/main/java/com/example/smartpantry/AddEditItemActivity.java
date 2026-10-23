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

public class AddEditItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";

    private EditText etName, etQty, etExpiry;
    private Spinner spUnit;
    private DatabaseHelper db;
    private long itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        db = new DatabaseHelper(this);

        etName = findViewById(R.id.etName);
        etQty = findViewById(R.id.etQuantity);
        etExpiry = findViewById(R.id.etExpiry);
        spUnit = findViewById(R.id.spUnit);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, RecipeMatcher.UNITS);
        spUnit.setAdapter(unitAdapter);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
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

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                etExpiry.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void save() {
        String name = etName.getText().toString().trim();
        String qtyText = etQty.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();
        String unit = (String) spUnit.getSelectedItem();

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
        if (!name.matches("[A-Za-z][A-Za-z '\\-]*")) {
            etName.setError("Use letters only (e.g. tomato, olive oil)");
            etName.requestFocus();
            return;
        }

        double qty;
        try {
            qty = Double.parseDouble(qtyText);
        } catch (NumberFormatException e) {
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

        if (itemId == -1) {
            db.insertItem(new PantryItem(0, name, qty, unit, expiry));
            Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
        } else {
            db.updateItem(new PantryItem(itemId, name, qty, unit, expiry));
            Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
