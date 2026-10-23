package com.example.smartpantry;

/**
 * PantryItem is the MODEL class for one row of the "pantry" table.
 *
 * A model class is a plain Java object that only holds data. The rest of the
 * app (DatabaseHelper, adapters, activities) passes PantryItem objects around
 * instead of juggling four separate values every time.
 *
 * Example: new PantryItem(0, "eggs", 12, "pcs", "2027-09-08")
 */
public class PantryItem {

    // Primary key from the database. It is 0 for a new item that has not been
    // saved yet; SQLite generates the real id when the row is inserted.
    private long id;

    // Ingredient name exactly as the user typed it (for example "Tomatoes").
    // RecipeMatcher normalises it later, so the stored text stays readable.
    private String name;

    // How much the user has (can be a decimal such as 0.5).
    private double quantity;

    // One of the units offered by the spinner: g, kg, ml, l, cup, tbsp, tsp, pcs.
    private String unit;

    // Optional expiry date stored as text in the format yyyy-MM-dd.
    // An empty string means "no expiry date set".
    private String expiry;

    /** Creates a pantry item with every field set. */
    public PantryItem(long id, String name, double quantity, String unit, String expiry) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiry = expiry;
    }

    // ---- Getters and setters (standard Java encapsulation) ----

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }
}
