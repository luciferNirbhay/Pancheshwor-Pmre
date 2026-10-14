package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.ThemeModePreference
import com.example.model.UserRole
import com.example.util.StringsRepo
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val lang = currentUser.language
    val isNe = lang == AppLanguage.NEPALI

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Theme Settings (TH-1)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = StringsRepo.get("theme", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeChip(
                            title = StringsRepo.get("system_theme", lang),
                            isSelected = currentUser.themeMode == ThemeModePreference.SYSTEM,
                            onClick = { viewModel.updateTheme(ThemeModePreference.SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeChip(
                            title = StringsRepo.get("light_theme", lang),
                            isSelected = currentUser.themeMode == ThemeModePreference.LIGHT,
                            onClick = { viewModel.updateTheme(ThemeModePreference.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeChip(
                            title = StringsRepo.get("dark_theme", lang),
                            isSelected = currentUser.themeMode == ThemeModePreference.DARK,
                            onClick = { viewModel.updateTheme(ThemeModePreference.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Language & Numerals Settings (LN-1)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = StringsRepo.get("language", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageOptionTile(
                            title = "English",
                            subtitle = "English UI",
                            isSelected = currentUser.language == AppLanguage.ENGLISH,
                            onClick = { viewModel.updateLanguage(AppLanguage.ENGLISH) },
                            modifier = Modifier.weight(1f)
                        )
                        LanguageOptionTile(
                            title = "नेपाली",
                            subtitle = "नेपाली इन्टरफेस",
                            isSelected = currentUser.language == AppLanguage.NEPALI,
                            onClick = { viewModel.updateLanguage(AppLanguage.NEPALI) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Nepali Digits Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StringsRepo.get("nepali_digits", lang),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isNe) "मूल्य तथा क्षेत्रफलमा १, २, ३ को साटो ०-९ अङ्क देखाउने" else "Show digits as ०, १, २, ३ in prices and areas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = currentUser.nepaliDigits,
                            onCheckedChange = { viewModel.updateNepaliDigits(it) }
                        )
                    }
                }
            }
        }

        // Connection & Data Saver
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StringsRepo.get("data_saver", lang),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isNe) "कमजोर इन्टरनेट वा ३जी नेटवर्कका लागि तस्बिरको गुणस्तर अनुकूलन गर्ने" else "Optimizes photo loading for 3G and weak cellular networks in Kanchanpur",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = currentUser.dataSaverMode,
                            onCheckedChange = { viewModel.updateDataSaver(it) }
                        )
                    }
                }
            }
        }

        // Quick Links
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text(StringsRepo.get("contact_us", lang)) },
                        supportingContent = { Text("Pancheshwor Multipurpose and Real Estate Pvt. Ltd.") },
                        leadingContent = { Icon(Icons.Default.SupportAgent, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier.testTag("settings_contact_btn")
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text(StringsRepo.get("seller_membership", lang)) },
                        supportingContent = { Text("View seller plans & payment options (eSewa / Khalti)") },
                        leadingContent = { Icon(Icons.Default.CardMembership, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("About App & Malpot Units") },
                        supportingContent = { Text("Version 1.0 (Bhimdatta/Mahendranagar Edition)") },
                        leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 12.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LanguageOptionTile(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
