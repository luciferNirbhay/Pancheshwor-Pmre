package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InquiryType
import com.example.model.Property
import com.example.model.PropertyStatus
import com.example.model.UserRole
import com.example.ui.components.PropertyImageView
import com.example.ui.components.launchPhoneCall
import com.example.ui.components.launchWhatsApp
import com.example.util.AreaConverterUtil
import com.example.util.NumberLocalizationUtil
import com.example.util.StringsRepo
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val property by viewModel.selectedProperty.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val lang = currentUser.language
    val isNe = lang == com.example.model.AppLanguage.NEPALI
    val useNepaliDigits = currentUser.nepaliDigits

    if (property == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Property not found")
        }
        return
    }

    val p = property!!
    val isFav = favorites.contains(p.id)
    val teraiArea = remember(p.areaM2) { AreaConverterUtil.m2ToTerai(p.areaM2) }
    val pricePerKattha = remember(p.priceNpr, p.areaM2) {
        AreaConverterUtil.calculatePricePerKattha(p.priceNpr, p.areaM2)
    }
    val pricePerM2 = remember(p.priceNpr, p.areaM2) {
        AreaConverterUtil.calculatePricePerM2(p.priceNpr, p.areaM2)
    }

    var activePhotoIndex by remember { mutableStateOf(0) }
    var showInquiryDialog by remember { mutableStateOf(false) }
    var showSiteVisitDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Call Button
                    Button(
                        onClick = { launchPhoneCall(context, p.developerPhone) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_call_button")
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(StringsRepo.get("call_seller", lang), fontWeight = FontWeight.Bold)
                    }

                    // WhatsApp Button
                    Button(
                        onClick = {
                            val msg = "Namaste! I am interested in property '${p.title}' (ID: ${p.id}) in Mahendranagar listed on Pancheshwor Real Estate. Please share more details."
                            launchWhatsApp(context, p.developerPhone, msg)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("detail_whatsapp_button")
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(StringsRepo.get("whatsapp_chat", lang), color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Inquiry / Visit Menu
                    IconButton(
                        onClick = { showInquiryDialog = true },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .testTag("detail_inquiry_button")
                    ) {
                        Icon(Icons.Default.MailOutline, contentDescription = "Inquire", tint = MaterialTheme.colorScheme.primary)
                    }

                    IconButton(
                        onClick = { showSiteVisitDialog = true },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .testTag("detail_visit_button")
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Site Visit", tint = MaterialTheme.colorScheme.secondary)
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
        ) {
            // Photo Gallery Hero Box
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.Black)
                ) {
                    val currentPhoto = p.photos.getOrNull(activePhotoIndex) ?: p.photos.firstOrNull()
                    PropertyImageView(
                        photoUrl = currentPhoto?.url ?: "prop_house_mahendra_1791944139592",
                        contentDescription = currentPhoto?.caption ?: p.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                viewModel.setSelectedPhotoIndex(activePhotoIndex)
                                viewModel.navigateTo(AppScreen.PHOTO_VIEWER)
                            }
                    )

                    // Counter Pill (e.g., "1 of 3")
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        val counterText = "${activePhotoIndex + 1} / ${p.photos.size.coerceAtLeast(1)}"
                        Text(
                            text = if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(counterText) else counterText,
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Tap to View Fullscreen Banner Prompt
                    Surface(
                        onClick = {
                            viewModel.setSelectedPhotoIndex(activePhotoIndex)
                            viewModel.navigateTo(AppScreen.PHOTO_VIEWER)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomStart)
                            .testTag("tap_photo_fullscreen_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isNe) "पूरा फोटो र मूल्य हेर्न थिच्नुहोस्" else "Tap photo for full price & area",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Favorite and Share overlay
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomEnd),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleFavorite(p.id) },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) Color.Red else Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Check out '${p.title}' in Bhimdatta on Pancheshwor Real Estate: NPR ${NumberLocalizationUtil.formatSouthAsianNumber(p.priceNpr)}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Property"))
                            },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }
                    }
                }
            }

            // Thumbnail strip
            if (p.photos.size > 1) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        itemsIndexed(p.photos) { index, photo ->
                            val isSelected = index == activePhotoIndex
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { activePhotoIndex = index }
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .padding(if (isSelected) 2.dp else 0.dp)
                            ) {
                                PropertyImageView(
                                    photoUrl = photo.url,
                                    contentDescription = photo.caption,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(6.dp))
                                )
                            }
                        }
                    }
                }
            }

            // Price & Primary Area Card
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (isNe) p.type.displayNameNe else p.type.displayNameEn,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            if (p.lalpurjaVerified) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = StringsRepo.get("lalpurja_verified", lang),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Title
                        Text(
                            text = if (isNe && p.titleNe.isNotBlank()) p.titleNe else p.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Tole and Ward
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val locationFull = "${if (isNe) p.toleNe else p.tole}, ${StringsRepo.get("ward", lang)} ${p.ward}, Bhimdatta, Kanchanpur"
                            Text(
                                text = if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(locationFull) else locationFull,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Total Price Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = StringsRepo.get("price", lang),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val priceFull = NumberLocalizationUtil.formatPriceNpr(
                                    amount = p.priceNpr,
                                    shortForm = false,
                                    useNepali = isNe,
                                    useNepaliDigits = useNepaliDigits || isNe
                                )
                                val priceShort = NumberLocalizationUtil.formatPriceNpr(
                                    amount = p.priceNpr,
                                    shortForm = true,
                                    useNepali = isNe,
                                    useNepaliDigits = useNepaliDigits || isNe
                                )
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = priceFull,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "($priceShort)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                                // Price per Kattha & Price per m²
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = StringsRepo.get("price_per_kattha", lang),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val perKatthaFormatted = NumberLocalizationUtil.formatPriceNpr(
                                            pricePerKattha,
                                            shortForm = true,
                                            useNepali = isNe,
                                            useNepaliDigits = useNepaliDigits || isNe
                                        )
                                        Text(
                                            text = perKatthaFormatted,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = StringsRepo.get("price_per_m2", lang),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val perM2Formatted = NumberLocalizationUtil.formatPriceNpr(
                                            pricePerM2,
                                            shortForm = false,
                                            useNepali = isNe,
                                            useNepaliDigits = useNepaliDigits || isNe
                                        )
                                        Text(
                                            text = "$perM2Formatted / m²",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Land Area Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = StringsRepo.get("mixed_terai_area", lang),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )

                                    // Quick button to open area converter
                                    TextButton(
                                        onClick = {
                                            viewModel.updateConverterFromM2(p.areaM2.toString())
                                            viewModel.navigateTo(AppScreen.AREA_CONVERTER)
                                        },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isNe) "क्यालकुलेटरमा हेर्नुहोस्" else "Open in Converter",
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Text(
                                    text = teraiArea.toFormattedString(isNe),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val m2Str = String.format(java.util.Locale.US, "%.2f m²", p.areaM2)
                                    val sqftStr = String.format(java.util.Locale.US, "%.1f sq ft", teraiArea.totalSqFt)
                                    Text(
                                        text = if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(m2Str) else m2Str,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(sqftStr) else sqftStr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Key Specs Grid
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (isNe) "विशेष विवरण" else "Property Specifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Spec items
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SpecItem(
                                icon = Icons.Default.AddRoad,
                                title = StringsRepo.get("road_access", lang),
                                value = if (isNe) p.roadAccessNe else p.roadAccess,
                                modifier = Modifier.weight(1f)
                            )
                            SpecItem(
                                icon = Icons.Default.Explore,
                                title = StringsRepo.get("facing", lang),
                                value = if (isNe) p.facingNe else p.facing,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            SpecItem(
                                icon = Icons.Default.Pin,
                                title = StringsRepo.get("kitta_no", lang),
                                value = p.kittaNumber ?: "Verified",
                                modifier = Modifier.weight(1f)
                            )
                            SpecItem(
                                icon = Icons.Default.WaterDrop,
                                title = if (isNe) "पानीको स्रोत" else "Water Source",
                                value = if (isNe) (p.waterSourceNe ?: "सुविधा छ") else (p.waterSource ?: "Available"),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (p.bedrooms != null || p.floors != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                if (p.floors != null) {
                                    SpecItem(
                                        icon = Icons.Default.Stairs,
                                        title = StringsRepo.get("floors", lang),
                                        value = "${p.floors} Floors",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (p.bedrooms != null) {
                                    SpecItem(
                                        icon = Icons.Default.Bed,
                                        title = StringsRepo.get("bedrooms", lang),
                                        value = "${p.bedrooms} Beds / ${p.bathrooms ?: 1} Baths",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Description
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (isNe) "विवरण" else "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isNe && p.descriptionNe.isNotBlank()) p.descriptionNe else p.description,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Developer / Lister Details
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (isNe) "सम्पर्क तथा बिक्रेता" else "Listing Lister",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = p.developerName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = p.developerPhone,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Inquiry Dialog
    if (showInquiryDialog) {
        var nameInput by remember { mutableStateOf(currentUser.name) }
        var phoneInput by remember { mutableStateOf(currentUser.phone) }
        var msgInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showInquiryDialog = false },
            title = { Text(StringsRepo.get("send_message", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(StringsRepo.get("name", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text(StringsRepo.get("phone_number", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = msgInput,
                        onValueChange = { msgInput = it },
                        label = { Text(StringsRepo.get("message", lang)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitInquiry(
                            propertyId = p.id,
                            propertyTitle = p.title,
                            name = nameInput,
                            phone = phoneInput,
                            message = msgInput.ifBlank { "Interested in this property." },
                            type = InquiryType.GENERAL
                        )
                        showInquiryDialog = false
                    }
                ) {
                    Text(StringsRepo.get("send_message", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showInquiryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Site Visit Request Dialog
    if (showSiteVisitDialog) {
        var nameInput by remember { mutableStateOf(currentUser.name) }
        var phoneInput by remember { mutableStateOf(currentUser.phone) }
        var dateInput by remember { mutableStateOf("Tomorrow (10:00 AM)") }
        var msgInput by remember { mutableStateOf("Please arrange site visit for this land/house.") }

        AlertDialog(
            onDismissRequest = { showSiteVisitDialog = false },
            title = { Text(StringsRepo.get("request_site_visit", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(StringsRepo.get("name", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text(StringsRepo.get("phone_number", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        label = { Text(StringsRepo.get("preferred_date", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = msgInput,
                        onValueChange = { msgInput = it },
                        label = { Text(StringsRepo.get("message", lang)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitInquiry(
                            propertyId = p.id,
                            propertyTitle = p.title,
                            name = nameInput,
                            phone = phoneInput,
                            message = msgInput,
                            type = InquiryType.SITE_VISIT,
                            preferredDate = dateInput,
                            preferredTime = "Morning"
                        )
                        showSiteVisitDialog = false
                    }
                ) {
                    Text(StringsRepo.get("request_site_visit", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSiteVisitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SpecItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
    }
}
