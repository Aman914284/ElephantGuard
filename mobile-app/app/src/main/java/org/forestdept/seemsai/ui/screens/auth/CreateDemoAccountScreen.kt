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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.forestdept.seemsai.auth.AuthRepository
import org.forestdept.seemsai.auth.AuthViewModel
import org.forestdept.seemsai.ui.components.ElephantGuardLogo
import org.forestdept.seemsai.ui.components.ForestErrorCard
import org.forestdept.seemsai.ui.components.ForestPrimaryButton
import org.forestdept.seemsai.ui.components.ForestTextButton
import org.forestdept.seemsai.ui.components.ForestTextField
import org.forestdept.seemsai.ui.theme.ForestCard
import org.forestdept.seemsai.ui.theme.ForestCardBorder
import org.forestdept.seemsai.ui.theme.ForestDark950
import org.forestdept.seemsai.ui.theme.ForestGreenLight
import org.forestdept.seemsai.ui.theme.TextMutedSage
import org.forestdept.seemsai.ui.theme.TextWhite

@Composable
fun CreateDemoAccountScreen(
    authViewModel: AuthViewModel,
    onNavigateBackToLogin: () -> Unit,
    onAccountCreatedSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var fullNameError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    fun validateForm(): Boolean {
        var isValid = true

        if (fullName.trim().length < 2) {
            fullNameError = "Full name must be at least 2 characters."
            isValid = false
        } else {
            fullNameError = null
        }

        if (!AuthRepository.validateMobile(mobileNumber)) {
            mobileError = "Enter a valid 10-digit mobile number."
            isValid = false
        } else {
            mobileError = null
        }

        if (!AuthRepository.validateEmail(email)) {
            emailError = "Enter a valid email address (e.g. name@domain.com)."
            isValid = false
        } else {
            emailError = null
        }

        if (password.length < 6) {
            passwordError = "Password must be at least 6 characters."
            isValid = false
        } else {
            passwordError = null
        }

        if (confirmPassword != password) {
            confirmPasswordError = "Passwords do not match."
            isValid = false
        } else {
            confirmPasswordError = null
        }

        return isValid
    }

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
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBackToLogin,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Login",
                        tint = TextWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Back to Login",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMutedSage
                )
            }

            // Screen Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElephantGuardLogo(sizeDp = 52)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Create New Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "Join the Wildlife Safety & Early Warning Network",
                        fontSize = 12.sp,
                        color = TextMutedSage
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Banner if Viewmodel returned an error
            if (authState.errorMessage != null) {
                ForestErrorCard(message = authState.errorMessage!!)
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Input Form Card
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
                    // Full Name
                    ForestTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            fullNameError = null
                            authViewModel.clearMessages()
                        },
                        label = "Full Name",
                        placeholder = "e.g. Officer Vikram Singh",
                        leadingIcon = Icons.Default.Person,
                        isError = fullNameError != null,
                        errorMessage = fullNameError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mobile Number
                    ForestTextField(
                        value = mobileNumber,
                        onValueChange = {
                            if (it.length <= 10) {
                                mobileNumber = it.filter { char -> char.isDigit() }
                            }
                            mobileError = null
                            authViewModel.clearMessages()
                        },
                        label = "Mobile Number",
                        placeholder = "10-digit mobile number",
                        leadingIcon = Icons.Default.Phone,
                        isError = mobileError != null,
                        errorMessage = mobileError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email ID
                    ForestTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                            authViewModel.clearMessages()
                        },
                        label = "Email ID",
                        placeholder = "e.g. vikram@forest.gov.in",
                        leadingIcon = Icons.Default.Email,
                        isError = emailError != null,
                        errorMessage = emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password
                    ForestTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                            authViewModel.clearMessages()
                        },
                        label = "Password (Min 6 chars)",
                        placeholder = "Create a secure password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        isPasswordVisible = isPasswordVisible,
                        onPasswordToggle = { isPasswordVisible = !isPasswordVisible },
                        isError = passwordError != null,
                        errorMessage = passwordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirm Password
                    ForestTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmPasswordError = null
                            authViewModel.clearMessages()
                        },
                        label = "Confirm Password",
                        placeholder = "Re-enter password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        isPasswordVisible = isConfirmPasswordVisible,
                        onPasswordToggle = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                        isError = confirmPasswordError != null,
                        errorMessage = confirmPasswordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (validateForm()) {
                                    authViewModel.createAccount(
                                        fullName = fullName,
                                        mobileNumber = mobileNumber,
                                        email = email,
                                        password = password,
                                        confirmPassword = confirmPassword,
                                        onSuccess = onAccountCreatedSuccess
                                    )
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button
                    ForestPrimaryButton(
                        text = "CREATE ACCOUNT",
                        isLoading = authState.isLoading,
                        onClick = {
                            focusManager.clearFocus()
                            if (validateForm()) {
                                authViewModel.createAccount(
                                    fullName = fullName,
                                    mobileNumber = mobileNumber,
                                    email = email,
                                    password = password,
                                    confirmPassword = confirmPassword,
                                    onSuccess = onAccountCreatedSuccess
                                )
                            }
                        },
                        icon = Icons.Default.PersonAdd
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back to Login Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    fontSize = 14.sp,
                    color = TextMutedSage
                )
                Spacer(modifier = Modifier.width(6.dp))
                ForestTextButton(
                    text = "Back to Login",
                    onClick = onNavigateBackToLogin,
                    color = ForestGreenLight,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
