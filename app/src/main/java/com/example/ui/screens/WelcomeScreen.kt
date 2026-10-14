package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.ThemeModePreference
import com.example.ui.components.PropertyImageView
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun WelcomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isNe = currentUser.language == AppLanguage.NEPALI

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("welcome_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isNe) "पञ्चेश्वर रियल इस्टेट" else "Pancheshwor Real Estate",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isNe) "महेन्द्रनगर (भीमदत्त नगरपालिका) तथा कञ्चनपुर" else "Mahendranagar (Bhimdatta Municipality), Kanchanpur",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Step 1: Language selection
            Text(
                text = if (isNe) "१. भाषा छनौट गर्नुहोस् (Choose Language)" else "1. Choose Language (भाषा छनौट)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    onClick = { viewModel.updateLanguage(AppLanguage.ENGLISH) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (currentUser.language == AppLanguage.ENGLISH) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = if (currentUser.language == AppLanguage.ENGLISH) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text("English", fontWeight = FontWeight.Bold)
                        Text("English Interface", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    onClick = { viewModel.updateLanguage(AppLanguage.NEPALI) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (currentUser.language == AppLanguage.NEPALI) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = if (currentUser.language == AppLanguage.NEPALI) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text("नेपाली", fontWeight = FontWeight.Bold)
                        Text("नेपाली इन्टरफेस", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step 2: Theme selection
            Text(
                text = if (isNe) "२. थिम छनौट गर्नुहोस् (Select Theme)" else "2. Select Theme",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple(ThemeModePreference.SYSTEM, "System", "प्रणाली"),
                    Triple(ThemeModePreference.LIGHT, "Light", "उज्यालो"),
                    Triple(ThemeModePreference.DARK, "Dark", "अँध्यारो")
                ).forEach { (mode, en, ne) ->
                    val isSel = currentUser.themeMode == mode
                    Surface(
                        onClick = { viewModel.updateTheme(mode) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSel) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary) else null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 12.dp)) {
                            Text(
                                text = if (isNe) ne else en,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Get Started Button
        Button(
            onClick = { viewModel.navigateTo(AppScreen.HOME) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
                .testTag("welcome_get_started_btn")
        ) {
            Text(
                text = if (isNe) "महेन्द्रनगरका सम्पत्तिहरू हेर्नुहोस्" else "Explore Properties in Bhimdatta",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}
