package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import com.example.model.Property
import com.example.ui.components.PropertyImageView
import com.example.util.AreaConverterUtil
import com.example.util.NumberLocalizationUtil
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun AdminApprovalsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val properties by viewModel.filteredProperties.collectAsState()
    val lang = currentUser.language
    val isNe = lang == com.example.model.AppLanguage.NEPALI

    var rejectTargetProperty by remember { mutableStateOf<Property?>(null) }
    var rejectReason by remember { mutableStateOf("Please provide clearer photos and verify Kitta Number.") }

    val pendingListings = remember(properties) {
        properties.filter { !it.approved }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("admin_approvals_screen"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin Badge Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Admin Review & Approvals Queue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pancheshwor Multipurpose and Real Estate Pvt. Ltd.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Pending Listings (${pendingListings.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (pendingListings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Approvals Queue is Clear!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All submitted properties from developers are reviewed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(pendingListings, key = { it.id }) { prop ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            PropertyImageView(
                                photoUrl = prop.photos.firstOrNull()?.url ?: "prop_house_mahendra_1791944139592",
                                contentDescription = prop.title,
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prop.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                val priceStr = NumberLocalizationUtil.formatPriceNpr(prop.priceNpr, true, isNe)
                                Text(
                                    text = priceStr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val teraiStr = AreaConverterUtil.m2ToTerai(prop.areaM2).toFormattedString(isNe)
                                Text(
                                    text = "$teraiStr (${String.format(java.util.Locale.US, "%.1f", prop.areaM2)} m²)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Lister: ${prop.developerName} (${prop.developerPhone})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        if (prop.kittaNumber != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Kitta No: ${prop.kittaNumber} • Ward: ${prop.ward} (${prop.tole})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { rejectTargetProperty = prop },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Send Back")
                            }

                            Button(
                                onClick = { viewModel.approveProperty(prop.id) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Approve")
                            }
                        }
                    }
                }
            }
        }
    }

    // Rejection / Send back dialog
    if (rejectTargetProperty != null) {
        val p = rejectTargetProperty!!
        AlertDialog(
            onDismissRequest = { rejectTargetProperty = null },
            title = { Text("Send Back Listing") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Provide feedback to developer (${p.developerName}) explaining what needs correction:")
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectProperty(p.id, rejectReason)
                        rejectTargetProperty = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Send Feedback")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectTargetProperty = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
