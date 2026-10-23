# Smart Pantry Manager

A Java Android app that helps reduce food waste. The user records the ingredients they already
have at home (their "pantry"), and the app suggests recipes they can cook **strictly** from those
ingredients - a recipe is only suggested if every ingredient it needs is in the pantry in at
least the required quantity.

Module: Mobile App Development 700 - Practical Assignment (Richfield).

## Features
- Pantry management: add, edit and delete ingredients (name, quantity, unit, optional expiry date)
- Pantry list screen (RecyclerView with a custom adapter, bound to the database)
- 22 pre-loaded recipes (seeded on first run)
- Suggested Recipes screen using the strict-matching rule
- Recipe detail screen (ingredients + method)
- Settings screen (expiry highlighting toggle, optional "Almost There" list, clear pantry)
- Friendly message when no recipes match
- Input validation on the add/edit form
- Bottom navigation bar

## Strict-matching rule
`RecipeMatcher.java` implements the rule. Names are normalised (case, simple plurals such as
"tomatoes" = "tomato"), units are converted (kg->g, l->ml, cup/tbsp/tsp->ml), and duplicate pantry
rows of the same ingredient are added together before comparing against the recipe.

## Database choice: SQLite
SQLite (via `SQLiteOpenHelper`) runs locally on the device, needs no network, account or backend,
and persists data across app restarts. It fits this app because the data is small and relational
(pantry items, recipes, recipe ingredients) and only one user needs it.

Tables: `pantry`, `recipes`, `recipe_ingredients`.

## Setup / run instructions
1. Install Android Studio (latest stable).
2. Clone this repository: `git clone https://github.com/brian-mx/SmartPantry.git`
3. In Android Studio choose **File > Open** and select the project folder.
4. Let Gradle sync finish (needs an internet connection the first time).
5. Create or start an emulator (API 24 or higher), or connect a physical device with USB debugging on.
6. Press **Run**.

No API keys, location permissions or internet access are required at runtime.
