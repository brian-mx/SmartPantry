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

/**
 * Screen 1: Pantry List (the launcher screen).
 *
 * Shows every pantry item from the database in a RecyclerView and lets the user
 * add (+ button), edit (tap a card) and delete (bin icon) items.
 *
 * ACTIVITY LIFECYCLE IN THIS SCREEN
 *  - onCreate(): runs once when the screen is created. Finds the views, creates
 *    the adapter and connects the buttons. No data is loaded here.
 *  - onResume(): runs every time the screen becomes visible again, including
 *    when the user comes back from the Add/Edit screen. The list is reloaded
 *    here so a newly added, edited or deleted item shows up immediately.
 */
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
        // A LinearLayoutManager lays the cards out in a vertical scrolling list.
        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        // "this" is passed as the Listener so the adapter can report taps back to us.
        adapter = new PantryAdapter(this);
        rvPantry.setAdapter(adapter);

        // The + button opens the Add/Edit screen with an EXPLICIT Intent.
        // No item id is sent, so that screen knows it is in "add" mode.
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

    /** Reads the pantry from SQLite and gives it to the adapter. */
    private void loadPantry() {
        List<PantryItem> items = db.getAllItems();
        // SharedPreferences holds the Settings toggles (small key/value storage).
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS, Context.MODE_PRIVATE);
        boolean alerts = prefs.getBoolean(SettingsActivity.KEY_EXPIRY, true);
        adapter.setData(items, alerts);

        // Show a friendly message instead of a blank screen when the pantry is empty.
        boolean empty = items.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvPantry.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    /** Called by the adapter when a card is tapped: open the form in "edit" mode. */
    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        // putExtra passes the item id to the next screen, which uses it to load the row.
        intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    /** Called by the adapter when the bin icon is tapped: confirm, then delete. */
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
