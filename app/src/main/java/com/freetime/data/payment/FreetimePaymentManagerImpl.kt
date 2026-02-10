package com.freetime.data.payment

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.edit
import com.freetime.domain.payment.*
import com.freetime.sdk.payment.CoinType
import com.freetime.sdk.payment.FreetimePaymentSDK
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import java.util.UUID
import kotlin.random.Random

class FreetimePaymentManagerImpl(private val context: Context) : FreetimePaymentManager {

    // FreetimeSDK instance
    private val sdk = FreetimePaymentSDK()

    private val _paymentStatuses = MutableStateFlow<Map<String, PaymentStatus>>(emptyMap())

    // Static merchant wallets
    private var btcWalletAddress: String? = null
    private var ethWalletAddress: String? = null
    private var ltcWalletAddress: String? = null
    private var bchWalletAddress: String? = null
    private var dogeWalletAddress: String? = null
    private var solWalletAddress: String? = null
    private var maticWalletAddress: String? = null
    private var bnbWalletAddress: String? = null
    private var tronWalletAddress: String? = null

    // Dynamic wallet fields for SDK-supported cryptos
    private val dynamicWalletFields = mutableMapOf<String, String?>()

    init {
        // Load saved wallet configuration
        loadWalletConfiguration()
        // Initialize default merchant wallets
        initializeMerchantWallets()
    }

    // Initialize merchant wallet addresses directly in code
    private fun initializeMerchantWallets() {
        btcWalletAddress = "1DsCAVrzvGokrzXpe6YR33QuTo5EppiKRE"
        ethWalletAddress = "0x3d3eee5b542975839d2dccbf2f97139debc711bc"
        ltcWalletAddress = "LU2ERRXKTeKnzpuieQcpsBteViEY7ff5Wg"
        bchWalletAddress = "qz5klapp9c4kq97psu5rg7sq9quu3vcv7qan8dn6ts"
        dogeWalletAddress = "DFZtQ1SedQFGijrR7LJ55RFBNFVQpbGULn"
        solWalletAddress = "6K6gpBF9nyrSL2vzSaFDZgAJQurkoEzPGtK67WAg6FjX"
        maticWalletAddress = "0x3d3eee5b542975839d2dccbf2f97139debc711bc"
        bnbWalletAddress = "0x3d3eee5b542975839d2dccbf2f97139debc711bc"
        tronWalletAddress = "TKUNwoQMyLuJzUzWPKwA7yw4qujz2Pz6gS"

        saveWalletConfiguration()
    }

    // Initialize wallet fields for SDK-supported cryptos
    private suspend fun initializeWalletFields() {
        val supportedCryptos = getSupportedCryptosFromSDK()
        supportedCryptos.keys.forEach { crypto ->
            dynamicWalletFields[crypto] = null
        }
        loadDynamicWalletConfiguration()
    }

    // Load dynamic wallet configuration from SharedPreferences
    private suspend fun loadDynamicWalletConfiguration() {
        val prefs = context.getSharedPreferences("freetime_wallet_config", Context.MODE_PRIVATE)
        dynamicWalletFields.keys.forEach { crypto ->
            val address = prefs.getString("wallet_$crypto", null)
            dynamicWalletFields[crypto] = address
        }
    }

    // Set custom wallet address for any supported cryptocurrency
    suspend fun setWalletAddress(crypto: String, address: String) {
        if (dynamicWalletFields.isEmpty()) {
            initializeWalletFields()
        }
        dynamicWalletFields[crypto.uppercase()] = address
        saveWalletConfiguration()
    }

    // Get wallet address for specific cryptocurrency
    suspend fun getWalletAddress(crypto: String): String? {
        if (dynamicWalletFields.isEmpty()) {
            initializeWalletFields()
        }
        return dynamicWalletFields[crypto.uppercase()]
    }

    // Get custom wallet address (dynamic first, fallback to static)
    private suspend fun getCustomWalletAddress(crypto: String): String? {
        if (dynamicWalletFields.isNotEmpty()) {
            return dynamicWalletFields[crypto.uppercase()]
        }
        return when (crypto.uppercase()) {
            "BTC" -> btcWalletAddress
            "ETH" -> ethWalletAddress
            "LTC" -> ltcWalletAddress
            "BCH" -> bchWalletAddress
            "DOGE" -> dogeWalletAddress
            "SOL" -> solWalletAddress
            "MATIC" -> maticWalletAddress
            "BNB" -> bnbWalletAddress
            "TRX" -> tronWalletAddress
            else -> null
        }
    }

    // Get all configured wallets
    suspend fun getConfiguredWallets(): Map<String, String> {
        val wallets = mutableMapOf<String, String>()

        btcWalletAddress?.let { wallets["BTC"] = it }
        ethWalletAddress?.let { wallets["ETH"] = it }
        ltcWalletAddress?.let { wallets["LTC"] = it }
        bchWalletAddress?.let { wallets["BCH"] = it }
        dogeWalletAddress?.let { wallets["DOGE"] = it }
        solWalletAddress?.let { wallets["SOL"] = it }
        maticWalletAddress?.let { wallets["MATIC"] = it }
        bnbWalletAddress?.let { wallets["BNB"] = it }
        tronWalletAddress?.let { wallets["TRX"] = it }

        if (dynamicWalletFields.isNotEmpty()) {
            dynamicWalletFields.forEach { (crypto, address) ->
                address?.let { wallets[crypto] = it }
            }
        }

        return wallets
    }

    // Save wallet configuration to SharedPreferences
    private fun saveWalletConfiguration() {
        val prefs = context.getSharedPreferences("freetime_wallet_config", Context.MODE_PRIVATE)
        prefs.edit {
            btcWalletAddress?.let { putString("wallet_BTC", it) }
            ethWalletAddress?.let { putString("wallet_ETH", it) }
            ltcWalletAddress?.let { putString("wallet_LTC", it) }
            bchWalletAddress?.let { putString("wallet_BCH", it) }
            dogeWalletAddress?.let { putString("wallet_DOGE", it) }
            solWalletAddress?.let { putString("wallet_SOL", it) }
            maticWalletAddress?.let { putString("wallet_MATIC", it) }
            bnbWalletAddress?.let { putString("wallet_BNB", it) }
            tronWalletAddress?.let { putString("wallet_TRX", it) }

            dynamicWalletFields.forEach { (crypto, address) ->
                address?.let { putString("wallet_$crypto", it) }
            }
        }
    }

    // Load wallet configuration from SharedPreferences
    private fun loadWalletConfiguration() {
        val prefs = context.getSharedPreferences("freetime_wallet_config", Context.MODE_PRIVATE)

        btcWalletAddress = prefs.getString("wallet_BTC", null)
        ethWalletAddress = prefs.getString("wallet_ETH", null)
        ltcWalletAddress = prefs.getString("wallet_LTC", null)
        bchWalletAddress = prefs.getString("wallet_BCH", null)
        dogeWalletAddress = prefs.getString("wallet_DOGE", null)
        solWalletAddress = prefs.getString("wallet_SOL", null)
        maticWalletAddress = prefs.getString("wallet_MATIC", null)
        bnbWalletAddress = prefs.getString("wallet_BNB", null)
        tronWalletAddress = prefs.getString("wallet_TRX", null)
    }

    // Get supported cryptocurrencies from FreetimeSDK dynamically
    private suspend fun getSupportedCryptosFromSDK(): Map<String, CoinType> {
        return try {
            val externalWalletManager = sdk.getExternalWalletManager()
            val allWallets = externalWalletManager.getAllSupportedWallets()
            val allSupportedCoins = allWallets.flatMap { it.supportedCoins }.toSet()
            allSupportedCoins.associate { coin -> coin.name to coin }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    override suspend fun initializePayment(
        amount: Double,
        currency: String,
        orderId: String,
        customerEmail: String,
        description: String
    ): Result<PaymentSession> {
        return try {
            val supportedCryptos = getSupportedCryptosFromSDK()

            val coinType = supportedCryptos[currency.uppercase()]
                ?: return Result.failure(
                    IllegalArgumentException(
                        "Unsupported cryptocurrency: $currency. Supported: ${supportedCryptos.keys.joinToString(", ")}"
                    )
                )

            val customWallet = getCustomWalletAddress(currency)
            val merchantWalletAddress =
                customWallet ?: "freetime_maker_shop_${currency.lowercase()}_wallet"

            val paymentGateway = sdk.createPaymentGateway(
                merchantWalletAddress = merchantWalletAddress,
                merchantCoinType = coinType
            )

            val paymentRequest = paymentGateway.createPaymentAddress(
                amount = BigDecimal(amount.toString()),
                customerReference = customerEmail,
                description = description
            )

            val paymentSession = PaymentSession(
                paymentId = paymentRequest.id,
                amount = amount,
                currency = currency,
                merchantId = "freetime_maker_shop",
                customerEmail = customerEmail,
                description = description,
                paymentUrl = "https://freetimemaker.github.io/Freetime-Maker-Shop/payment/${paymentRequest.id}",
                expiresAt = paymentRequest.expiresAt,
                status = PaymentStatus.PENDING
            )

            storePaymentLocally(paymentSession.paymentId, paymentSession, coinType)

            Result.success(paymentSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun processPayment(paymentSession: PaymentSession): Result<PaymentResult> {
        return try {
            updatePaymentStatus(paymentSession.paymentId, PaymentStatus.PROCESSING)
            delay(2000)

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

    private fun storePaymentLocally(
        paymentId: String,
        paymentSession: PaymentSession,
        coinType: CoinType
    ) {
        val prefs = context.getSharedPreferences("freetime_payments", Context.MODE_PRIVATE)
        prefs.edit {
            putString("payment_$paymentId", "${paymentSession}|crypto:${coinType.name}")
        }
        updatePaymentStatus(paymentId, PaymentStatus.PENDING)
    }

    override suspend fun getAvailableWalletApps(): Result<List<ExternalWalletApp>> {
        return try {
            val externalWalletManager = sdk.getExternalWalletManager()
            val allWalletApps = externalWalletManager.getAllSupportedWallets()

            val walletApps = allWalletApps.map { sdkWallet ->
                val supportedCoins = sdkWallet.supportedCoins.map { it.name }
                ExternalWalletApp(
                    name = sdkWallet.name,
                    packageName = sdkWallet.packageName,
                    supportedCoins = supportedCoins,
                    iconUrl = sdkWallet.iconUrl,
                    isInstalled = isPackageInstalled(sdkWallet.packageName)
                )
            }

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
            val supportedCryptos = getSupportedCryptosFromSDK()

            val coinType = supportedCryptos[paymentSession.currency.uppercase()]
                ?: return Result.failure(
                    IllegalArgumentException(
                        "Unsupported cryptocurrency: ${paymentSession.currency}. Supported: ${supportedCryptos.keys.joinToString(", ")}"
                    )
                )

            val externalWalletManager = sdk.getExternalWalletManager()
            val sdkWallet = externalWalletManager.getWalletByPackageName(walletApp.packageName)
                ?: return Result.failure(IllegalArgumentException("Wallet app not found"))

            val amount = BigDecimal(paymentSession.amount.toString())
            val customWallet = getCustomWalletAddress(paymentSession.currency)
            val merchantWalletAddress =
                customWallet ?: "freetime_maker_shop_${paymentSession.currency.lowercase()}_wallet"

            val deepLink = externalWalletManager.generatePaymentDeepLink(
                walletApp = sdkWallet,
                address = merchantWalletAddress,
                amount = amount,
                coinType = coinType
            )

            Result.success(deepLink)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSupportedCryptocurrencies(): List<String> {
        return getSupportedCryptosFromSDK().keys.toList().sorted()
    }

    suspend fun getWalletsForCryptocurrency(currency: String): Result<List<ExternalWalletApp>> {
        return try {
            val supportedCryptos = getSupportedCryptosFromSDK()
            val coinType = supportedCryptos[currency.uppercase()]
                ?: return Result.failure(
                    IllegalArgumentException(
                        "Unsupported cryptocurrency: $currency. Supported: ${supportedCryptos.keys.joinToString(", ")}"
                    )
                )

            val externalWalletManager = sdk.getExternalWalletManager()
            val walletApps = externalWalletManager.getWalletsForCryptocurrency(coinType).map { sdkWallet ->
                ExternalWalletApp(
                    name = sdkWallet.name,
                    packageName = sdkWallet.packageName,
                    supportedCoins = sdkWallet.supportedCoins.map { it.name },
                    iconUrl = sdkWallet.iconUrl,
                    isInstalled = isPackageInstalled(sdkWallet.packageName)
                )
            }

            Result.success(walletApps)
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
            val supportedCryptos = getSupportedCryptosFromSDK()

            val coinType = supportedCryptos[currency.uppercase()]
                ?: return Result.failure(
                    IllegalArgumentException(
                        "Unsupported cryptocurrency: $currency. Supported: ${supportedCryptos.keys.joinToString(", ")}"
                    )
                )

            val customWallet = getCustomWalletAddress(currency)
            val merchantWalletAddress =
                customWallet ?: "freetime_maker_shop_${currency.lowercase()}_wallet"

            val paymentGateway = sdk.createUsdPaymentGatewayWithWalletSupport(
                merchantWalletAddress = merchantWalletAddress,
                merchantCoinType = coinType
            )

            val usdPaymentRequestWithSelection = paymentGateway.createUsdPaymentWithWalletSelection(
                usdAmount = BigDecimal(amount.toString()),
                email = customerEmail,
                note = description
            )

            val paymentId = UUID.randomUUID().toString()
            val paymentSession = PaymentSession(
                paymentId = paymentId,
                amount = amount,
                currency = currency,
                merchantId = "freetime_maker_shop",
                customerEmail = customerEmail,
                description = description,
                paymentUrl = "https://freetimemaker.github.io/Freetime-Maker-Shop/payment/$paymentId",
                expiresAt = System.currentTimeMillis() + (30 * 60 * 1000),
                status = PaymentStatus.PENDING
            )

            storePaymentLocally(paymentSession.paymentId, paymentSession, coinType)

            val availableWallets = usdPaymentRequestWithSelection.availableWalletApps
                .filter { sdkWallet -> sdkWallet.supportedCoins.contains(coinType) }
                .map { sdkWallet ->
                    ExternalWalletApp(
                        name = sdkWallet.name,
                        packageName = sdkWallet.packageName,
                        supportedCoins = sdkWallet.supportedCoins.map { it.name },
                        iconUrl = sdkWallet.iconUrl,
                        isInstalled = isPackageInstalled(sdkWallet.packageName)
                    )
                }

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

// Simple Flow.map extension to avoid extra imports
private fun <T, R> Flow<T>.map(transform: suspend (value: T) -> R): Flow<R> {
    return kotlinx.coroutines.flow.flow {
        collect { value ->
            emit(transform(value))
        }
    }
}
