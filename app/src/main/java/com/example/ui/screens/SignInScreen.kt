package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.util.StringsRepo
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun SignInScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val lang = currentUser.language
    val isNe = lang == com.example.model.AppLanguage.NEPALI

    var phoneInput by remember { mutableStateOf("9848712345") }
    var otpInput by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.BUYER) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("signin_screen"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.LockPerson,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isNe) "पञ्चेश्वर रियल इस्टेटमा लग इन गर्नुहोस्" else "Sign In to Pancheshwor Real Estate",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isNe) "नेपाली मोबाइल नम्बर र एक पटकको कोड (OTP)" else "Sign in with Nepal mobile number (No password required)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Mobile Number
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text(if (isNe) "मोबाइल नम्बर" else "Nepal Mobile Number") },
                    leadingIcon = {
                        Text(
                            text = "+977 ",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!otpSent) {
                    Button(
                        onClick = {
                            otpSent = true
                            otpInput = "7842" // Demo OTP code
                            viewModel.showSnackbar("Demo OTP sent to +977 $phoneInput: 7842")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Send OTP (एसएमएस कोड पठाउनुहोस्)")
                    }
                } else {
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it },
                        label = { Text("Enter 4-Digit OTP Code") },
                        placeholder = { Text("7842") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.switchRole(selectedRole)
                            viewModel.showSnackbar("Signed in successfully as ${selectedRole.titleEn}!")
                            viewModel.navigateBack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verify & Continue (लग इन गर्नुहोस्)")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Role quick toggle for testing
        Text(
            text = "Testing Role Selection:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UserRole.values().forEach { role ->
                FilterChip(
                    selected = selectedRole == role,
                    onClick = { selectedRole = role },
                    label = { Text(role.titleEn.substringBefore(" "), fontSize = 11.sp) }
                )
            }
        }
    }
}
