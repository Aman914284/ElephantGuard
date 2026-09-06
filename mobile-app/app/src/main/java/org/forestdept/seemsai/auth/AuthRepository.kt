package org.forestdept.seemsai.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.regex.Pattern

/**
 * Local repository for prototype authentication in Elephant Guard.
 * Manages Demo users and dynamically registered demo accounts in-memory + local SharedPreferences.
 */
class AuthRepository(private val context: Context? = null) {

    private val gson = Gson()
    private val prefName = "elephant_guard_auth_prefs"
    private val keyAccounts = "registered_accounts"
    private val keyActiveSession = "active_session_user"

    // Default Pre-seeded Officer Account
    val defaultDemoAccount = UserAccount(
        fullName = "Wildlife Officer",
        mobileNumber = "9876543210",
        email = "officer@elephantguard.org",
        password = "guard123",
        role = "Forest Wildlife Patrol Officer",
        isDemo = false
    )

    private val registeredAccounts = mutableListOf<UserAccount>()
    private var currentUser: UserAccount? = null

    init {
        // Load default demo user
        registeredAccounts.add(defaultDemoAccount)

        // Load any persisted demo accounts and active session if context is provided
        context?.let { ctx ->
            try {
                val prefs: SharedPreferences = ctx.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                val json = prefs.getString(keyAccounts, null)
                if (!json.isNullOrBlank()) {
                    val listType = object : TypeToken<List<UserAccount>>() {}.type
                    val savedList: List<UserAccount>? = gson.fromJson(json, listType)
                    if (savedList != null) {
                        for (account in savedList) {
                            if (registeredAccounts.none { it.email.equals(account.email, ignoreCase = true) || it.mobileNumber == account.mobileNumber }) {
                                registeredAccounts.add(account)
                            }
                        }
                    }
                }

                // Restore active user session across app restarts
                val sessionJson = prefs.getString(keyActiveSession, null)
                if (!sessionJson.isNullOrBlank()) {
                    val savedSessionUser: UserAccount? = gson.fromJson(sessionJson, UserAccount::class.java)
                    if (savedSessionUser != null) {
                        currentUser = savedSessionUser
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Attempts login using either Email ID or Mobile Number.
     */
    fun login(identifier: String, password: String): Result<UserAccount> {
        val cleanIdentifier = identifier.trim()
        val cleanPassword = password.trim()

        if (cleanIdentifier.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter your Email ID or Mobile Number."))
        }
        if (cleanPassword.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter your password."))
        }

        val found = registeredAccounts.firstOrNull {
            (it.email.equals(cleanIdentifier, ignoreCase = true) || it.mobileNumber == cleanIdentifier) &&
                    it.password == cleanPassword
        }

        return if (found != null) {
            currentUser = found
            saveSession(found)
            Result.success(found)
        } else {
            Result.failure(IllegalArgumentException("Invalid credentials. Please verify your Email ID/Mobile Number and password."))
        }
    }

    /**
     * One-tap instant Demo login for quick inspection and field testing.
     */
    fun loginAsDemo(): UserAccount {
        currentUser = defaultDemoAccount
        saveSession(defaultDemoAccount)
        return defaultDemoAccount
    }

    /**
     * Registers a new local demo account.
     */
    fun createAccount(
        fullName: String,
        mobileNumber: String,
        email: String,
        password: String
    ): Result<UserAccount> {
        val cleanName = fullName.trim()
        val cleanMobile = mobileNumber.trim()
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        // Validation
        if (cleanName.length < 2) {
            return Result.failure(IllegalArgumentException("Full name must be at least 2 characters."))
        }
        if (!validateMobile(cleanMobile)) {
            return Result.failure(IllegalArgumentException("Please enter a valid 10-digit mobile number."))
        }
        if (!validateEmail(cleanEmail)) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (cleanPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        // Duplicate checks
        val emailExists = registeredAccounts.any { it.email.equals(cleanEmail, ignoreCase = true) }
        if (emailExists) {
            return Result.failure(IllegalArgumentException("An account with this Email ID already exists."))
        }

        val mobileExists = registeredAccounts.any { it.mobileNumber == cleanMobile }
        if (mobileExists) {
            return Result.failure(IllegalArgumentException("An account with this Mobile Number already exists."))
        }

        val newAccount = UserAccount(
            fullName = cleanName,
            mobileNumber = cleanMobile,
            email = cleanEmail,
            password = cleanPassword,
            role = "Community Wildlife Guard",
            isDemo = false
        )

        registeredAccounts.add(newAccount)
        persistAccounts()
        currentUser = newAccount
        saveSession(newAccount)

        return Result.success(newAccount)
    }

    fun getCurrentUser(): UserAccount? = currentUser

    fun logout() {
        currentUser = null
        clearSession()
    }

    fun getAllAccounts(): List<UserAccount> = registeredAccounts.toList()

    private fun saveSession(user: UserAccount) {
        context?.let { ctx ->
            try {
                val prefs: SharedPreferences = ctx.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                val json = gson.toJson(user)
                prefs.edit().putString(keyActiveSession, json).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun clearSession() {
        context?.let { ctx ->
            try {
                val prefs: SharedPreferences = ctx.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                prefs.edit().remove(keyActiveSession).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun persistAccounts() {
        context?.let { ctx ->
            try {
                val prefs: SharedPreferences = ctx.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                val json = gson.toJson(registeredAccounts.filter { it.email != defaultDemoAccount.email })
                prefs.edit().putString(keyAccounts, json).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private val EMAIL_REGEX = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
        private val MOBILE_REGEX = Pattern.compile("^[0-9]{10}$")

        fun validateEmail(email: String): Boolean {
            return EMAIL_REGEX.matcher(email.trim()).matches()
        }

        fun validateMobile(mobile: String): Boolean {
            val clean = mobile.trim().replace("+91", "").replace(" ", "").replace("-", "")
            return MOBILE_REGEX.matcher(clean).matches()
        }
    }
}
