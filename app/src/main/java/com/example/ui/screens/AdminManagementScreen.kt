package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import com.example.model.CompanyInfo
import com.example.model.PaymentStatus
import com.example.util.NumberLocalizationUtil
import com.example.viewmodel.MainViewModel

@Composable
fun AdminManagementScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val companyInfo by viewModel.companyInfo.collectAsState()
    val plans by viewModel.membershipPlans.collectAsState()
    val lang = currentUser.language
    val isNe = lang == com.example.model.AppLanguage.NEPALI

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Company info edit state
    var editEmail by remember(companyInfo) { mutableStateOf(companyInfo.email) }
    var editWhatsApp by remember(companyInfo) { mutableStateOf(companyInfo.whatsAppNumber) }
    var editPhone by remember(companyInfo) { mutableStateOf(companyInfo.phone) }
    var editAddress by remember(companyInfo) { mutableStateOf(companyInfo.address) }
    var editHours by remember(companyInfo) { mutableStateOf(companyInfo.openingHours) }

    val filteredPayments = remember(payments, searchQuery) {
        if (searchQuery.isBlank()) payments
        else payments.filter {
            it.userName.contains(searchQuery, ignoreCase = true) ||
            it.providerReference.contains(searchQuery, ignoreCase = true) ||
            it.id.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_management_screen")
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Payments (${payments.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Company Info") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Plans") }
            )
        }

        if (selectedTab == 0) {
            // Payments Ledger with search & CSV export
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by user, ref or txn...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = {
                                val csv = StringBuilder()
                                csv.append("TxnId,User,Phone,Plan,Provider,Amount,Status,Date\n")
                                payments.forEach { p ->
                                    csv.append("${p.id},${p.userName},${p.userPhone},${p.planName},${p.provider.displayName},${p.amountNpr},${p.status.name},${p.createdAt}\n")
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Payments CSV", csv.toString()))
                                viewModel.showSnackbar("Exported ${payments.size} records to CSV (copied to clipboard)!")
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CSV", fontSize = 12.sp)
                        }
                    }
                }

                items(filteredPayments) { p ->
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
                                Column {
                                    Text(text = p.userName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(text = p.userPhone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = "NPR ${NumberLocalizationUtil.formatSouthAsianNumber(p.amountNpr)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(text = "Plan: ${p.planName} • ${p.provider.displayName}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Ref: ${p.providerReference}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (p.status) {
                                        PaymentStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                                        PaymentStatus.REFUNDED -> MaterialTheme.colorScheme.errorContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ) {
                                    Text(
                                        text = p.status.displayNameEn,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                if (p.status == PaymentStatus.COMPLETED) {
                                    TextButton(
                                        onClick = { viewModel.refundPayment(p.id) },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Refund")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // Company Info Editor (CT-3 requirement: editable in admin settings)
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Company Contact Details (Live in Contact Us screen)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Company Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = editWhatsApp,
                        onValueChange = { editWhatsApp = it },
                        label = { Text("Company WhatsApp Number (e.g. +9779858712345)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Office Telephone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Office Physical Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = editHours,
                        onValueChange = { editHours = it },
                        label = { Text("Office Opening Hours") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Button(
                        onClick = {
                            val updated = companyInfo.copy(
                                email = editEmail,
                                whatsAppNumber = editWhatsApp,
                                phone = editPhone,
                                address = editAddress,
                                openingHours = editHours
                            )
                            viewModel.updateCompanyInfo(updated)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Company Details")
                    }
                }
            }
        } else {
            // Plans list
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(plans) { plan ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = plan.nameEn, fontWeight = FontWeight.Bold)
                                Text("NPR ${plan.priceNpr}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Text("Duration: ${plan.durationDays} Days • Quota: ${plan.maxActiveListings} Listings (${plan.maxPhotosPerListing} photos/listing)")
                        }
                    }
                }
            }
        }
    }
}
