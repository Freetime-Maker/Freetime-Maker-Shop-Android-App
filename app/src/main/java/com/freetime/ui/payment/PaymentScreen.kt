package com.freetime.ui.payment

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freetime.domain.payment.*

@Composable
fun PaymentScreen(
    paymentManager: FreetimePaymentManager,
    amount: Double,
    currency: String,
    orderId: String,
    customerEmail: String,
    description: String,
    onPaymentComplete: (PaymentResult) -> Unit,
    onPaymentError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var showWalletSelection by remember { mutableStateOf(false) }
    var paymentRequestWithWallet by remember { mutableStateOf<PaymentRequestWithWalletSelection?>(null) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Processing payment...")
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Payment Summary",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Amount: $amount $currency")
                    Text("Order ID: $orderId")
                    Text("Description: $description")
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            isLoading = true
                            // Create payment with wallet selection
                            // This would be called from a coroutine in real implementation
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Proceed to Payment")
                    }
                }
            }
        }
    }
    
    // Wallet Selection Dialog
    if (showWalletSelection && paymentRequestWithWallet != null) {
        WalletSelectionDialog(
            wallets = paymentRequestWithWallet!!.availableWallets,
            onWalletSelected = { wallet ->
                isLoading = true
                // Generate deep link and open wallet
                // This would be handled in a coroutine
            },
            onDismiss = {
                showWalletSelection = false
                isLoading = false
            }
        )
    }
}

fun openDeepLink(context: android.content.Context, deepLink: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle error - no app can handle the deep link
    }
}
