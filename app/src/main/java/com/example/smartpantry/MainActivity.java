package com.example.smartpantry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PantryAdapter.Listener {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private TextView tvEmpty;
    private RecyclerView rvPantry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        tvEmpty = findViewById(R.id.tvEmpty);
        rvPantry = findViewById(R.id.rvPantry);
        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this);
        rvPantry.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddEditItemActivity.class)));

        NavHelper.setup(this, R.id.nav_pantry);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantry(); // refresh after returning from Add/Edit
    }

    private void loadPantry() {
        List<PantryItem> items = db.getAllItems();
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS, Context.MODE_PRIVATE);
        boolean alerts = prefs.getBoolean(SettingsActivity.KEY_EXPIRY, true);
        adapter.setData(items, alerts);

        boolean empty = items.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvPantry.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(final PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete item")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.deleteItem(item.getId());
                    Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                    loadPantry();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
