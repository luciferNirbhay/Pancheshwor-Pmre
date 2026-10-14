package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AreaConverterUtil
import com.example.util.NumberLocalizationUtil
import com.example.util.StringsRepo
import com.example.viewmodel.MainViewModel

@Composable
fun AreaConverterScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val lang = currentUser.language
    val isNe = lang == com.example.model.AppLanguage.NEPALI
    val useNepaliDigits = currentUser.nepaliDigits

    val m2Input by viewModel.converterM2Input.collectAsState()
    val bighaInput by viewModel.converterBighaInput.collectAsState()
    val katthaInput by viewModel.converterKatthaInput.collectAsState()
    val dhurInput by viewModel.converterDhurInput.collectAsState()
    val showHillUnits by viewModel.converterShowHillUnits.collectAsState()
    val teraiResult by viewModel.teraiResult.collectAsState()
    val hillResult by viewModel.hillResult.collectAsState()

    val testPresets = listOf(
        "1 Dhur" to 16.931579,
        "1 Kattha" to 338.63158,
        "1 Bigha" to 6772.6316,
        "5 Kattha 10 Dhur" to 1862.47369,
        "1,000 m²" to 1000.0,
        "3 Bigha 4 Kattha 5 Dhur" to 21757.079,
        "6,772.63 m²" to 6772.63
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("area_converter_screen"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.SquareFoot, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = StringsRepo.get("convert_prompt", lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "1 Bigha = 20 Kattha = 400 Dhur = 6,772.63 m²",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Quick Test Presets (Verifies PRD Test Cases)
        item {
            Column {
                Text(
                    text = if (isNe) "परीक्षण एकाइहरू (द्रुत छनौट):" else "Test Presets (Official Land Factors):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(testPresets) { (label, value) ->
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.setConverterPreset(value) },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Two-way Input Card: Square Meters (m²)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = StringsRepo.get("unit_m2", lang),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = m2Input,
                        onValueChange = { viewModel.updateConverterFromM2(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("converter_m2_input"),
                        trailingIcon = {
                            Text(
                                text = "m²",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                    )
                }
            }
        }

        // Two-way Input Card: Terai Units (Bigha, Kattha, Dhur)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isNe) "तराई जग्गा एकाइ (बिघा, कठ्ठा, धुर)" else "Terai Units (Bigha, Kattha, Dhur)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = bighaInput,
                            onValueChange = { viewModel.updateConverterFromTerai(it, katthaInput, dhurInput) },
                            label = { Text(if (isNe) "बिघा" else "Bigha") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("converter_bigha_input")
                        )
                        OutlinedTextField(
                            value = katthaInput,
                            onValueChange = { viewModel.updateConverterFromTerai(bighaInput, it, dhurInput) },
                            label = { Text(if (isNe) "कठ्ठा" else "Kattha") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("converter_kattha_input")
                        )
                        OutlinedTextField(
                            value = dhurInput,
                            onValueChange = { viewModel.updateConverterFromTerai(bighaInput, katthaInput, it) },
                            label = { Text(if (isNe) "धुर" else "Dhur") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("converter_dhur_input")
                        )
                    }
                }
            }
        }

        // Live Conversion Result Display & Copy/Share
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isNe) "रूपान्तरित नाप परिणाम" else "Converted Result",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Mixed Terai line: e.g. "2 Kattha 19.06 Dhur"
                    val mixedFormatted = teraiResult.toFormattedString(isNe)
                    Text(
                        text = mixedFormatted,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Breakdown chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            val totalDhurStr = String.format(java.util.Locale.US, "%.2f Dhur", teraiResult.totalDhur)
                            Text(
                                text = if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(totalDhurStr) else totalDhurStr,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            val totalKatthaStr = String.format(java.util.Locale.US, "%.2f Kattha", teraiResult.totalKattha)
                            Text(
                                text = if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(totalKatthaStr) else totalKatthaStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val totalSqFtStr = String.format(java.util.Locale.US, "%.1f sq ft", teraiResult.totalSqFt)
                            Text(
                                text = if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(totalSqFtStr) else totalSqFtStr,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            val totalBighaStr = String.format(java.util.Locale.US, "%.4f Bigha", teraiResult.totalBigha)
                            Text(
                                text = if (useNepaliDigits || isNe) NumberLocalizationUtil.toNepaliDigits(totalBighaStr) else totalBighaStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Copy & Share buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Land Area", "${teraiResult.toFormattedString(isNe)} = ${m2Input} m²")
                                clipboard.setPrimaryClip(clip)
                                viewModel.showSnackbar(StringsRepo.get("copied", lang))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(StringsRepo.get("copy_result", lang), fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val shareText = "Land measurement from Pancheshwor Real Estate: ${teraiResult.toFormattedString(isNe)} = ${m2Input} m² (${String.format(java.util.Locale.US, "%.1f", teraiResult.totalSqFt)} sq ft)"
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Measurement"))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(StringsRepo.get("share", lang), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Optional Hill Units (Ropani / Aana) Toggle Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Landscape, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = StringsRepo.get("hill_units", lang),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = showHillUnits,
                            onCheckedChange = { viewModel.converterShowHillUnits.value = it }
                        )
                    }

                    if (showHillUnits) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "1 Ropani = 16 Aana = 64 Paisa = 508.74 m²",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = hillResult.toFormattedString(isNe),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}
