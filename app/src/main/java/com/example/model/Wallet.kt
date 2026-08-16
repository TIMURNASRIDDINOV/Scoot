package com.example.model

enum class PaymentType {
    PAYME,
    CLICK,
    UZCARD,
    HUMO,
    VISA_MC,
    SCOOT_WALLET
}

data class PaymentCard(
    val id: String,
    val brand: PaymentType,
    val maskedNumber: String,     // e.g. "8600 •••• •••• 9214"
    val holderName: String = "TIMUR NASRIDDINOV",
    val expiry: String = "12/28",
    val isDefault: Boolean = false
)

data class PaymentTransaction(
    val id: String,
    val title: String,
    val amountUzs: Long,
    val isDebit: Boolean,         // true for ride payment, false for top-up
    val timestampMillis: Long,
    val paymentMethod: String,
    val referenceId: String
)

data class WalletAccount(
    val balanceUzs: Long = 45000L,
    val cards: List<PaymentCard> = listOf(
        PaymentCard("card-1", PaymentType.PAYME, "8600 •••• •••• 4129", "TIMUR N.", "09/27", true),
        PaymentCard("card-2", PaymentType.CLICK, "9860 •••• •••• 8831", "TIMUR N.", "11/28", false),
        PaymentCard("card-3", PaymentType.UZCARD, "8600 •••• •••• 7712", "TIMUR N.", "05/29", false)
    ),
    val appliedPromos: List<String> = listOf("WELCOME_TASHKENT")
)
