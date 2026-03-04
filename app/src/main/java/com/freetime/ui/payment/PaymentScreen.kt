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
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var showWalletSelection by remember { mutableStateOf(false) }
    var paymentRequestWithWallet by remember { mutableStateOf<PaymentRequestWithWalletSelection?>(null) }
    var supportedCurrencies by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedCurrency by remember { mutableStateOf(currency) }

    LaunchedEffect(Unit) {
        val cryptos = try {
            paymentManager.getSupportedCryptocurrencies()
        } catch (e: Exception) {
            emptyList()
        }
        supportedCurrencies = if (cryptos.isNotEmpty()) cryptos else listOf(selectedCurrency)
        if (!supportedCurrencies.contains(selectedCurrency)) {
            selectedCurrency = supportedCurrencies.first()
        }
    }
    
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
                    
                    Text("Amount: $amount $selectedCurrency")
                    Text("Order ID: $orderId")
                    Text("Description: $description")

                    Spacer(modifier = Modifier.height(8.dp))

                    if (supportedCurrencies.size > 1) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Currency:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            TextButton(
                                onClick = {
                                    val idx = supportedCurrencies.indexOf(selectedCurrency).takeIf { it >= 0 } ?: 0
                                    val nextIndex = (idx + 1) % supportedCurrencies.size
                                    selectedCurrency = supportedCurrencies[nextIndex]
                                }
                            ) {
                                Text(selectedCurrency)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            isLoading = true
                            scope.launch {
                                val result = paymentManager.createPaymentWithWalletSelection(
                                    amount = amount,
                                    currency = selectedCurrency,
                                    orderId = orderId,
                                    customerEmail = customerEmail,
                                    description = description
                                )

                                if (result.isSuccess) {
                                    paymentRequestWithWallet = result.getOrNull()
                                    showWalletSelection = true
                                } else {
                                    isLoading = false
                                    onPaymentError(
                                        result.exceptionOrNull()?.message
                                            ?: "Failed to create payment."
                                    )
                                }
                            }
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
                scope.launch {
                    val session = paymentRequestWithWallet!!.paymentSession
                    val deepLinkResult = paymentManager.generatePaymentDeepLink(
                        walletApp = wallet,
                        paymentSession = session
                    )

                    if (deepLinkResult.isSuccess) {
                        val deepLink = deepLinkResult.getOrNull()
                        if (!deepLink.isNullOrBlank()) {
                            openDeepLink(context, deepLink)
                            onPaymentComplete(
                                PaymentResult(
                                    paymentId = session.paymentId,
                                    status = PaymentStatus.PENDING,
                                    amount = session.amount,
                                    currency = session.currency,
                                    processedAt = System.currentTimeMillis()
                                )
                            )
                        } else {
                            onPaymentError("Failed to generate payment deep link.")
                        }
                    } else {
                        onPaymentError(
                            deepLinkResult.exceptionOrNull()?.message
                                ?: "Failed to generate payment deep link."
                        )
                    }

                    isLoading = false
                    showWalletSelection = false
                }
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
