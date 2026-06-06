package com.ccs.thaparbites.navigation

object NavRoutes {
    const val SPLASH   = "splash"
    const val LOGIN    = "login"
    const val REGISTER = "register"
    const val HOME     = "home"
    const val STORE    = "store/{storeId}"        // deep-link capable
    const val MENU     = "menu/{storeId}"
    const val CART     = "cart"
    const val CHECKOUT = "checkout"
    const val ORDERS   = "orders"
    const val PROFILE  = "profile"

    const val PHONE_SETUP = "phone_setup"

    const val EXPENSES = "expenses"

    const val SHOP_LOGIN     = "shop_login"
    const val SHOP_DASHBOARD = "shop_dashboard"

    // Helpers to build parameterised routes
    fun store(storeId: String) = "store/$storeId"
    fun menu(storeId: String)  = "menu/$storeId"
}


