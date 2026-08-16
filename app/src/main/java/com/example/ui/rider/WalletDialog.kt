package com.example.ui.rider

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.model.PaymentType
import com.example.model.WalletAccount
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.ScootCyan
import com.example.ui.theme.ScootGreen
import com.example.ui.theme.ScootRed
import com.example.ui.theme.ScootYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDialog(
    wallet: WalletAccount,
    transactions: List<TransactionEntity>,
    onDismiss: () -> Unit,
    onTopUp: (amountUzs: Long, method: PaymentType) -> Unit,
    onRedeemPromo: (code: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Top-Up & Cards, 1: History
    var topUpAmount by remember { mutableLongStateOf(25000L) }
    var selectedPaymentType by remember { mutableStateOf(PaymentType.PAYME) }
    var promoCodeInput by remember { mutableStateOf("") }

    val presetAmounts = listOf(10000L, 25000L, 50000L, 100000L)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF7F9F2),
        scrimColor = Color.Black.copy(alpha = 0.7f),
        modifier = modifier.testTag("wallet_modal_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = ScootGreen,
                        shape = CircleShape,
                        border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF1A1C18),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SCOOT WALLET",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C18),
                        letterSpacing = 0.5.sp
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.testTag("wallet_close_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF1A1C18))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wallet Balance Hero Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, DarkSurfaceBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "AVAILABLE BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF424940),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%,d", wallet.balanceUzs).replace(',', ' '),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1C18)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "UZS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF424940),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Auto-refill: Active (Payme)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF424940)
                        )
                        Surface(
                            color = Color(0xFFD7E8CD),
                            border = BorderStroke(1.dp, DarkSurfaceBorder),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "UZBEKISTAN SOM",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1C18),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs: Top-up vs History
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF1A1C18),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF1A1C18)
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("TOP UP & RAILS", fontWeight = FontWeight.Black) },
                    modifier = Modifier.testTag("tab_topup")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("HISTORY (${transactions.size})", fontWeight = FontWeight.Black) },
                    modifier = Modifier.testTag("tab_history")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Top-Up View
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "SELECT AMOUNT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C18)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset Amount Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetAmounts.forEach { amount ->
                            val isSelected = topUpAmount == amount
                            Surface(
                                color = if (isSelected) ScootGreen else Color.White,
                                border = BorderStroke(2.dp, DarkSurfaceBorder),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { topUpAmount = amount }
                                    .testTag("preset_amount_${amount}")
                            ) {
                                Text(
                                    text = "${amount / 1000}k",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1A1C18),
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "PAYMENT METHOD",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C18)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Payment Rails in Uzbekistan
                    PaymentMethodItem(
                        title = "Payme",
                        subtitle = "Instant Uzbek Card • 8600 •••• 4129",
                        isSelected = selectedPaymentType == PaymentType.PAYME,
                        onClick = { selectedPaymentType = PaymentType.PAYME }
                    )
                    PaymentMethodItem(
                        title = "Click Evolution",
                        subtitle = "Uzcard / Humo Direct Debit",
                        isSelected = selectedPaymentType == PaymentType.CLICK,
                        onClick = { selectedPaymentType = PaymentType.CLICK }
                    )
                    PaymentMethodItem(
                        title = "Uzcard / Humo Switch",
                        subtitle = "Direct banking card link",
                        isSelected = selectedPaymentType == PaymentType.UZCARD,
                        onClick = { selectedPaymentType = PaymentType.UZCARD }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Promo Code Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = promoCodeInput,
                            onValueChange = { promoCodeInput = it.uppercase() },
                            placeholder = { Text("PROMO CODE (e.g. TASHKENT)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1A1C18),
                                unfocusedBorderColor = DarkSurfaceBorder,
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                focusedTextColor = Color(0xFF1A1C18),
                                unfocusedTextColor = Color(0xFF1A1C18)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).testTag("promo_input")
                        )

                        Button(
                            onClick = {
                                if (promoCodeInput.isNotBlank()) {
                                    onRedeemPromo(promoCodeInput)
                                    promoCodeInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1A1C18)
                            ),
                            border = BorderStroke(2.dp, DarkSurfaceBorder),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(54.dp).testTag("apply_promo_btn")
                        ) {
                            Text("APPLY", fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Top-Up Button
                    Button(
                        onClick = { onTopUp(topUpAmount, selectedPaymentType) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScootGreen,
                            contentColor = Color(0xFF1A1C18)
                        ),
                        border = BorderStroke(2.dp, DarkSurfaceBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("submit_topup_btn")
                    ) {
                        Text(
                            text = "TOP UP ${String.format("%,d", topUpAmount).replace(',', ' ')} UZS",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                // History List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().height(320.dp).testTag("history_list")
                ) {
                    if (transactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No previous transactions recorded.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF424940)
                                )
                            }
                        }
                    } else {
                        items(transactions) { tx ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = if (tx.isDebit) ScootRed.copy(alpha = 0.2f) else ScootGreen,
                                            shape = CircleShape,
                                            border = BorderStroke(1.5.dp, DarkSurfaceBorder),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (tx.isDebit) Icons.Default.Payment else Icons.Default.Add,
                                                contentDescription = null,
                                                tint = Color(0xFF1A1C18),
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = tx.title,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF1A1C18)
                                            )
                                            val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(tx.timestampMillis))
                                            Text(
                                                text = "$dateStr • ${tx.paymentMethod}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF424940)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${if (tx.isDebit) "-" else "+"}${String.format("%,d", tx.amountUzs).replace(',', ' ')} UZS",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Black,
                                        color = if (tx.isDebit) ScootRed else Color(0xFF1A1C18)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) Color(0xFFD7E8CD) else Color.White,
        border = BorderStroke(2.dp, DarkSurfaceBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = Color(0xFF1A1C18),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C18)
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424940)
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color(0xFF1A1C18),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
