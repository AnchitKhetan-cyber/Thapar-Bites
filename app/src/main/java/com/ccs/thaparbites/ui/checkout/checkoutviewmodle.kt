package com.ccs.thaparbites.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccs.thaparbites.data.dummy.CartItem
import com.ccs.thaparbites.data.dummy.PaymentMethod
import com.ccs.thaparbites.data.dummy.Store
import com.ccs.thaparbites.data.dummy.UserProfile
import com.ccs.thaparbites.data.repository.CartRepository
import com.ccs.thaparbites.data.repository.OrderRepository
import com.ccs.thaparbites.data.repository.StoreRepository
import com.ccs.thaparbites.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// Sealed UI state
// ─────────────────────────────────────────────────────────────────────────────

sealed class CheckoutUiState {
    object Idle    : CheckoutUiState()
    object Placing : CheckoutUiState()
    data class Success(val orderId: String) : CheckoutUiState()
    data class Error(val message: String)   : CheckoutUiState()
}

// ─────────────────────────────────────────────────────────────────────────────
// Consolidated screen state
// Every property read by CheckoutScreen lives here.
// ─────────────────────────────────────────────────────────────────────────────

data class CheckoutState(
    // loading / error / success
    val uiState: CheckoutUiState = CheckoutUiState.Idle,

    // data
    val cart:            List<CartItem> = emptyList(),
    val store:           Store?         = null,
    val user:            UserProfile    = UserProfile(),

    // payment
    val selectedPayment: PaymentMethod  = PaymentMethod.UPI,

    // billing
    val subtotal:        Double         = 0.0,
    val deliveryFee:     Double         = 10.0,
) {
    /** Derived; the screen reads state.total directly. */
    val total: Double get() = subtotal + deliveryFee
}

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val userRepository:  UserRepository,
    private val cartRepository:  CartRepository,   // supplies the active cart
    private val storeRepository: StoreRepository,  // supplies the current store
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState())
    /** Single state flow – collected by the screen as: val state by viewModel.state.collectAsState() */
    val state: StateFlow<CheckoutState> = _state.asStateFlow()

    // ── Initialisation ───────────────────────────────────────────────────────

    init {
        loadCheckoutData()
    }

    /**
     * Loads cart, store, and user profile in parallel.
     * Call this from init{} or re-call it after a retry.
     */
    private fun loadCheckoutData() {
        viewModelScope.launch {
            // Cart is already in memory — instant
            val cart  = cartRepository.getActiveCart()
            // Store needs a Firestore round-trip
            val store = storeRepository.getCurrentStore()
            // User from your existing UserRepository
            val user  = userRepository.getUser()

            if (user == null) {
                _state.update { it.copy(uiState = CheckoutUiState.Error("Could not load user profile")) }
                return@launch
            }
            if (cart.isEmpty()) {
                _state.update { it.copy(uiState = CheckoutUiState.Error("Your cart is empty")) }
                return@launch
            }
            if (store == null) {
                _state.update { it.copy(uiState = CheckoutUiState.Error("Could not load store info")) }
                return@launch
            }

            val subtotal = cart.sumOf {
                (it.menuItem.price * it.quantity).toDouble()
            }

            _state.update {
                it.copy(
                    cart    = cart,
                    store   = store,
                    user    = user,
                    subtotal = subtotal,
                    uiState  = CheckoutUiState.Idle
                )
            }
        }
    }

    // ── Payment selection ────────────────────────────────────────────────────

    /**
     * Called by the screen's PaymentOption rows:
     *   viewModel.selectPayment(PaymentMethod.UPI)
     *   viewModel.selectPayment(PaymentMethod.CASH)
     */
    fun selectPayment(method: PaymentMethod) {
        _state.update { it.copy(selectedPayment = method) }
    }

    // ── Place order ──────────────────────────────────────────────────────────

    /**
     * Matches the exact call-site in CheckoutScreen:
     *
     *   viewModel.placeOrder { upiId, storeName, amount ->
     *       launchUpiIntent(context, upiId, storeName, amount)
     *   }
     *
     * The lambda is invoked (on the main thread, before persisting) only when
     * the selected payment method is UPI, so the Intent fires at the right moment.
     * Context stays in the UI layer – the ViewModel never touches it.
     */
    fun placeOrder(
        onLaunchUpiIntent: (upiId: String, storeName: String, amount: Double) -> Unit
    ) {
        val snap = _state.value

        // ── Pre-flight guards ────────────────────────────────────────────────
        if (snap.uiState is CheckoutUiState.Placing) return  // already in flight

        val store = snap.store
        if (store == null) {
            _state.update { it.copy(uiState = CheckoutUiState.Error("Store information is missing")) }
            return
        }
        if (snap.cart.isEmpty()) {
            _state.update { it.copy(uiState = CheckoutUiState.Error("Your cart is empty")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(uiState = CheckoutUiState.Placing) }

            // Re-fetch user inside the coroutine for freshness
            val user = userRepository.getUser()
            if (user == null) {
                _state.update { it.copy(uiState = CheckoutUiState.Error("Could not load user profile")) }
                return@launch
            }

            // Fire the UPI intent before persisting the order
            if (snap.selectedPayment == PaymentMethod.UPI) {
                onLaunchUpiIntent(store.upiId, store.name, snap.total)
            }

            val orderId = orderRepository.placeOrder(
                cart          = snap.cart,
                store         = store,
                userProfile   = user,
                paymentMethod = snap.selectedPayment,
                subtotal      = snap.subtotal,
                deliveryFee   = snap.deliveryFee,
                total         = snap.total
            )

            _state.update {
                it.copy(
                    uiState = if (orderId != null)
                        CheckoutUiState.Success(orderId)
                    else
                        CheckoutUiState.Error("Failed to place order. Please try again.")
                )
            }
        }
    }

    // ── Error handling ───────────────────────────────────────────────────────

    /**
     * Called when the user taps the error banner:
     *   viewModel.dismissError()
     */
    fun dismissError() {
        _state.update { it.copy(uiState = CheckoutUiState.Idle) }
    }

    /**
     * Call from the success screen's "back to home" to reset before re-entry.
     */
    fun resetState() {
        _state.update { it.copy(uiState = CheckoutUiState.Idle) }
    }
}