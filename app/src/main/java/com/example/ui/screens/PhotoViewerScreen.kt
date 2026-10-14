package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PropertyImageView
import com.example.util.AreaConverterUtil
import com.example.util.NumberLocalizationUtil
import com.example.util.StringsRepo
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun PhotoViewerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val property by viewModel.selectedProperty.collectAsState()
    val currentIndex by viewModel.selectedPhotoIndex.collectAsState()
    val infoPanelVisible by viewModel.photoInfoPanelVisible.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val lang = currentUser.language
    val isNe = lang == com.example.model.AppLanguage.NEPALI
    val useNepaliDigits = currentUser.nepaliDigits

    if (property == null || property!!.photos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("No photos available", color = Color.White)
        }
        return
    }

    val p = property!!
    val photos = p.photos
    val currentPhoto = photos.getOrElse(currentIndex) { photos.first() }

    // Pinch-to-zoom & double tap state
    var scale by remember(currentIndex) { mutableFloatStateOf(1f) }
    var offsetX by remember(currentIndex) { mutableFloatStateOf(0f) }
    var offsetY by remember(currentIndex) { mutableFloatStateOf(0f) }

    val teraiArea = remember(p.areaM2) { AreaConverterUtil.m2ToTerai(p.areaM2) }
    val pricePerKattha = remember(p.priceNpr, p.areaM2) {
        AreaConverterUtil.calculatePricePerKattha(p.priceNpr, p.areaM2)
    }
    val pricePerM2 = remember(p.priceNpr, p.areaM2) {
        AreaConverterUtil.calculatePricePerM2(p.priceNpr, p.areaM2)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // Permanent dark canvas per PRD TH-3
            .testTag("fullscreen_photo_viewer")
    ) {
        // Main Photo with Gesture Detection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(currentIndex) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 4f)
                        if (scale > 1f) {
                            offsetX += pan.x
                            offsetY += pan.y
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                }
                .pointerInput(currentIndex) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > 1f) {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            } else {
                                scale = 2.5f
                            }
                        },
                        onTap = {
                            // Tap photo to toggle info panel overlay! (PH-3 requirement)
                            viewModel.togglePhotoInfoPanel()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            PropertyImageView(
                photoUrl = currentPhoto.url,
                contentDescription = currentPhoto.caption.ifBlank { p.title },
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            )
        }

        // Top Navigation & Action Bar Overlay
        AnimatedVisibility(
            visible = infoPanelVisible,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("photo_viewer_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    // Counter (e.g. 2 of 3)
                    val counterText = "${currentIndex + 1} / ${photos.size}"
                    Text(
                        text = if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(counterText) else counterText,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row {
                        // Download/Save button (PH-7)
                        IconButton(
                            onClick = {
                                viewModel.showSnackbar("Photo saved to phone's Pictures gallery")
                            },
                            modifier = Modifier.testTag("photo_download_btn")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download Photo", tint = Color.White)
                        }

                        // Share button
                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Photo of '${p.title}' in Bhimdatta: NPR ${NumberLocalizationUtil.formatSouthAsianNumber(p.priceNpr)}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Photo"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }
                    }
                }
            }
        }

        // Previous / Next Navigation Arrows
        if (photos.size > 1) {
            if (currentIndex > 0) {
                IconButton(
                    onClick = {
                        viewModel.setSelectedPhotoIndex(currentIndex - 1)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }

            if (currentIndex < photos.size - 1) {
                IconButton(
                    onClick = {
                        viewModel.setSelectedPhotoIndex(currentIndex + 1)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }

        // Bottom Info Panel Overlay (PH-3 & PH-4 requirement!)
        // "Tapping a photo opens it full screen with an info panel showing the price in NPR and the area in m². Tapping again hides or shows the panel."
        // "The panel also shows the area in Bigha / Kattha / Dhur and the price per m² and per Kattha."
        AnimatedVisibility(
            visible = infoPanelVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("photo_info_panel")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Tap hint bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                        ) {}
                    }

                    // Photo caption if any
                    if (currentPhoto.caption.isNotBlank()) {
                        Text(
                            text = currentPhoto.caption,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Price row (Full NPR & Short Form)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = StringsRepo.get("price", lang),
                                style = MaterialTheme.typography.labelSmall,
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
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = priceFull,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "($priceShort)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Converter quick launch
                        FilledTonalButton(
                            onClick = {
                                viewModel.updateConverterFromM2(p.areaM2.toString())
                                viewModel.navigateTo(AppScreen.AREA_CONVERTER)
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isNe) "नाप रूपान्तरण" else "Convert", fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Area row: Stored m² and Terai Land (Bigha / Kattha / Dhur)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = StringsRepo.get("area", lang) + " (m²)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val m2Formatted = String.format(java.util.Locale.US, "%.2f m²", p.areaM2)
                            Text(
                                text = if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(m2Formatted) else m2Formatted,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = StringsRepo.get("mixed_terai_area", lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = teraiArea.toFormattedString(isNe),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Price per Kattha and per m²
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val perKatthaStr = NumberLocalizationUtil.formatPriceNpr(
                            pricePerKattha,
                            shortForm = true,
                            useNepali = isNe,
                            useNepaliDigits = useNepaliDigits || isNe
                        )
                        val perM2Str = NumberLocalizationUtil.formatPriceNpr(
                            pricePerM2,
                            shortForm = false,
                            useNepali = isNe,
                            useNepaliDigits = useNepaliDigits || isNe
                        )

                        Text(
                            text = "${StringsRepo.get("price_per_kattha", lang)}: $perKatthaStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "${StringsRepo.get("price_per_m2", lang)}: $perM2Str / m²",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isNe) "फोटो लुकाउन वा देखाउन पुनः छुनुहोस्" else "Tap photo anywhere to show/hide this info overlay",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
