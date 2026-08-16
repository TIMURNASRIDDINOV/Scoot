package com.example.gateway

import com.example.model.PaymentCard
import com.example.model.PaymentTransaction
import com.example.model.PaymentType
import java.util.UUID

data class PaymentAuthResult(
    val success: Boolean,
    val transactionId: String,
    val receiptNumber: String,
    val amountUzs: Long,
    val paymentGateway: String,
    val errorMessage: String? = null
)

interface PaymentProvider {
    suspend fun authorizeAndCapture(
        amountUzs: Long,
        paymentType: PaymentType,
        card: PaymentCard?,
        description: String
    ): PaymentAuthResult

    suspend fun topUpWallet(
        amountUzs: Long,
        paymentType: PaymentType,
        card: PaymentCard?
    ): PaymentAuthResult

    suspend fun applyPromoCode(code: String): Result<Long>
}

class MockUzbekPaymentProvider : PaymentProvider {

    override suspend fun authorizeAndCapture(
        amountUzs: Long,
        paymentType: PaymentType,
        card: PaymentCard?,
        description: String
    ): PaymentAuthResult {
        // Simulated network latency
        kotlinx.coroutines.delay(200)

        val txNum = (100000..999999).random()
        val gatewayName = when (paymentType) {
            PaymentType.PAYME -> "Payme UZ (JSC Inspired)"
            PaymentType.CLICK -> "Click Evolution (JSC Click)"
            PaymentType.UZCARD -> "Uzcard National Switch"
            PaymentType.HUMO -> "Humo Payment System"
            PaymentType.VISA_MC -> "Uzbekistan Visa/Mastercard Gateway"
            PaymentType.SCOOT_WALLET -> "Scoot In-App Balance"
        }

        return PaymentAuthResult(
            success = true,
            transactionId = "TX-UZ-$txNum-${System.currentTimeMillis().toString().takeLast(4)}",
            receiptNumber = "CHK-UZ-$txNum",
            amountUzs = amountUzs,
            paymentGateway = gatewayName
        )
    }

    override suspend fun topUpWallet(
        amountUzs: Long,
        paymentType: PaymentType,
        card: PaymentCard?
    ): PaymentAuthResult {
        kotlinx.coroutines.delay(250)
        val txNum = (100000..999999).random()
        return PaymentAuthResult(
            success = true,
            transactionId = "TOPUP-UZ-$txNum",
            receiptNumber = "RCP-UZ-$txNum",
            amountUzs = amountUzs,
            paymentGateway = paymentType.name
        )
    }

    override suspend fun applyPromoCode(code: String): Result<Long> {
        val normalized = code.trim().uppercase()
        return when (normalized) {
            "TASHKENT2026", "TASHKENT" -> Result.success(20000L) // +20,000 UZS bonus
            "YANGIUZB", "YANGIOZBEKISTON" -> Result.success(15000L) // +15,000 UZS bonus
            "FIRSTSCOOT", "SCOOTUZ" -> Result.success(10000L) // +10,000 UZS bonus
            "FREEUNLOCK" -> Result.success(5000L)
            else -> Result.failure(IllegalArgumentException("Promo code '$code' is invalid or expired."))
        }
    }
}
