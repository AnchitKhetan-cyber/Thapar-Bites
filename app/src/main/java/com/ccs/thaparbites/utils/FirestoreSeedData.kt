package com.example.thaparbites.util

/**
 * SeedData.kt
 * Run this ONCE from a test or a debug screen to populate Firestore.
 * Call: FirestoreSeedData.seed(FirebaseFirestore.getInstance())
 */

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreSeedData {

    suspend fun seed(db: FirebaseFirestore) {
        seedCanteens(db)
        seedMenuItems(db)
    }

    // ════════════════════════════════════════════════════════
    // CANTEENS
    // ════════════════════════════════════════════════════════
    private suspend fun seedCanteens(db: FirebaseFirestore) {
        val canteens = listOf(
            mapOf(
                "canteenId"    to "canteen_nescafe",
                "name"         to "Nescafe",
                "location"     to "Near Main Gate, Block A",
                "imageUrl"     to "",
                "isOpen"       to true,
                "openingTime"  to "08:00",
                "closingTime"  to "22:00",
                "adminUid"     to "",
                "rating"       to 4.2,
                "totalRatings" to 0
            ),
            mapOf(
                "canteenId"    to "canteen_subway",
                "name"         to "Subway",
                "location"     to "Student Activity Center",
                "imageUrl"     to "",
                "isOpen"       to true,
                "openingTime"  to "09:00",
                "closingTime"  to "21:00",
                "adminUid"     to "",
                "rating"       to 4.5,
                "totalRatings" to 0
            ),
            mapOf(
                "canteenId"    to "canteen_fc",
                "name"         to "Food Court",
                "location"     to "Central Block Ground Floor",
                "imageUrl"     to "",
                "isOpen"       to true,
                "openingTime"  to "08:00",
                "closingTime"  to "21:30",
                "adminUid"     to "",
                "rating"       to 4.0,
                "totalRatings" to 0
            ),
            mapOf(
                "canteenId"    to "canteen_juice",
                "name"         to "Juice Corner",
                "location"     to "Near LT-1",
                "imageUrl"     to "",
                "isOpen"       to true,
                "openingTime"  to "09:00",
                "closingTime"  to "20:00",
                "adminUid"     to "",
                "rating"       to 4.3,
                "totalRatings" to 0
            ),
            mapOf(
                "canteenId"    to "canteen_dominos",
                "name"         to "Domino's",
                "location"     to "Student Activity Center",
                "imageUrl"     to "",
                "isOpen"       to true,
                "openingTime"  to "11:00",
                "closingTime"  to "23:00",
                "adminUid"     to "",
                "rating"       to 4.6,
                "totalRatings" to 0
            )
        )

        val batch = db.batch()
        canteens.forEach { canteen ->
            val ref = db.collection("canteens").document(canteen["canteenId"] as String)
            batch.set(ref, canteen)
        }
        batch.commit().await()
        println("✅ Canteens seeded")
    }

    // ════════════════════════════════════════════════════════
    // MENU ITEMS
    // ════════════════════════════════════════════════════════
    private suspend fun seedMenuItems(db: FirebaseFirestore) {
        val items = listOf(

            // ── Nescafe ──────────────────────────────────────
            item("canteen_nescafe", "Cappuccino",           "Creamy espresso with steamed milk",       40.0,  "Beverages", true,  5),
            item("canteen_nescafe", "Cold Coffee",          "Chilled blended coffee with ice cream",   60.0,  "Beverages", true,  8),
            item("canteen_nescafe", "Masala Tea",           "Freshly brewed spiced Indian tea",        20.0,  "Beverages", true,  3),
            item("canteen_nescafe", "Maggi",                "Classic two-minute noodles",              40.0,  "Snacks",    true,  5),
            item("canteen_nescafe", "Veg Sandwich",         "Grilled sandwich with fresh veggies",     60.0,  "Snacks",    true,  7),
            item("canteen_nescafe", "Paneer Tikka Roll",    "Spiced paneer wrapped in a paratha",      80.0,  "Snacks",    true,  10),
            item("canteen_nescafe", "Brownie",              "Warm chocolate brownie",                  50.0,  "Desserts",  true,  2),

            // ── Subway ───────────────────────────────────────
            item("canteen_subway",  "Veggie Delight",       "6-inch sub loaded with fresh vegetables", 159.0, "Meals",     true,  10),
            item("canteen_subway",  "Paneer Tikka Sub",     "6-inch sub with spiced paneer filling",   199.0, "Meals",     true,  12),
            item("canteen_subway",  "Chicken Teriyaki Sub", "6-inch sub with teriyaki chicken",        219.0, "Meals",     false, 12),
            item("canteen_subway",  "Aloo Patty Sub",       "Crispy potato patty with sauces",         149.0, "Meals",     true,  10),
            item("canteen_subway",  "Cookies",              "Freshly baked chocolate chip cookies",    49.0,  "Desserts",  true,  2),
            item("canteen_subway",  "Fountain Drink",       "Cold soft drink (Pepsi/7Up/Mirinda)",     49.0,  "Beverages", true,  1),

            // ── Food Court ───────────────────────────────────
            item("canteen_fc",     "Rajma Chawal",          "Kidney bean curry with steamed rice",     80.0,  "Meals",     true,  15),
            item("canteen_fc",     "Chole Bhature",         "Spiced chickpeas with fried bread",       70.0,  "Meals",     true,  12),
            item("canteen_fc",     "Dal Makhani + Roti",    "Creamy black lentil dal with 2 rotis",    90.0,  "Meals",     true,  15),
            item("canteen_fc",     "Chicken Curry + Rice",  "Homestyle chicken curry with rice",       120.0, "Meals",     false, 15),
            item("canteen_fc",     "Samosa (2 pcs)",        "Crispy potato-filled pastry",             20.0,  "Snacks",    true,  5),
            item("canteen_fc",     "Aloo Paratha",          "Stuffed flatbread with butter",           50.0,  "Meals",     true,  10),
            item("canteen_fc",     "Mango Lassi",           "Chilled blended yogurt with mango",       50.0,  "Beverages", true,  3),
            item("canteen_fc",     "Gulab Jamun (2 pcs)",   "Soft milk dumplings in sugar syrup",      30.0,  "Desserts",  true,  2),

            // ── Juice Corner ─────────────────────────────────
            item("canteen_juice",  "Watermelon Juice",      "Fresh cold-pressed watermelon",           40.0,  "Beverages", true,  5),
            item("canteen_juice",  "Mixed Fruit Juice",     "Seasonal fruits blend",                   60.0,  "Beverages", true,  5),
            item("canteen_juice",  "Sugarcane Juice",       "Cold fresh sugarcane juice",              30.0,  "Beverages", true,  3),
            item("canteen_juice",  "Strawberry Milkshake",  "Thick strawberry flavored milkshake",     80.0,  "Beverages", true,  5),
            item("canteen_juice",  "Green Detox Juice",     "Spinach, cucumber, ginger, lemon",        70.0,  "Beverages", true,  5),
            item("canteen_juice",  "Masala Lemonade",       "Tangy spiced lemonade",                   40.0,  "Beverages", true,  3),

            // ── Domino's ─────────────────────────────────────
            item("canteen_dominos","Margherita Pizza",      "Classic tomato base with cheese",         199.0, "Meals",     true,  20),
            item("canteen_dominos","Farmhouse Pizza",       "Veggie loaded pizza with golden corn",    259.0, "Meals",     true,  20),
            item("canteen_dominos","Chicken Dominator",     "Loaded with chicken toppings",            349.0, "Meals",     false, 20),
            item("canteen_dominos","Stuffed Garlic Bread",  "Cheese stuffed garlic bread sticks",      129.0, "Snacks",    true,  12),
            item("canteen_dominos","Pasta Italiano",        "Arrabbiata sauce pasta baked in oven",    179.0, "Meals",     true,  15),
            item("canteen_dominos","Choco Lava Cake",       "Warm chocolate cake with molten center",  99.0,  "Desserts",  true,  8),
            item("canteen_dominos","Cold Drink (Pepsi)",    "330ml chilled Pepsi can",                 59.0,  "Beverages", true,  1)
        )

        // Firestore batch limit = 500 ops, we're well under
        val batch = db.batch()
        items.forEach { menuItem ->
            val ref = db.collection("menuItems").document()
            batch.set(ref, menuItem + mapOf("itemId" to ref.id))
        }
        batch.commit().await()
        println("✅ Menu items seeded (${items.size} items)")
    }

    // Helper to build a menu item map
    private fun item(
        canteenId: String,
        name: String,
        description: String,
        price: Double,
        category: String,
        isVeg: Boolean,
        prepTime: Int
    ): Map<String, Any> = mapOf(
        "canteenId"               to canteenId,
        "name"                    to name,
        "description"             to description,
        "price"                   to price,
        "category"                to category,
        "imageUrl"                to "",
        "isAvailable"             to true,
        "isVeg"                   to isVeg,
        "preparationTimeMinutes"  to prepTime,
        "rating"                  to 0.0,
        "totalRatings"            to 0
    )
}
