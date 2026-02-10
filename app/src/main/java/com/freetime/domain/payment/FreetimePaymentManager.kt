package com.freetime.domain.payment

import com.freetime.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface FreetimePaymentManager {
    suspend fun initializePayment(
        amount: Double,
        currency: String,
        orderId: String,
        customerEmail: String,
        description: String
    ): Result<PaymentSession>
    
    suspend fun processPayment(paymentSession: PaymentSession): Result<PaymentResult>
    
    fun getPaymentStatus(paymentId: String): Flow<PaymentStatus>
    
    suspend fun cancelPayment(paymentId: String): Result<Unit>
    
    suspend fun refundPayment(paymentId: String, amount: Double? = null): Result<Unit>
    
    // External Wallet Integration (v1.0.4)
    suspend fun getAvailableWalletApps(): Result<List<ExternalWalletApp>>
    
    suspend fun generatePaymentDeepLink(
        walletApp: ExternalWalletApp,
        paymentSession: PaymentSession
    ): Result<String>
    
    suspend fun createPaymentWithWalletSelection(
        amount: Double,
        currency: String,
        orderId: String,
        customerEmail: String,
        description: String
    ): Result<PaymentRequestWithWalletSelection>
}

data class PaymentSession(
    val paymentId: String,
    val amount: Double,
    val currency: String,
    val merchantId: String,
    val customerEmail: String,
    val description: String,
    val paymentUrl: String? = null,
    val expiresAt: Long,
    val status: PaymentStatus
)

data class PaymentResult(
    val paymentId: String,
    val status: PaymentStatus,
    val transactionId: String? = null,
    val amount: Double,
    val currency: String,
    val processedAt: Long,
    val errorMessage: String? = null
)

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUNDED,
    EXPIRED
}

data class ExternalWalletApp(
    val name: String,
    val packageName: String,
    val supportedCoins: List<String>,
    val iconUrl: String? = null,
    val isInstalled: Boolean = false
)

data class PaymentRequestWithWalletSelection(
    val paymentSession: PaymentSession,
    val availableWallets: List<ExternalWalletApp>,
    val selectedWallet: ExternalWalletApp? = null,
    val deepLink: String? = null
)
