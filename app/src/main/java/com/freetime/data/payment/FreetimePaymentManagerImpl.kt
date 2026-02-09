package com.freetime.data.payment

import android.content.Context
import android.content.pm.PackageManager
import com.freetime.domain.payment.*
import com.freetime.sdk.payment.FreetimePaymentSDK
import com.freetime.sdk.payment.CoinType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import java.util.UUID
import kotlin.random.Random

class FreetimePaymentManagerImpl(private val context: Context) : FreetimePaymentManager {
    
    private val sdk = FreetimePaymentSDK()
    private val _paymentStatuses = MutableStateFlow<Map<String, PaymentStatus>>(emptyMap())
    
    override suspend fun initializePayment(
        amount: Double,
        currency: String,
        orderId: String,
        customerEmail: String,
        description: String
    ): Result<PaymentSession> {
        return try {
            val paymentId = UUID.randomUUID().toString()
            val expiresAt = System.currentTimeMillis() + (30 * 60 * 1000) // 30 minutes
            
            val paymentSession = PaymentSession(
                paymentId = paymentId,
                amount = amount,
                currency = currency,
                merchantId = "freetime_maker_shop",
                customerEmail = customerEmail,
                description = description,
                paymentUrl = "https://freetimemaker.github.io/Freetime-Maker-Shop/payment/$paymentId",
                expiresAt = expiresAt,
                status = PaymentStatus.PENDING
            )
            
            // Update payment status
            val currentStatuses = _paymentStatuses.value.toMutableMap()
            currentStatuses[paymentId] = PaymentStatus.PENDING
            _paymentStatuses.value = currentStatuses
            
            Result.success(paymentSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun processPayment(paymentSession: PaymentSession): Result<PaymentResult> {
        return try {
            // Simulate payment processing
            updatePaymentStatus(paymentSession.paymentId, PaymentStatus.PROCESSING)
            delay(2000) // Simulate network delay
            
            // Simulate success/failure (90% success rate for demo)
            val isSuccess = Random.nextDouble() > 0.1
            
            val status = if (isSuccess) {
                updatePaymentStatus(paymentSession.paymentId, PaymentStatus.COMPLETED)
                PaymentStatus.COMPLETED
            } else {
                updatePaymentStatus(paymentSession.paymentId, PaymentStatus.FAILED)
                PaymentStatus.FAILED
            }
            
            val paymentResult = PaymentResult(
                paymentId = paymentSession.paymentId,
                status = status,
                transactionId = if (isSuccess) UUID.randomUUID().toString() else null,
                amount = paymentSession.amount,
                currency = paymentSession.currency,
                processedAt = System.currentTimeMillis(),
                errorMessage = if (!isSuccess) "Payment failed. Please try again." else null
            )
            
            Result.success(paymentResult)
        } catch (e: Exception) {
            updatePaymentStatus(paymentSession.paymentId, PaymentStatus.FAILED)
            Result.failure(e)
        }
    }
    
    override fun getPaymentStatus(paymentId: String): Flow<PaymentStatus> {
        return _paymentStatuses.asStateFlow().map { statuses ->
            statuses[paymentId] ?: PaymentStatus.FAILED
        }
    }
    
    override suspend fun cancelPayment(paymentId: String): Result<Unit> {
        return try {
            updatePaymentStatus(paymentId, PaymentStatus.CANCELLED)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun refundPayment(paymentId: String, amount: Double?): Result<Unit> {
        return try {
            updatePaymentStatus(paymentId, PaymentStatus.REFUNDED)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun updatePaymentStatus(paymentId: String, status: PaymentStatus) {
        val currentStatuses = _paymentStatuses.value.toMutableMap()
        currentStatuses[paymentId] = status
        _paymentStatuses.value = currentStatuses
    }
    
    // External Wallet Integration (v1.0.4)
    override suspend fun getAvailableWalletApps(): Result<List<ExternalWalletApp>> {
        return try {
            // Simulate wallet apps since ExternalWalletManager might not be available
            val walletApps = listOf(
                ExternalWalletApp(
                    name = "Trust Wallet",
                    packageName = "com.wallet.crypto.trustapp",
                    supportedCoins = listOf("BTC", "ETH", "BNB"),
                    iconUrl = null,
                    isInstalled = isPackageInstalled("com.wallet.crypto.trustapp")
                ),
                ExternalWalletApp(
                    name = "MetaMask",
                    packageName = "io.metamask",
                    supportedCoins = listOf("ETH", "BNB"),
                    iconUrl = null,
                    isInstalled = isPackageInstalled("io.metamask")
                ),
                ExternalWalletApp(
                    name = "Coinbase Wallet",
                    packageName = "com.coinbase.android",
                    supportedCoins = listOf("BTC", "ETH", "SOL"),
                    iconUrl = null,
                    isInstalled = isPackageInstalled("com.coinbase.android")
                )
            )
            Result.success(walletApps)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun generatePaymentDeepLink(
        walletApp: ExternalWalletApp,
        paymentSession: PaymentSession
    ): Result<String> {
        return try {
            // Generate deep link manually since ExternalWalletManager might not be available
            val coinType = CoinType.valueOf(paymentSession.currency.uppercase())
            val amount = BigDecimal(paymentSession.amount.toString())
            
            val deepLink = when (walletApp.packageName) {
                "com.wallet.crypto.trustapp" -> "trust://btc_payment?address=merchant_wallet&amount=$amount"
                "io.metamask" -> "metamask://eth_payment?address=merchant_wallet&amount=$amount"
                "com.coinbase.android" -> "coinbase://btc_payment?address=merchant_wallet&amount=$amount"
                else -> "https://wallet-app.com/payment?address=merchant_wallet&amount=$amount&coin=${coinType.name}"
            }
            
            Result.success(deepLink)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createPaymentWithWalletSelection(
        amount: Double,
        currency: String,
        orderId: String,
        customerEmail: String,
        description: String
    ): Result<PaymentRequestWithWalletSelection> {
        return try {
            // Create regular payment session first
            val paymentSessionResult = initializePayment(amount, currency, orderId, customerEmail, description)
            if (paymentSessionResult.isFailure) {
                return Result.failure(paymentSessionResult.exceptionOrNull()!!)
            }
            
            val paymentSession = paymentSessionResult.getOrThrow()
            val availableWallets = getAvailableWalletApps().getOrThrow()
            
            val paymentRequestWithSelection = PaymentRequestWithWalletSelection(
                paymentSession = paymentSession,
                availableWallets = availableWallets
            )
            
            Result.success(paymentRequestWithSelection)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}

// Extension function for Flow mapping
private fun <T, R> Flow<T>.map(transform: suspend (value: T) -> R): Flow<R> {
    return kotlinx.coroutines.flow.flow {
        collect { value ->
            emit(transform(value))
        }
    }
}
