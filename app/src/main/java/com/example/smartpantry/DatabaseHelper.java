package com.example.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    private static final String T_PANTRY = "pantry";
    private static final String T_RECIPES = "recipes";
    private static final String T_ING = "recipe_ingredients";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "expiry TEXT)");

        db.execSQL("CREATE TABLE " + T_RECIPES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "steps TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + T_ING + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "FOREIGN KEY(recipe_id) REFERENCES " + T_RECIPES + "(id))");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_ING);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        onCreate(db);
    }

    private ContentValues toValues(PantryItem item) {
        ContentValues cv = new ContentValues();
        cv.put("name", item.getName());
        cv.put("quantity", item.getQuantity());
        cv.put("unit", item.getUnit());
        cv.put("expiry", item.getExpiry());
        return cv;
    }

    public long insertItem(PantryItem item) {
        return getWritableDatabase().insert(T_PANTRY, null, toValues(item));
    }

    public List<PantryItem> getAllItems() {
        List<PantryItem> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(T_PANTRY, null, null, null, null, null,
                "name COLLATE NOCASE ASC");
        try {
            while (c.moveToNext()) {
                list.add(readItem(c));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public PantryItem getItem(long id) {
        Cursor c = getReadableDatabase().query(T_PANTRY, null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (c.moveToFirst()) {
                return readItem(c);
            }
        } finally {
            c.close();
        }
        return null;
    }

    public int updateItem(PantryItem item) {
        return getWritableDatabase().update(T_PANTRY, toValues(item), "id=?",
                new String[]{String.valueOf(item.getId())});
    }

    public int deleteItem(long id) {
        return getWritableDatabase().delete(T_PANTRY, "id=?", new String[]{String.valueOf(id)});
    }

    public void clearPantry() {
        getWritableDatabase().delete(T_PANTRY, null, null);
    }

    private PantryItem readItem(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.getString(c.getColumnIndexOrThrow("expiry")));
    }

    public List<Recipe> getAllRecipes() {
        SQLiteDatabase db = getReadableDatabase();
        List<Recipe> list = new ArrayList<>();
        Cursor c = db.query(T_RECIPES, null, null, null, null, null, "name ASC");
        try {
            while (c.moveToNext()) {
                Recipe r = new Recipe(
                        c.getLong(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getString(c.getColumnIndexOrThrow("steps")));
                r.getIngredients().addAll(loadIngredients(db, r.getId()));
                list.add(r);
            }
        } finally {
            c.close();
        }
        return list;
    }

    public int getRecipeCount() {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM recipes", null
        );

        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            cursor.close();
        }
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_RECIPES, null, "id=?", new String[]{String.valueOf(id)},
                null, null, null);
        try {
            if (c.moveToFirst()) {
                Recipe r = new Recipe(
                        c.getLong(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getString(c.getColumnIndexOrThrow("steps")));
                r.getIngredients().addAll(loadIngredients(db, r.getId()));
                return r;
            }
        } finally {
            c.close();
        }
        return null;
    }

    private List<RecipeIngredient> loadIngredients(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        Cursor c = db.query(T_ING, null, "recipe_id=?", new String[]{String.valueOf(recipeId)},
                null, null, "id ASC");
        try {
            while (c.moveToNext()) {
                list.add(new RecipeIngredient(
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getDouble(c.getColumnIndexOrThrow("quantity")),
                        c.getString(c.getColumnIndexOrThrow("unit"))));
            }
        } finally {
            c.close();
        }
        return list;
    }

    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues rv = new ContentValues();
        rv.put("name", name);
        rv.put("steps", steps);
        long recipeId = db.insert(T_RECIPES, null, rv);

        for (String ing : ingredients) {
            String[] p = ing.split(":");
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", recipeId);
            iv.put("name", p[0]);
            iv.put("quantity", Double.parseDouble(p[1]));
            iv.put("unit", p[2]);
            db.insert(T_ING, null, iv);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Toast the bread.\n2. Whisk the eggs in a bowl.\n3. Melt the butter in a pan over low heat.\n4. Add the eggs and stir gently until just set.\n5. Spoon the eggs onto the toast and serve.",
                "egg:3:pcs", "bread:2:pcs", "butter:10:g");

        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta in salted water until tender.\n2. Chop the tomatoes and crush the garlic.\n3. Fry the garlic in olive oil for 30 seconds.\n4. Add the tomatoes and simmer for 10 minutes.\n5. Drain the pasta and toss through the sauce.",
                "pasta:200:g", "tomato:4:pcs", "garlic:2:pcs", "olive oil:2:tbsp");

        addRecipe(db, "Pancakes",
                "1. Whisk the flour, sugar, eggs and milk into a smooth batter.\n2. Heat a lightly greased pan.\n3. Pour in a ladle of batter and cook until bubbles form.\n4. Flip and cook the other side until golden.\n5. Serve warm.",
                "flour:200:g", "milk:300:ml", "egg:2:pcs", "sugar:2:tbsp");

        addRecipe(db, "Egg Fried Rice",
                "1. Dice the onion and carrot.\n2. Stir-fry them in a hot pan for 3 minutes.\n3. Push to the side, scramble the eggs in the pan.\n4. Add the cooked rice and soy sauce.\n5. Toss everything together for 3 minutes and serve.",
                "rice:300:g", "egg:2:pcs", "onion:1:pcs", "soy sauce:2:tbsp", "carrot:1:pcs");

        addRecipe(db, "Cheese Omelette",
                "1. Beat the eggs.\n2. Melt the butter in a pan.\n3. Pour in the eggs and let the base set.\n4. Sprinkle over the grated cheese.\n5. Fold in half and serve.",
                "egg:3:pcs", "cheese:50:g", "butter:10:g");

        addRecipe(db, "Mashed Potatoes",
                "1. Peel and chop the potatoes.\n2. Boil for 15-20 minutes until soft.\n3. Drain well.\n4. Mash with the butter and warm milk until smooth.",
                "potato:500:g", "butter:30:g", "milk:100:ml");

        addRecipe(db, "Chicken Rice Bowl",
                "1. Cook the rice.\n2. Slice the chicken breast and fry until golden and cooked through.\n3. Add the chopped onion and garlic and cook for 5 minutes.\n4. Serve the chicken mixture over the rice.",
                "chicken breast:300:g", "rice:200:g", "onion:1:pcs", "garlic:2:pcs");

        addRecipe(db, "Veggie Stir Fry",
                "1. Slice the carrots and cut the broccoli into florets.\n2. Heat the oil in a wok.\n3. Stir-fry the garlic, carrots and broccoli for 6 minutes.\n4. Add the soy sauce and toss for 1 minute.",
                "carrot:2:pcs", "broccoli:200:g", "soy sauce:2:tbsp", "garlic:2:pcs", "cooking oil:1:tbsp");

        addRecipe(db, "Tuna Pasta",
                "1. Cook the pasta.\n2. Soften the chopped onion in olive oil.\n3. Stir in the drained tuna.\n4. Toss the pasta through the tuna mixture.",
                "pasta:200:g", "tuna:1:pcs", "onion:1:pcs", "olive oil:1:tbsp");

        addRecipe(db, "Peanut Butter Banana Toast",
                "1. Toast the bread.\n2. Spread with peanut butter.\n3. Top with sliced banana.",
                "bread:2:pcs", "peanut butter:2:tbsp", "banana:1:pcs");

        addRecipe(db, "Banana Oatmeal",
                "1. Heat the milk in a pot.\n2. Stir in the oats and cook for 5 minutes.\n3. Top with sliced banana and drizzle with honey.",
                "oat:80:g", "milk:250:ml", "banana:1:pcs", "honey:1:tbsp");

        addRecipe(db, "Garlic Bread",
                "1. Mix the soft butter with crushed garlic.\n2. Spread over the bread.\n3. Bake at 200C for 8-10 minutes until golden.",
                "bread:4:pcs", "butter:40:g", "garlic:3:pcs");

        addRecipe(db, "Cheese Toastie",
                "1. Butter the outside of the bread.\n2. Place the cheese between the slices.\n3. Toast in a pan on both sides until golden and melted.",
                "bread:2:pcs", "cheese:60:g", "butter:10:g");

        addRecipe(db, "Potato Soup",
                "1. Melt the butter and soften the chopped onion.\n2. Add the diced potatoes and 500ml water.\n3. Simmer for 20 minutes.\n4. Blend, stir in the milk and heat through.",
                "potato:400:g", "onion:1:pcs", "milk:200:ml", "butter:20:g");

        addRecipe(db, "Egg Fried Noodles",
                "1. Cook the noodles and drain.\n2. Scramble the eggs in a hot pan.\n3. Add the grated carrot and noodles.\n4. Season with the soy sauce and toss well.",
                "noodle:200:g", "egg:2:pcs", "soy sauce:1:tbsp", "carrot:1:pcs");

        addRecipe(db, "Simple Greek Salad",
                "1. Chop the tomatoes, cucumber and onion.\n2. Combine in a bowl.\n3. Crumble the feta over the top.\n4. Dress with the olive oil.",
                "tomato:3:pcs", "cucumber:1:pcs", "onion:1:pcs", "olive oil:2:tbsp", "feta cheese:50:g");

        addRecipe(db, "Baked Beans on Toast",
                "1. Heat the beans in a small pot.\n2. Toast and butter the bread.\n3. Pour the beans over the toast.",
                "baked bean:400:g", "bread:2:pcs", "butter:10:g");

        addRecipe(db, "Quick Chicken Curry",
                "1. Fry the chopped onions until soft.\n2. Add the diced chicken and curry powder and cook for 5 minutes.\n3. Add the chopped tomatoes and simmer for 15 minutes.\n4. Serve with the cooked rice.",
                "chicken breast:400:g", "onion:2:pcs", "tomato:2:pcs", "curry powder:2:tbsp", "rice:200:g");

        addRecipe(db, "Banana Pancakes",
                "1. Mash the bananas.\n2. Whisk in the eggs and flour.\n3. Cook spoonfuls of batter in a hot pan until golden on both sides.",
                "banana:2:pcs", "egg:2:pcs", "flour:100:g");

        addRecipe(db, "Vegetable Soup",
                "1. Chop the carrots, potato and onion.\n2. Add to a pot with the vegetable stock.\n3. Simmer for 25 minutes.\n4. Serve hot.",
                "carrot:2:pcs", "potato:300:g", "onion:1:pcs", "vegetable stock:500:ml");

        addRecipe(db, "Egg and Cheese Wrap",
                "1. Scramble the eggs.\n2. Warm the tortillas.\n3. Fill with eggs and grated cheese.\n4. Roll up and serve.",
                "tortilla:2:pcs", "egg:3:pcs", "cheese:40:g");

        addRecipe(db, "Rice Pudding",
                "1. Combine the rice, milk and sugar in a pot.\n2. Simmer on low heat for 30 minutes, stirring often.\n3. Serve warm.",
                "rice:100:g", "milk:500:ml", "sugar:3:tbsp");
    }
}
