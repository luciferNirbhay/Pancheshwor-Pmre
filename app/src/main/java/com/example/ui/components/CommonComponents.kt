package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.*
import com.example.util.AreaConverterUtil
import com.example.util.NumberLocalizationUtil
import com.example.util.StringsRepo

@Composable
fun PropertyImageView(
    photoUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val resId = remember(photoUrl) {
        val cleanName = photoUrl.substringBeforeLast(".")
        context.resources.getIdentifier(cleanName, "drawable", context.packageName)
    }

    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        // Fallback to Coil or placeholder icon
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PancheshworTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    currentRole: UserRole,
    onRoleClick: () -> Unit,
    currentTheme: ThemeModePreference,
    onToggleTheme: () -> Unit,
    currentLang: AppLanguage,
    onToggleLang: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Bhimdatta (Mahendranagar)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("nav_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            } else {
                IconButton(onClick = onRoleClick) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = "App Logo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        actions = {
            // Role badge button
            AssistChip(
                onClick = onRoleClick,
                label = {
                    Text(
                        text = when (currentRole) {
                            UserRole.GUEST -> "Guest"
                            UserRole.BUYER -> "Buyer"
                            UserRole.DEVELOPER -> "Developer"
                            UserRole.ADMIN -> "Admin"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = when (currentRole) {
                            UserRole.GUEST -> Icons.Outlined.Person
                            UserRole.BUYER -> Icons.Filled.ShoppingBag
                            UserRole.DEVELOPER -> Icons.Filled.BusinessCenter
                            UserRole.ADMIN -> Icons.Filled.AdminPanelSettings
                        },
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                modifier = Modifier
                    .padding(end = 4.dp)
                    .testTag("role_switcher_chip")
            )

            // Theme toggle button
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier.testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = when (currentTheme) {
                        ThemeModePreference.DARK -> Icons.Default.DarkMode
                        ThemeModePreference.LIGHT -> Icons.Default.LightMode
                        ThemeModePreference.SYSTEM -> Icons.Default.BrightnessAuto
                    },
                    contentDescription = "Toggle Theme"
                )
            }

            // Language quick toggle
            TextButton(
                onClick = onToggleLang,
                modifier = Modifier.testTag("lang_toggle_button")
            ) {
                Text(
                    text = if (currentLang == AppLanguage.ENGLISH) "ने" else "EN",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}

@Composable
fun PropertyCard(
    property: Property,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    language: AppLanguage,
    useNepaliDigits: Boolean,
    modifier: Modifier = Modifier
) {
    val isNe = language == AppLanguage.NEPALI
    val coverPhoto = property.photos.firstOrNull { it.isCover } ?: property.photos.firstOrNull()
    val teraiArea = remember(property.areaM2) {
        AreaConverterUtil.m2ToTerai(property.areaM2)
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("property_card_${property.id}")
    ) {
        Column {
            // Photo with tags
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                PropertyImageView(
                    photoUrl = coverPhoto?.url ?: "prop_house_mahendra_1791944139592",
                    contentDescription = property.title,
                    modifier = Modifier.fillMaxSize()
                )

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (property.status) {
                        PropertyStatus.AVAILABLE -> MaterialTheme.colorScheme.primary
                        PropertyStatus.BOOKED -> MaterialTheme.colorScheme.secondary
                        PropertyStatus.SOLD -> MaterialTheme.colorScheme.outline
                    },
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = if (isNe) property.status.displayNameNe else property.status.displayNameEn,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Favorite button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .size(36.dp)
                        .testTag("fav_btn_${property.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Photos count pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val countStr = "${property.photos.size}"
                        Text(
                            text = if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(countStr) else countStr,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Tap for Price & Area overlay hint
                Surface(
                    shape = RoundedCornerShape(topStart = 8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 12.dp, bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isNe) "फोटो छोएर मूल्य हेर्नुहोस्" else "Tap photo for info",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Info Section
            Column(modifier = Modifier.padding(16.dp)) {
                // Type & Ward badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (if (isNe) property.type.displayNameNe else property.type.displayNameEn).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val wardText = "${StringsRepo.get("ward", language)} ${property.ward}"
                    Text(
                        text = if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(wardText) else wardText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = if (isNe && property.titleNe.isNotBlank()) property.titleNe else property.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val priceStr = NumberLocalizationUtil.formatPriceNpr(
                        amount = property.priceNpr,
                        shortForm = true,
                        useNepali = isNe,
                        useNepaliDigits = useNepaliDigits || isNe
                    )
                    Text(
                        text = priceStr,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Price per Kattha
                    val pricePerKattha = AreaConverterUtil.calculatePricePerKattha(property.priceNpr, property.areaM2)
                    if (pricePerKattha > 0) {
                        val perKatthaStr = NumberLocalizationUtil.formatPriceNpr(
                            amount = pricePerKattha,
                            shortForm = true,
                            useNepali = isNe,
                            useNepaliDigits = useNepaliDigits || isNe
                        )
                        Text(
                            text = "($perKatthaStr / ${if (isNe) "कठ्ठा" else "Kattha"})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Area Pill (m² and Terai Land)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Terai land
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SquareFoot,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = teraiArea.toFormattedString(isNe),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // m²
                        val m2Formatted = String.format(java.util.Locale.US, "%.1f m²", property.areaM2)
                        Text(
                            text = if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(m2Formatted) else m2Formatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Verification & Location row
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isNe) property.toleNe else property.tole,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (property.lalpurjaVerified) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = StringsRepo.get("lalpurja_verified", language),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSwitcherBottomSheet(
    currentRole: UserRole,
    onSelectRole: (UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Switch User Role for Testing",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Experience the app from any perspective as specified in the PRD:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            UserRole.values().forEach { role ->
                val isSelected = role == currentRole
                Surface(
                    onClick = {
                        onSelectRole(role)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = when (role) {
                                UserRole.GUEST -> Icons.Outlined.Person
                                UserRole.BUYER -> Icons.Filled.ShoppingBag
                                UserRole.DEVELOPER -> Icons.Filled.BusinessCenter
                                UserRole.ADMIN -> Icons.Filled.AdminPanelSettings
                            },
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = role.titleEn,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (role) {
                                    UserRole.GUEST -> "Browse listings, open photo galleries, convert land units"
                                    UserRole.BUYER -> "Save favorites, send inquiries, book site visits"
                                    UserRole.DEVELOPER -> "Upload properties, manage listings, view inquiries"
                                    UserRole.ADMIN -> "Approve listings, manage payments and company info"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun launchPhoneCall(context: Context, phoneNumber: String) {
    try {
        val clean = phoneNumber.replace(" ", "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
        context.startActivity(intent)
    } catch (_: Exception) {}
}

fun launchWhatsApp(context: Context, whatsAppNumber: String, message: String) {
    try {
        val cleanNumber = whatsAppNumber.replace("+", "").replace(" ", "").replace("-", "")
        val encodedMessage = Uri.encode(message)
        val url = "https://wa.me/$cleanNumber?text=$encodedMessage"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {}
}

fun launchEmail(context: Context, email: String, subject: String, body: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
