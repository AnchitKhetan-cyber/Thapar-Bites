package com.ccs.thaparbites.data.dummy

import java.util.Date


enum class StoreStatus { OPEN, CLOSED, BUSY }
enum class PaymentMethod { CASH, UPI }  // match repository exactly
enum class OrderStatus { PLACED, CONFIRMED, PREPARING, READY, DELIVERED, CANCELLED }

data class Store(
    val id: String,
    val name: String,
    val location: String,
    val description: String,
    val rating: Float,
    val reviewCount: Int,
    val status: StoreStatus,
    val etaMinutes: Int,
    val paymentMethod: PaymentMethod,
    val timings: String,
    val emoji: String ,         // used as avatar placeholder
    val upiId: String = ""
)

data class MenuItem(
    val id: String,
    val storeId: String,
    val name: String,
    val description: String,
    val price: Int,            // in ₹
    val category: String,
    val isVeg: Boolean,
    val isAvailable: Boolean,
    val emoji: String
)

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
)

data class Order(
    val id: String,
    val storeName: String,
    val storeEmoji: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val status: OrderStatus,
    val paymentMethod: PaymentMethod,
    val placedAt: Date
)

data class UserProfile(
    val uid: String = "",
    val name: String ="",
    val email: String = "",
    val phone: String = "",
    val hostelName: String = ""
)



val campusLocations = listOf("COS", "Kravings", "G-Block", "Aahar", "TSLAS")


val dummyStores = listOf(
    Store("s1","Chai Point","COS","Freshly brewed chai, snacks & more",4.5f,210,StoreStatus.OPEN,8,PaymentMethod.CASH,"8 AM – 10 PM","☕"),
    Store("s2","Burger Barn","COS","Juicy burgers, fries & shakes",4.2f,187,StoreStatus.BUSY,18,PaymentMethod.CASH,"10 AM – 11 PM","🍔"),
    Store("s3","The Wrap Co.","COS","Rolls, wraps & kathi rolls",4.0f,95,StoreStatus.OPEN,12,PaymentMethod.UPI,"9 AM – 9 PM","🌯"),
    Store("s4","Punjabi Dhaba","Kravings","Authentic Punjabi home-style food",4.7f,340,StoreStatus.OPEN,20,PaymentMethod.CASH,"12 PM – 10 PM","🍛"),
    Store("s5","Pizza Stop","Kravings","Wood-fired pizzas & garlic bread",4.1f,155,StoreStatus.CLOSED,0,PaymentMethod.UPI,"11 AM – 11 PM","🍕"),
    Store("s6","Momos Corner","Kravings","Steamed, fried & tandoori momos",4.6f,422,StoreStatus.OPEN,10,PaymentMethod.CASH,"11 AM – 10 PM","🥟"),
    Store("s7","Juice Junction","G-Block","Fresh fruit juices & smoothies",4.3f,130,StoreStatus.OPEN,6,PaymentMethod.UPI,"8 AM – 8 PM","🥤"),
    Store("s8","Noodle House","G-Block","Noodles, fried rice & Hakka specialties",3.9f,88,StoreStatus.BUSY,22,PaymentMethod.CASH,"11 AM – 10 PM","🍜"),
    Store("s9","Aahar Cafeteria","Aahar","Daily thali & comfort food",4.4f,510,StoreStatus.OPEN,15,PaymentMethod.CASH,"7 AM – 10 PM","🍱"),
    Store("s10","Sandwich Studio","TSLAS","Loaded sandwiches & toasties",4.0f,67,StoreStatus.OPEN,9,PaymentMethod.UPI,"8 AM – 8 PM","🥪")
)

// ─────────────────────────────────────────────
//  Dummy Menu Items
// ─────────────────────────────────────────────

val dummyMenuItems = listOf(
    // Chai Point (s1)
    MenuItem("m1","s1","Masala Chai","Classic spiced tea with ginger",15,  "Hot Drinks",true,true,"☕"),
    MenuItem("m2","s1","Cutting Chai","Strong half-cup chai",10,            "Hot Drinks",true,true,"☕"),
    MenuItem("m3","s1","Samosa (2 pcs)","Crispy potato stuffed samosas",20, "Snacks",true,true,"🥟"),
    MenuItem("m4","s1","Bread Omelette","Egg omelette with buttered bread",40,"Snacks",false,true,"🍳"),
    MenuItem("m5","s1","Cold Coffee","Chilled blended coffee with milk",60,  "Cold Drinks",true,true,"🧋"),

    // Burger Barn (s2)
    MenuItem("m6","s2","Veg Burger","Crispy aloo tikki with fresh veggies",80,"Burgers",true,true,"🍔"),
    MenuItem("m7","s2","Chicken Burger","Juicy grilled chicken patty",120,   "Burgers",false,true,"🍔"),
    MenuItem("m8","s2","Cheese Fries","Loaded with cheddar & jalapeños",70,  "Sides",true,true,"🍟"),
    MenuItem("m9","s2","Oreo Shake","Thick blended Oreo milkshake",90,       "Drinks",true,true,"🥤"),
    MenuItem("m10","s2","Zinger Burger","Spicy crispy chicken",130,          "Burgers",false,false,"🍔"),

    // Momos Corner (s6)
    MenuItem("m11","s6","Steamed Veg Momos (6)","Classic steamed with chutney",60,"Steamed",true,true,"🥟"),
    MenuItem("m12","s6","Fried Chicken Momos (6)","Deep-fried juicy momos",90, "Fried",false,true,"🥟"),
    MenuItem("m13","s6","Tandoori Momos (6)","Charred smoky momos",100,       "Tandoori",true,true,"🥟"),
    MenuItem("m14","s6","Momo Soup","Clear broth with floating momos",50,     "Soups",true,true,"🍲"),

    // Punjabi Dhaba (s4)
    MenuItem("m15","s4","Dal Makhani","Slow-cooked black lentils",120,        "Mains",true,true,"🫕"),
    MenuItem("m16","s4","Butter Chicken","Tender chicken in tomato gravy",160,"Mains",false,true,"🍛"),
    MenuItem("m17","s4","Garlic Naan","Soft naan with garlic butter",30,      "Breads",true,true,"🫓"),
    MenuItem("m18","s4","Veg Thali","Full meal with 4 items + roti",150,      "Thali",true,true,"🍱"),
    MenuItem("m19","s4","Lassi (Sweet)","Chilled thick Punjabi lassi",50,     "Drinks",true,true,"🥛"),

    // Aahar Cafeteria (s9)
    MenuItem("m20","s9","Morning Breakfast Thali","Poha/Upma + tea",60,       "Breakfast",true,true,"🌅"),
    MenuItem("m21","s9","Lunch Thali","Dal + sabzi + rice + roti",100,        "Lunch",true,true,"🍱"),
    MenuItem("m22","s9","Rajma Chawal","Classic comfort bowl",80,             "Mains",true,true,"🍛"),
    MenuItem("m23","s9","Paneer Bhurji","Scrambled cottage cheese",90,        "Mains",true,true,"🧀"),
)


val dummyOrders = listOf(
    Order(
        id = "ORD-2031", storeName = "Momos Corner", storeEmoji = "🥟",
        items = listOf(CartItem(dummyMenuItems[10], 2), CartItem(dummyMenuItems[13], 1)),
        subtotal = 160.0, deliveryFee = 10.0, total = 170.0,
        status = OrderStatus.DELIVERED,
        paymentMethod = PaymentMethod.UPI,
        placedAt = Date()
    ),
    Order(
        id = "ORD-2030", storeName = "Burger Barn", storeEmoji = "🍔",
        items = listOf(CartItem(dummyMenuItems[6], 1), CartItem(dummyMenuItems[7], 1), CartItem(dummyMenuItems[8], 1)),
        subtotal = 310.0, deliveryFee = 10.0, total = 320.0,
        status = OrderStatus.DELIVERED,
        paymentMethod = PaymentMethod.UPI,
        placedAt = Date(System.currentTimeMillis() - 86_400_000)
    ),
    Order(
        id = "ORD-2029", storeName = "Chai Point", storeEmoji = "☕",
        items = listOf(CartItem(dummyMenuItems[0], 2), CartItem(dummyMenuItems[2], 1)),
        subtotal = 40.0, deliveryFee = 10.0, total = 50.0,
        status = OrderStatus.DELIVERED,
        paymentMethod = PaymentMethod.CASH,
        placedAt = Date(System.currentTimeMillis() - 7 * 86_400_000)
    ),
    Order(
        id = "ORD-2028", storeName = "Punjabi Dhaba", storeEmoji = "🍛",
        items = listOf(CartItem(dummyMenuItems[17], 1), CartItem(dummyMenuItems[16], 2)),
        subtotal = 200.0, deliveryFee = 10.0, total = 210.0,
        status = OrderStatus.CANCELLED,
        paymentMethod = PaymentMethod.UPI,
        placedAt = Date(System.currentTimeMillis() - 11 * 86_400_000)
    )
)



val dummyUser = UserProfile(
    name = "Arjun Sharma",
    email = "arjun.sharma@thapar.edu",
    phone = "9876543210",
    hostelName = "Kailash Boys Hostel"
)