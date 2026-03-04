package com.freetime.shop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.freetime.shop.ui.feature.home.HomeScreen
import com.freetime.shop.ui.feature.product.ProductDetailScreen
import com.freetime.shop.ui.feature.cart.CartScreen
import com.freetime.shop.ui.feature.checkout.CheckoutScreen
import com.freetime.shop.ui.feature.order.OrderSuccessScreen
import com.freetime.ui.payment.PaymentScreen
import com.freetime.domain.payment.FreetimePaymentManager
import com.freetime.shop.ui.theme.FreetimeMakerShopTheme
import org.koin.compose.koinInject
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreetimeMakerShopTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(navController)
                    }
                    composable("product/{productId}") { backStackEntry ->
                        val productId = backStackEntry.arguments?.getString("productId") ?: ""
                        ProductDetailScreen(productId, navController)
                    }
                    composable("cart") {
                        CartScreen(navController)
                    }
                    composable("checkout") {
                        CheckoutScreen(navController)
                    }
                    composable("order_success") {
                        OrderSuccessScreen(navController)
                    }
                    composable("payment/{amount}/{currency}/{email}") { backStackEntry ->
                        val amount = backStackEntry.arguments?.getString("amount")?.toDoubleOrNull() ?: 0.0
                        val currency = backStackEntry.arguments?.getString("currency") ?: "USD"
                        val email = backStackEntry.arguments?.getString("email") ?: ""
                        val paymentManager: FreetimePaymentManager = koinInject()

                        PaymentScreen(
                            paymentManager = paymentManager,
                            amount = amount,
                            currency = currency,
                            orderId = UUID.randomUUID().toString(),
                            customerEmail = email,
                            description = "Order from Freetime Maker Shop",
                            onPaymentComplete = {
                                navController.navigate("order_success") {
                                    popUpTo("home") { inclusive = false }
                                }
                            },
                            onPaymentError = {
                                // Hier könntest du später einen Snackbar/Dialog zeigen
                            }
                        )
                    }
                }
            }
        }
    }
}
