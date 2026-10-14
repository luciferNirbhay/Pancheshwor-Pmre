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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PropertyStatus
import com.example.ui.components.PropertyImageView
import com.example.ui.components.launchPhoneCall
import com.example.util.AreaConverterUtil
import com.example.util.NumberLocalizationUtil
import com.example.util.StringsRepo
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun MyListingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val properties by viewModel.filteredProperties.collectAsState()
    val inquiries by viewModel.inquiries.collectAsState()
    val lang = currentUser.language
    val isNe = lang == com.example.model.AppLanguage.NEPALI
    val useNepaliDigits = currentUser.nepaliDigits

    var selectedTab by remember { mutableIntStateOf(0) }

    val myListings = remember(properties, currentUser) {
        properties.filter { it.developerId == currentUser.id }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.startCreateListing() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(StringsRepo.get("create_listing", lang)) },
                modifier = Modifier.testTag("fab_create_listing")
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("my_listings_screen")
        ) {
            // Tabs: My Properties & Received Inquiries
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("${StringsRepo.get("my_listings", lang)} (${myListings.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("${StringsRepo.get("inquire_now", lang)} (${inquiries.size})") }
                )
            }

            if (selectedTab == 0) {
                // Properties Tab
                if (myListings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (isNe) "तपाईंले कुनै सम्पत्ति थप्नुभएको छैन" else "You haven't listed any properties yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.startCreateListing() }) {
                                Text(StringsRepo.get("create_listing", lang))
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(myListings, key = { it.id }) { prop ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        PropertyImageView(
                                            photoUrl = prop.photos.firstOrNull()?.url ?: "prop_house_mahendra_1791944139592",
                                            contentDescription = prop.title,
                                            modifier = Modifier
                                                .size(72.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = prop.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            val priceStr = NumberLocalizationUtil.formatPriceNpr(
                                                prop.priceNpr,
                                                shortForm = true,
                                                useNepali = isNe,
                                                useNepaliDigits = useNepaliDigits || isNe
                                            )
                                            Text(
                                                text = priceStr,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            val teraiStr = AreaConverterUtil.m2ToTerai(prop.areaM2).toFormattedString(isNe)
                                            Text(
                                                text = teraiStr,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Approval and Status Row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (prop.approved) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                                        ) {
                                            Text(
                                                text = if (prop.approved) "Live / Approved" else "Pending Admin Approval",
                                                color = if (prop.approved) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }

                                        // Status Switcher: Available, Booked, Sold
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            PropertyStatus.values().forEach { st ->
                                                val isCur = prop.status == st
                                                FilterChip(
                                                    selected = isCur,
                                                    onClick = { viewModel.markPropertyStatus(prop.id, st) },
                                                    label = { Text(st.displayNameEn, fontSize = 11.sp) }
                                                )
                                            }
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                                    // Actions: Edit, Delete, View Detail
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = {
                                            viewModel.selectProperty(prop)
                                            viewModel.navigateTo(AppScreen.PROPERTY_DETAIL)
                                        }) {
                                            Text("Preview")
                                        }
                                        TextButton(onClick = { viewModel.startEditListing(prop) }) {
                                            Text("Edit")
                                        }
                                        IconButton(onClick = { viewModel.deleteProperty(prop.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Inquiries Tab
                if (inquiries.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if (isNe) "कुनै सोधपुछ प्राप्त भएको छैन" else "No inquiries received yet")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(inquiries) { inq ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = inq.buyerName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = inq.inquiryType.titleEn,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Re: ${inq.propertyTitle}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "\"${inq.message}\"",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    if (inq.preferredDate != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Requested Visit: ${inq.preferredDate} (${inq.preferredTime ?: "Flexible"})",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = inq.buyerPhone,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        val ctx = LocalContext.current
                                        Button(
                                            onClick = { launchPhoneCall(ctx, inq.buyerPhone) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("Call Buyer", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
