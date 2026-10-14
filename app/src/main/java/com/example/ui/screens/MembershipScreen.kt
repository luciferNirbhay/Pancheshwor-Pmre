package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MembershipPlan
import com.example.model.PaymentProvider
import com.example.ui.theme.ESewaGreen
import com.example.ui.theme.KhaltiPurple
import com.example.ui.theme.MobileBankingBlue
import com.example.util.NumberLocalizationUtil
import com.example.util.StringsRepo
import com.example.viewmodel.MainViewModel

@Composable
fun MembershipScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val plans by viewModel.membershipPlans.collectAsState()
    val selectedPlan by viewModel.selectedCheckoutPlan.collectAsState()
    val selectedProvider by viewModel.selectedPaymentProvider.collectAsState()
    val isProcessing by viewModel.isPaymentProcessing.collectAsState()
    val lang = currentUser.language
    val isNe = lang == com.example.model.AppLanguage.NEPALI
    val useNepaliDigits = currentUser.nepaliDigits

    var currentChosenPlan by remember { mutableStateOf(selectedPlan ?: plans.getOrNull(1) ?: plans.first()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("membership_checkout_screen"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Explanatory Banner
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = StringsRepo.get("seller_membership", lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isNe) "बिक्रेता सदस्यता लिएपछि तपाईं आफ्ना घर, जग्गा र व्यावसायिक सम्पत्तिहरू सूचीकृत गर्न सक्नुहुन्छ। खरिदकर्ताहरूका लागि एप सधैं निःशुल्क छ।"
                        else "Sellers pay a membership fee in NPR to publish listings. Buyers browse completely free.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Plan Cards
        items(plans) { plan ->
            val isSelected = plan.id == currentChosenPlan.id
            Card(
                onClick = { currentChosenPlan = plan },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("plan_card_${plan.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = if (isNe) plan.nameNe else plan.nameEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val durationText = "${plan.durationDays} ${if (isNe) "दिनको म्याद" else "Days Duration"}"
                            Text(
                                text = if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(durationText) else durationText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Price Tag
                        val priceFormatted = NumberLocalizationUtil.formatPriceNpr(
                            plan.priceNpr,
                            shortForm = false,
                            useNepali = isNe,
                            useNepaliDigits = useNepaliDigits || isNe
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = priceFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Features list
                    val features = if (isNe) plan.featuresNe else plan.featuresEn
                    features.forEach { feat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feat,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // Payment Method Selector
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isNe) "भुक्तानी माध्यम छनौट गर्नुहोस्" else "Select Nepal Payment Gateway",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // eSewa Option
                    PaymentOptionTile(
                        provider = PaymentProvider.ESEWA,
                        title = StringsRepo.get("pay_with_esewa", lang),
                        badgeColor = ESewaGreen,
                        icon = Icons.Default.AccountBalanceWallet,
                        isSelected = selectedProvider == PaymentProvider.ESEWA,
                        onClick = { viewModel.selectedPaymentProvider.value = PaymentProvider.ESEWA }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Khalti Option
                    PaymentOptionTile(
                        provider = PaymentProvider.KHALTI,
                        title = StringsRepo.get("pay_with_khalti", lang),
                        badgeColor = KhaltiPurple,
                        icon = Icons.Default.CreditCard,
                        isSelected = selectedProvider == PaymentProvider.KHALTI,
                        onClick = { viewModel.selectedPaymentProvider.value = PaymentProvider.KHALTI }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mobile Banking / connectIPS Option
                    PaymentOptionTile(
                        provider = PaymentProvider.MOBILE_BANKING,
                        title = StringsRepo.get("pay_with_mobile_banking", lang),
                        badgeColor = MobileBankingBlue,
                        icon = Icons.Default.AccountBalance,
                        isSelected = selectedProvider == PaymentProvider.MOBILE_BANKING,
                        onClick = { viewModel.selectedPaymentProvider.value = PaymentProvider.MOBILE_BANKING }
                    )
                }
            }
        }

        // Checkout Button & Processing state
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                val priceFormatted = NumberLocalizationUtil.formatPriceNpr(
                    currentChosenPlan.priceNpr,
                    shortForm = false,
                    useNepali = isNe,
                    useNepaliDigits = useNepaliDigits || isNe
                )

                Button(
                    onClick = {
                        viewModel.selectedCheckoutPlan.value = currentChosenPlan
                        viewModel.processPayment()
                    },
                    enabled = !isProcessing,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("pay_membership_btn")
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isNe) "भुक्तानी प्रमाणीकरण हुँदैछ..." else "Verifying with Gateway...",
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${StringsRepo.get("proceed_payment", lang)} ($priceFormatted)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "🔒 Secure 256-bit payment verification via ${selectedProvider.displayName} gateway. Instant seller activation.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun PaymentOptionTile(
    provider: PaymentProvider,
    title: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) badgeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, badgeColor) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = badgeColor)
            )
        }
    }
}
