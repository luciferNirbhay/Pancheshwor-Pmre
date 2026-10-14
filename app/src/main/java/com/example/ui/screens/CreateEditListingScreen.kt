package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.PropertyImageView
import com.example.util.AreaConverterUtil
import com.example.util.NumberLocalizationUtil
import com.example.util.StringsRepo
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditListingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val editingProp by viewModel.editingProperty.collectAsState()
    val lang = currentUser.language
    val isNe = lang == AppLanguage.NEPALI

    val initial = editingProp ?: Property(
        id = "prop_" + System.currentTimeMillis().toString().takeLast(6),
        title = "",
        type = PropertyType.LAND,
        priceNpr = 0L,
        areaM2 = 0.0,
        ward = 4,
        developerId = currentUser.id
    )

    var title by remember { mutableStateOf(initial.title) }
    var titleNe by remember { mutableStateOf(initial.titleNe) }
    var type by remember { mutableStateOf(initial.type) }
    var priceNprStr by remember { mutableStateOf(if (initial.priceNpr > 0) initial.priceNpr.toString() else "") }
    var areaM2Str by remember { mutableStateOf(if (initial.areaM2 > 0) initial.areaM2.toString() else "") }

    // Terai land fields for live 2-way entry
    val initialTerai = remember(initial.areaM2) { AreaConverterUtil.m2ToTerai(initial.areaM2) }
    var bighaStr by remember { mutableStateOf(if (initial.areaM2 > 0) initialTerai.bigha.toString() else "0") }
    var katthaStr by remember { mutableStateOf(if (initial.areaM2 > 0) initialTerai.kattha.toString() else "0") }
    var dhurStr by remember { mutableStateOf(if (initial.areaM2 > 0) String.format(java.util.Locale.US, "%.2f", initialTerai.dhur) else "0.0") }

    var ward by remember { mutableIntStateOf(initial.ward) }
    var tole by remember { mutableStateOf(initial.tole) }
    var roadAccess by remember { mutableStateOf(initial.roadAccess) }
    var facing by remember { mutableStateOf(initial.facing) }
    var kittaNumber by remember { mutableStateOf(initial.kittaNumber ?: "") }
    var description by remember { mutableStateOf(initial.description) }

    // Photos list
    var photos by remember {
        mutableStateOf(
            if (initial.photos.isNotEmpty()) initial.photos
            else listOf(
                PropertyPhoto("p1", "prop_terai_land_1791944158134", "Plot overview", isCover = true, order = 1),
                PropertyPhoto("p2", "prop_house_mahendra_1791944139592", "Access road & neighborhood", isCover = false, order = 2)
            )
        )
    }

    val availableSampleAssets = listOf(
        "prop_terai_land_1791944158134" to "Terai Land Plot",
        "prop_house_mahendra_1791944139592" to "Residential Villa",
        "prop_commercial_1791944180176" to "Commercial Building"
    )

    fun syncFromM2(newM2: String) {
        areaM2Str = newM2
        val m2 = newM2.toDoubleOrNull() ?: 0.0
        val t = AreaConverterUtil.m2ToTerai(m2)
        bighaStr = t.bigha.toString()
        katthaStr = t.kattha.toString()
        dhurStr = String.format(java.util.Locale.US, "%.2f", t.dhur)
    }

    fun syncFromTerai(bStr: String, kStr: String, dStr: String) {
        bighaStr = bStr
        katthaStr = kStr
        dhurStr = dStr
        val b = bStr.toDoubleOrNull() ?: 0.0
        val k = kStr.toDoubleOrNull() ?: 0.0
        val d = dStr.toDoubleOrNull() ?: 0.0
        val m2 = AreaConverterUtil.teraiToM2(b, k, d)
        areaM2Str = String.format(java.util.Locale.US, "%.2f", m2)
    }

    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 6.dp, color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val p = initial.copy(
                                title = title.ifBlank { "Draft Property" },
                                titleNe = titleNe,
                                type = type,
                                priceNpr = priceNprStr.toLongOrNull() ?: 0L,
                                areaM2 = areaM2Str.toDoubleOrNull() ?: 0.0,
                                ward = ward,
                                tole = tole,
                                roadAccess = roadAccess,
                                facing = facing,
                                kittaNumber = kittaNumber,
                                description = description,
                                photos = photos,
                                approved = false
                            )
                            viewModel.saveEditingProperty(p, isSubmitForApproval = false)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(StringsRepo.get("save_draft", lang))
                    }

                    Button(
                        onClick = {
                            if (currentUser.membershipStatus != "Active" && currentUser.role != UserRole.ADMIN) {
                                viewModel.showSnackbar("Active seller membership required to publish! Please buy a plan.")
                                viewModel.navigateTo(AppScreen.MEMBERSHIP_CHECKOUT)
                                return@Button
                            }
                            val p = initial.copy(
                                title = title.ifBlank { "Untitled Property in Bhimdatta" },
                                titleNe = titleNe,
                                type = type,
                                priceNpr = priceNprStr.toLongOrNull() ?: 1000000L,
                                areaM2 = areaM2Str.toDoubleOrNull() ?: 100.0,
                                ward = ward,
                                tole = tole,
                                roadAccess = roadAccess,
                                facing = facing,
                                kittaNumber = kittaNumber,
                                description = description,
                                photos = photos,
                                approved = currentUser.role == UserRole.ADMIN // auto-approve if admin
                            )
                            viewModel.saveEditingProperty(p, isSubmitForApproval = true)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_listing_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(StringsRepo.get("publish_listing", lang), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    text = if (initial.title.isBlank()) StringsRepo.get("create_listing", lang) else StringsRepo.get("edit_listing", lang),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bhimdatta Municipality (Mahendranagar), Kanchanpur",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Title Fields (English & Nepali)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title (English) *") },
                            placeholder = { Text("e.g. 1 Kattha Land Plot near Kaluwapur Road") },
                            modifier = Modifier.fillMaxWidth().testTag("listing_title_input")
                        )

                        OutlinedTextField(
                            value = titleNe,
                            onValueChange = { titleNe = it },
                            label = { Text("शीर्षक (नेपाली)") },
                            placeholder = { Text("जस्तै: कालुवापुर सडक नजिक १ कठ्ठा जग्गा") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Property Type
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Property Type *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(PropertyType.values().toList()) { t ->
                                FilterChip(
                                    selected = type == t,
                                    onClick = { type = t },
                                    label = { Text(if (isNe) t.displayNameNe else t.displayNameEn) }
                                )
                            }
                        }
                    }
                }
            }

            // Price in NPR
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Price in NPR (Whole Amount) *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = priceNprStr,
                            onValueChange = { priceNprStr = it },
                            label = { Text("Price in NPR") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("listing_price_input")
                        )
                        val num = priceNprStr.toLongOrNull() ?: 0L
                        if (num > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val formatted = NumberLocalizationUtil.formatPriceNpr(num, shortForm = true, useNepali = isNe)
                            Text(
                                text = "Preview: $formatted (${NumberLocalizationUtil.formatSouthAsianNumber(num)})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Dual Area Input: Enter in m² OR Bigha/Kattha/Dhur with live auto-sync! (DV-6)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Area Measurement (m² & Bigha/Kattha/Dhur live sync) *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "You can enter either square meters or local Terai units — both update automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = areaM2Str,
                            onValueChange = { syncFromM2(it) },
                            label = { Text("Area in Square Meters (m²)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().testTag("listing_area_m2_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Or enter in Terai Land Units:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = bighaStr,
                                onValueChange = { syncFromTerai(it, katthaStr, dhurStr) },
                                label = { Text("Bigha") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = katthaStr,
                                onValueChange = { syncFromTerai(bighaStr, it, dhurStr) },
                                label = { Text("Kattha") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = dhurStr,
                                onValueChange = { syncFromTerai(bighaStr, katthaStr, it) },
                                label = { Text("Dhur") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        val m2Val = areaM2Str.toDoubleOrNull() ?: 0.0
                        if (m2Val > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val tArea = AreaConverterUtil.m2ToTerai(m2Val)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Stored: ${String.format(java.util.Locale.US, "%.2f", m2Val)} m²  =  ${tArea.toFormattedString(isNe)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Location: Ward & Tole
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "Location Details (Kanchanpur)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        // Ward
                        Column {
                            Text(text = "Ward Number (Bhimdatta Municipality): Ward $ward", style = MaterialTheme.typography.labelMedium)
                            Slider(
                                value = ward.toFloat(),
                                onValueChange = { ward = it.toInt() },
                                valueRange = 1f..19f,
                                steps = 17
                            )
                        }

                        OutlinedTextField(
                            value = tole,
                            onValueChange = { tole = it },
                            label = { Text("Tole / Locality *") },
                            placeholder = { Text("e.g. Suda Chowk, Kaluwapur Road, Traffic Chowk") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = roadAccess,
                                onValueChange = { roadAccess = it },
                                label = { Text("Road Access") },
                                placeholder = { Text("20 ft Pitched") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = facing,
                                onValueChange = { facing = it },
                                label = { Text("Facing") },
                                placeholder = { Text("East / West") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = kittaNumber,
                            onValueChange = { kittaNumber = it },
                            label = { Text("Kitta Number (Lalpurja Certificate)") },
                            placeholder = { Text("e.g. 512/04") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Photo Upload & Reorder Section (DV-2 & DV-4)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Photos (${photos.size}/20) *",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "★ = Cover Photo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Photos horizontal strip
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(photos) { index, photo ->
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (photo.isCover) 2.dp else 1.dp,
                                            color = if (photo.isCover) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    PropertyImageView(
                                        photoUrl = photo.url,
                                        contentDescription = photo.caption,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Cover badge / toggle
                                    IconButton(
                                        onClick = {
                                            photos = photos.mapIndexed { idx, p ->
                                                p.copy(isCover = idx == index)
                                            }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .size(28.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(bottomEnd = 8.dp))
                                    ) {
                                        Icon(
                                            imageVector = if (photo.isCover) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "Set Cover",
                                            tint = if (photo.isCover) Color.Yellow else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Delete photo button
                                    if (photos.size > 1) {
                                        IconButton(
                                            onClick = {
                                                photos = photos.filterIndexed { idx, _ -> idx != index }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(28.dp)
                                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(bottomStart = 8.dp))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Delete",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Add Photo Action
                            item {
                                Surface(
                                    onClick = {
                                        val sample = availableSampleAssets[(photos.size) % availableSampleAssets.size]
                                        val newP = PropertyPhoto(
                                            id = "p_" + System.currentTimeMillis().toString().takeLast(4),
                                            url = sample.first,
                                            caption = sample.second,
                                            isCover = false,
                                            order = photos.size + 1
                                        )
                                        photos = photos + newP
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(100.dp)
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("+ Add Photo", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Description
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Description", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("Details regarding surroundings, boundary walls, water supply, electricity, title deeds...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4
                        )
                    }
                }
            }
        }
    }
}
