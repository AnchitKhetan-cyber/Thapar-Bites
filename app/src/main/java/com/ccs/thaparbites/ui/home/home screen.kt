package com.ccs.thaparbites.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ccs.thaparbites.data.dummy.*
import com.ccs.thaparbites.ui.theme.*
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.ccs.thaparbites.R

// ─────────────────────────────────────────────
//  Screen (wired to nav)
// ─────────────────────────────────────────────



@Composable
fun HomeScreen(
    cartItemCount: Int = 0,
    onStoreClick: (Store) -> Unit = {},
    onCartClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onExpensesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory())
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    var selectedLocation by remember {
        mutableStateOf(campusLocations.first())
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                cartItemCount = cartItemCount,
                onCartClick = onCartClick
            )
        },
        bottomBar = {
            HomeBottomBar(
                onHomeClick = {},
                onOrdersClick = onOrdersClick,
                onExpensesClick = onExpensesClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        when (val state = uiState) {

            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            "Loading canteens...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text("😕", fontSize = 48.sp)

                        Spacer(Modifier.height(12.dp))

                        Text(
                            "Couldn't load canteens",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = { homeViewModel.retry() }
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }

            is HomeUiState.Success -> {
                val stores =
                    state.stores.filter {
                        it.location == selectedLocation
                    }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {

                    item {
                        GreetingBanner(
                            userName = state.userName
                        )
                    }

                    item {
                        LocationSelector(
                            locations = campusLocations,
                            selected = selectedLocation,
                            onSelect = {
                                selectedLocation = it
                            }
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${stores.size} Places",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.weight(1f))

                            Text(
                                text = "at $selectedLocation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (stores.isEmpty()) {
                        item {
                            EmptyLocationState(
                                location = selectedLocation
                            )
                        }
                    } else {
                        items(
                            stores,
                            key = { it.id }
                        ) { store ->

                            StoreCard(
                                store = store,
                                onClick = {
                                    onStoreClick(store)
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}
// ─────────────────────────────────────────────
//  Top Bar
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(cartItemCount: Int, onCartClick: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ti", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Thapar Bites",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        actions = {
            BadgedBox(
                badge = {
                    if (cartItemCount > 0) {
                        Badge(
                            containerColor = Color.White,
                            contentColor   = Crimson500
                        ) { Text(cartItemCount.toString(), fontWeight = FontWeight.Bold) }
                    }
                }
            ) {
                IconButton(onClick = onCartClick) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.White)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Crimson500)
    )
}

// ─────────────────────────────────────────────
//  Bottom Navigation Bar
// ─────────────────────────────────────────────

@Composable
fun HomeBottomBar(
    currentRoute: String = "home",
    onHomeClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick  = onHomeClick,
            icon = {
                Icon(
                    if (currentRoute == "home") Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label  = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor    = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "orders",
            onClick  = onOrdersClick,
            icon = {
                Icon(
                    if (currentRoute == "orders") Icons.Filled.Receipt else Icons.Outlined.Receipt,
                    contentDescription = "Orders"
                )
            },
            label  = { Text("Orders") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor    = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
        )

        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick  = onProfileClick,
            icon = {
                Icon(
                    if (currentRoute == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            },
            label  = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor    = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
        )
    }
}

// ─────────────────────────────────────────────
//  Greeting Banner
// ─────────────────────────────────────────────

@Composable
private fun GreetingBanner(userName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Column {
            Text(
                text  = "Hey, ${userName.split(" ").first()} 👋",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = "What are you craving today?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Location Selector
// ─────────────────────────────────────────────

@Composable
private fun LocationSelector(
    locations: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column {
        Text(
            text  = "📍 Choose Location",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(locations) { location ->
                val isSelected = location == selected
                FilterChip(
                    selected = isSelected,
                    onClick  = { onSelect(location) },
                    label = {
                        Text(
                            text       = location,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor     = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled             = true,
                        selected            = isSelected,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        borderColor         = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Store Card  (unchanged from original)
// ─────────────────────────────────────────────

@Composable
fun StoreCard(
    store: Store,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = MaterialTheme.extendedColors
    val statusColor = when (store.status) {
        StoreStatus.OPEN   -> ext.storeOpen
        StoreStatus.BUSY   -> ext.storeBusy
        StoreStatus.CLOSED -> ext.storeClosed
    }
    val statusLabel = when (store.status) {
        StoreStatus.OPEN   -> "Open"
        StoreStatus.BUSY   -> "Busy"
        StoreStatus.CLOSED -> "Closed"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(enabled = store.status != StoreStatus.CLOSED, onClick = onClick),
        shape    = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(store.emoji, fontSize = 30.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = store.name,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        modifier   = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(BadgeShape)
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text       = statusLabel,
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = statusColor
                        )
                    }
                }

                Spacer(Modifier.height(3.dp))

                Text(
                    text     = store.description,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null,
                            tint = Gold400, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(
                            "${store.rating} (${store.reviewCount})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (store.status != StoreStatus.CLOSED) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccessTime, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(
                                "${store.etaMinutes} min",
                                style      = MaterialTheme.typography.labelSmall,
                                color      = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    val payLabel = if (store.paymentMethod == PaymentMethod.CASH) "UPI & Cash" else "UPI Only"
                    Text(
                        text  = payLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (store.status == StoreStatus.CLOSED) {
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                Text(
                    "Opens at ${store.timings.substringAfter("–").trim()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyLocationState(location: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏗️", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "No stores at $location yet",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Check back soon!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────
//  Previews
// ─────────────────────────────────────────────

@Preview(showBackground = true, name = "Home – Light")
@Composable
private fun HomePreviewLight() {
    ThaparBitesTheme(darkTheme = false) { HomeScreen(cartItemCount = 2) }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Home – Dark")
@Composable
private fun HomePreviewDark() {
    ThaparBitesTheme(darkTheme = true) { HomeScreen(cartItemCount = 0) }
}