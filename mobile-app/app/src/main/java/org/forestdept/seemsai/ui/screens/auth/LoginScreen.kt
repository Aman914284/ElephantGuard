package org.forestdept.seemsai.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.forestdept.seemsai.auth.AuthViewModel
import org.forestdept.seemsai.ui.components.ElephantGuardHeader
import org.forestdept.seemsai.ui.components.ForestErrorCard
import org.forestdept.seemsai.ui.components.ForestPrimaryButton
import org.forestdept.seemsai.ui.components.ForestSecondaryButton
import org.forestdept.seemsai.ui.components.ForestSuccessCard
import org.forestdept.seemsai.ui.components.ForestTextButton
import org.forestdept.seemsai.ui.components.ForestTextField
import org.forestdept.seemsai.ui.components.PoweredBySeemsAiBadge
import org.forestdept.seemsai.ui.theme.ForestCard
import org.forestdept.seemsai.ui.theme.ForestCardBorder
import org.forestdept.seemsai.ui.theme.ForestDark900
import org.forestdept.seemsai.ui.theme.ForestDark950
import org.forestdept.seemsai.ui.theme.ForestGreenLight
import org.forestdept.seemsai.ui.theme.ForestGreenPrimary
import org.forestdept.seemsai.ui.theme.TextMutedSage
import org.forestdept.seemsai.ui.theme.TextSubtle
import org.forestdept.seemsai.ui.theme.TextWhite

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToCreateAccount: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var identifierError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ForestDark950)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Branding Header
            ElephantGuardHeader(
                title = "ELEPHANT GUARD",
                subtitle = "Protect Wildlife. Protect Lives."
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Banner Notifications (Error / Success)
            if (authState.errorMessage != null) {
                ForestErrorCard(message = authState.errorMessage!!)
                Spacer(modifier = Modifier.height(14.dp))
            } else if (authState.successMessage != null) {
                ForestSuccessCard(message = authState.successMessage!!)
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Input Card Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ForestCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Sign In to Early Warning Portal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "Enter your credentials to access the early warning network",
                        fontSize = 12.sp,
                        color = TextMutedSage,
                        modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
                    )

                    // Email / Mobile Field
                    ForestTextField(
                        value = identifier,
                        onValueChange = {
                            identifier = it
                            identifierError = null
                            authViewModel.clearMessages()
                        },
                        label = "Email ID / Mobile Number",
                        placeholder = "officer@forest.gov or 9876543210",
                        leadingIcon = Icons.Default.Person,
                        isError = identifierError != null,
                        errorMessage = identifierError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    ForestTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                            authViewModel.clearMessages()
                        },
                        label = "Password",
                        placeholder = "Enter your password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        isPasswordVisible = isPasswordVisible,
                        onPasswordToggle = { isPasswordVisible = !isPasswordVisible },
                        isError = passwordError != null,
                        errorMessage = passwordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (identifier.isBlank()) {
                                    identifierError = "Email ID or Mobile Number is required."
                                }
                                if (password.isBlank()) {
                                    passwordError = "Password is required."
                                }
                                if (identifier.isNotBlank() && password.isNotBlank()) {
                                    authViewModel.login(identifier, password, onLoginSuccess)
                                }
                            }
                        )
                    )

                    // Forgot Password Text
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        ForestTextButton(
                            text = "Forgot Password?",
                            onClick = { showForgotPasswordDialog = true },
                            color = ForestGreenLight
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Login Button
                    ForestPrimaryButton(
                        text = "LOGIN",
                        isLoading = authState.isLoading,
                        onClick = {
                            focusManager.clearFocus()
                            var hasError = false
                            if (identifier.isBlank()) {
                                identifierError = "Email ID or Mobile Number is required."
                                hasError = true
                            }
                            if (password.isBlank()) {
                                passwordError = "Password is required."
                                hasError = true
                            }
                            if (!hasError) {
                                authViewModel.login(identifier, password, onLoginSuccess)
                            }
                        },
                        icon = Icons.AutoMirrored.Filled.Login
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Create New Account Option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    fontSize = 14.sp,
                    color = TextMutedSage
                )
                Spacer(modifier = Modifier.width(6.dp))
                ForestTextButton(
                    text = "Create New Account",
                    onClick = onNavigateToCreateAccount,
                    color = ForestGreenLight,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Credentials Helper Dialog
        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                containerColor = ForestCard,
                titleContentColor = TextWhite,
                textContentColor = TextMutedSage,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ForestGreenLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Account Access Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "You can sign in using your registered account or use the pre-configured officer credentials below:",
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ForestDark900),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ForestCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Wildlife Patrol Officer Credentials:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• Email: officer@elephantguard.org",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ForestGreenLight
                                )
                                Text(
                                    text = "• Mobile: 9876543210",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ForestGreenLight
                                )
                                Text(
                                    text = "• Password: guard123",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ForestGreenLight
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            identifier = "officer@elephantguard.org"
                            password = "guard123"
                            identifierError = null
                            passwordError = null
                            showForgotPasswordDialog = false
                        }
                    ) {
                        Text(
                            text = "Auto-Fill Credentials",
                            color = ForestGreenLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text(text = "Close", color = TextMutedSage)
                    }
                }
            )
        }
    }
}
