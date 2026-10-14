package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PaymentRecord
import com.example.model.PaymentStatus
import com.example.util.NumberLocalizationUtil
import com.example.util.StringsRepo
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun PaymentsHistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val lang = currentUser.language
    val isNe = lang == com.example.model.AppLanguage.NEPALI
    val useNepaliDigits = currentUser.nepaliDigits

    var selectedReceiptForDialog by remember { mutableStateOf<PaymentRecord?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("payments_history_screen"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Membership status overview
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isNe) "हालको सदस्यता स्थिति" else "Active Seller Membership",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentUser.membershipStatus == "Active") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = currentUser.membershipStatus,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val dateStr = NumberLocalizationUtil.formatDateWithBS(currentUser.membershipEndsAt, isNe)
                    Text(
                        text = "${if (isNe) "म्याद सकिने मिति" else "Expires on"}: $dateStr",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FilledTonalButton(
                        onClick = { viewModel.navigateTo(AppScreen.MEMBERSHIP_CHECKOUT) },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Upgrade, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isNe) "सदस्यता थप गर्नुहोस् वा नवीकरण गर्नुहोस्" else "Renew / Upgrade Plan", fontSize = 12.sp)
                    }
                }
            }
        }

        // Ledger Header
        item {
            Text(
                text = if (isNe) "भुक्तानी इतिहास र रसिदहरू" else "Payment Ledger & Official Receipts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (payments.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(if (isNe) "कुनै भुक्तानी रेकर्ड छैन" else "No payment records found")
                }
            }
        } else {
            items(payments) { p ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = p.planName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            val priceStr = NumberLocalizationUtil.formatPriceNpr(
                                p.amountNpr,
                                shortForm = false,
                                useNepali = isNe,
                                useNepaliDigits = useNepaliDigits || isNe
                            )
                            Text(
                                text = priceStr,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Gateway: ${p.provider.displayName} (${p.providerReference})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (p.status) {
                                    PaymentStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                                    PaymentStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                                    PaymentStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                                    PaymentStatus.REFUNDED -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Text(
                                    text = if (isNe) p.status.displayNameNe else p.status.displayNameEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val dateStr = NumberLocalizationUtil.formatDateWithBS(p.createdAt, isNe)
                        Text(
                            text = "Date: $dateStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { selectedReceiptForDialog = p }) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(StringsRepo.get("view_receipt", lang))
                            }
                        }
                    }
                }
            }
        }
    }

    // Official In-App Receipt Dialog (MP-7)
    if (selectedReceiptForDialog != null) {
        val r = selectedReceiptForDialog!!
        val dateFormatted = NumberLocalizationUtil.formatDateWithBS(r.createdAt, isNe)
        val amountStr = NumberLocalizationUtil.formatPriceNpr(r.amountNpr, false, isNe, useNepaliDigits || isNe)

        AlertDialog(
            onDismissRequest = { selectedReceiptForDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Official Tax Receipt")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pancheshwor Multipurpose and Real Estate Pvt. Ltd.",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Bhimdatta Municipality - 4, Kanchanpur, Nepal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider()

                    Text("Receipt No: ${r.id}", fontWeight = FontWeight.Bold)
                    Text("Transaction Ref: ${r.providerReference}")
                    Text("Billed To: ${r.userName} (${r.userPhone})")
                    Text("Membership Plan: ${r.planName}")
                    Text("Payment Method: ${r.provider.displayName}")
                    Text("Date: $dateFormatted")
                    Text("Status: ${r.status.displayNameEn}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount Paid:", fontWeight = FontWeight.Bold)
                        Text(amountStr, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareText = """
                            Official Receipt from Pancheshwor Real Estate:
                            Receipt ID: ${r.id}
                            Plan: ${r.planName}
                            Amount: NPR ${r.amountNpr}
                            Provider: ${r.provider.displayName}
                            Reference: ${r.providerReference}
                            Date: $dateFormatted
                        """.trimIndent()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Receipt"))
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(StringsRepo.get("download_pdf", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedReceiptForDialog = null }) {
                    Text("Close")
                }
            }
        )
    }
}
