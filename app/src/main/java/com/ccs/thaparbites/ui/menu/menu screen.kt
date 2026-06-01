package com.ccs.thaparbites.ui.menu

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ccs.thaparbites.data.dummy.MenuItem
import com.ccs.thaparbites.data.dummy.dummyMenuItems
import com.ccs.thaparbites.data.dummy.dummyStores
import com.ccs.thaparbites.navigation.NavRoutes.store
import com.ccs.thaparbites.ui.shared.SharedCartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    storeId: String,
    onBack: () -> Unit,
    onViewCart: () -> Unit,
    // Receives the Activity-scoped SharedCartViewModel from MainActivity
    cartViewModel: SharedCartViewModel
) {
    val menuViewModel: MenuViewModel = viewModel(
        factory = MenuViewModel.Factory(storeId)
    )

    val uiState by menuViewModel.uiState.collectAsState()

    when (val state = uiState) {

        MenuUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        is MenuUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(state.message)
            }
            return
        }

        is MenuUiState.Success -> {

            val store = state.store
            val allItems = state.menuItems

            val cartItems by cartViewModel.cartItems.collectAsState()
            val totalQty = cartItems.sumOf { it.quantity }

            val categories =
                listOf("All") + allItems.map { it.category }.distinct()

            var selectedCategory by remember { mutableStateOf("All") }
            var vegOnly by remember { mutableStateOf(false) }   // ← add this

            val displayed = allItems
                .filter { if (selectedCategory == "All") true else it.category == selectedCategory }
                .filter { if (vegOnly) it.isVeg else true }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "${store?.emoji ?: "🍽️"}  ${store?.name ?: "Menu"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                if (store != null) {
                                    Text(
                                        text = store.location,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            Box {
                                IconButton(onClick = onViewCart) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = "Cart",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (totalQty > 0) {
                                    Badge(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset((-6).dp, 6.dp)
                                    ) {
                                        Text(totalQty.toString(), fontSize = 10.sp)
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    AnimatedVisibility(
                        visible = totalQty > 0,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        CartSummaryBar(
                            itemCount = totalQty,
                            subtotal  = cartViewModel.subtotal,
                            onClick   = onViewCart
                        )
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Category chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        LazyRow(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 10.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (vegOnly) Color(0xFF2E7D32)
                                    else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    1.5.dp,
                                    Color(0xFF2E7D32),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { vegOnly = !vegOnly },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .border(
                                        1.5.dp,
                                        if (vegOnly) Color.White else Color(0xFF2E7D32),
                                        RoundedCornerShape(3.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (vegOnly) Color.White
                                            else Color(0xFF2E7D32)
                                        )
                                )
                            }
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp)

                    val listState = rememberLazyListState()

                    LaunchedEffect(vegOnly, selectedCategory) {
                        listState.scrollToItem(0)
                    }

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(displayed, key = { it.id }) { item ->

                            val quantity =
                                cartItems.find { it.menuItem.id == item.id }?.quantity ?: 0

                            MenuItemCard(
                                item = item,
                                quantity = quantity,
                                onAdd = { cartViewModel.addItem(item) },
                                onRemove = { cartViewModel.removeItem(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Menu item card ───────────────────────────────────────────────────────────

@Composable
fun MenuItemCard(
    item: MenuItem,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji thumbnail
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VegDot(isVeg = item.isVeg)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text     = item.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text     = item.description,
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text       = "₹${item.price}",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    color      = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            when {
                !item.isAvailable -> SoldOutBadge()
                quantity == 0     -> AddButton(onClick = onAdd)
                else              -> QuantityStepper(quantity, onAdd, onRemove)
            }
        }
    }
}

@Composable
fun VegDot(isVeg: Boolean) {
    val color = if (isVeg) Color(0xFF2E7D32) else Color(0xFFB71C1C)
    Box(
        modifier = Modifier
            .size(14.dp)
            .border(1.5.dp, color, RoundedCornerShape(2.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun SoldOutBadge() {
    Text(
        text     = "Sold Out",
        fontSize = 12.sp,
        color    = Color.Gray,
        modifier = Modifier
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

@Composable
fun AddButton(onClick: () -> Unit) {
    Button(
        onClick          = onClick,
        shape            = RoundedCornerShape(10.dp),
        contentPadding   = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
        colors           = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text("Add", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun QuantityStepper(quantity: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        IconButton(onClick = onRemove, modifier = Modifier.size(30.dp)) {
            Text("−", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text       = quantity.toString(),
            color      = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize   = 14.sp,
            modifier   = Modifier.padding(horizontal = 4.dp)
        )
        IconButton(onClick = onAdd, modifier = Modifier.size(30.dp)) {
            Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Bottom cart bar ──────────────────────────────────────────────────────────

@Composable
fun CartSummaryBar(itemCount: Int, subtotal: Double, onClick: () -> Unit) {
    Surface(
        modifier       = Modifier.fillMaxWidth(),
        color          = MaterialTheme.colorScheme.primary,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "$itemCount ${if (itemCount == 1) "item" else "items"}",
                    color    = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
                Text(
                    "₹${subtotal.toInt()}",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
            }
            Button(
                onClick = onClick,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor   = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("View Cart", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}