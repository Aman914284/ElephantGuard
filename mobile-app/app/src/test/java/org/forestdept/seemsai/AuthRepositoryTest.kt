package org.forestdept.seemsai

import org.forestdept.seemsai.auth.AuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        // Initialize repository in in-memory test mode (no context)
        authRepository = AuthRepository(context = null)
    }

    @Test
    fun testDefaultDemoAccount_exists() {
        val user = authRepository.defaultDemoAccount
        assertEquals("Wildlife Officer", user.fullName)
        assertEquals("officer@elephantguard.org", user.email)
        assertEquals("9876543210", user.mobileNumber)
        assertEquals("guard123", user.password)
    }

    @Test
    fun testEmptyLogin_failsValidation() {
        // 1. Both empty
        val result1 = authRepository.login("", "")
        assertTrue(result1.isFailure)

        // 2. Empty password
        val result2 = authRepository.login("officer@elephantguard.org", "")
        assertTrue(result2.isFailure)

        // 3. Empty identifier
        val result3 = authRepository.login("", "guard123")
        assertTrue(result3.isFailure)
    }

    @Test
    fun testDemoLogin_succeeds() {
        val officerUser = authRepository.loginAsDemo()
        assertNotNull(officerUser)
        assertEquals("Wildlife Officer", officerUser.fullName)
        assertEquals(officerUser, authRepository.getCurrentUser())
    }

    @Test
    fun testLoginWithDemoCredentials_byEmail_and_byMobile() {
        // Login by Email
        val emailResult = authRepository.login("officer@elephantguard.org", "guard123")
        assertTrue(emailResult.isSuccess)
        assertEquals("Wildlife Officer", emailResult.getOrNull()?.fullName)

        // Login by Mobile
        val mobileResult = authRepository.login("9876543210", "guard123")
        assertTrue(mobileResult.isSuccess)
        assertEquals("Wildlife Officer", mobileResult.getOrNull()?.fullName)
    }

    @Test
    fun testInvalidPassword_fails() {
        val result = authRepository.login("officer@elephantguard.org", "wrongPassword")
        assertTrue(result.isFailure)
    }

    @Test
    fun testCreateDemoAccount_and_LoginWithNewAccount() {
        val createResult = authRepository.createAccount(
            fullName = "Vikram Forest Guard",
            mobileNumber = "9123456780",
            email = "vikram@forest.gov.in",
            password = "securePassword123"
        )

        assertTrue(createResult.isSuccess)
        val createdUser = createResult.getOrNull()
        assertNotNull(createdUser)
        assertEquals("Vikram Forest Guard", createdUser?.fullName)
        assertEquals("vikram@forest.gov.in", createdUser?.email)
        assertEquals("9123456780", createdUser?.mobileNumber)

        // Verify newly created account can log in with Email
        val emailLogin = authRepository.login("vikram@forest.gov.in", "securePassword123")
        assertTrue(emailLogin.isSuccess)
        assertEquals("Vikram Forest Guard", emailLogin.getOrNull()?.fullName)

        // Verify newly created account can log in with Mobile
        val mobileLogin = authRepository.login("9123456780", "securePassword123")
        assertTrue(mobileLogin.isSuccess)
        assertEquals("Vikram Forest Guard", mobileLogin.getOrNull()?.fullName)
    }

    @Test
    fun testCreateDemoAccount_invalidInputs() {
        // 1. Short name
        val shortNameResult = authRepository.createAccount(
            fullName = "A",
            mobileNumber = "9123456780",
            email = "valid@forest.org",
            password = "password123"
        )
        assertTrue(shortNameResult.isFailure)

        // 2. Invalid mobile (less than 10 digits)
        val invalidMobileResult = authRepository.createAccount(
            fullName = "Valid Name",
            mobileNumber = "12345",
            email = "valid@forest.org",
            password = "password123"
        )
        assertTrue(invalidMobileResult.isFailure)

        // 3. Invalid email format
        val invalidEmailResult = authRepository.createAccount(
            fullName = "Valid Name",
            mobileNumber = "9123456780",
            email = "notanemail",
            password = "password123"
        )
        assertTrue(invalidEmailResult.isFailure)

        // 4. Short password
        val shortPasswordResult = authRepository.createAccount(
            fullName = "Valid Name",
            mobileNumber = "9123456780",
            email = "valid@forest.org",
            password = "123"
        )
        assertTrue(shortPasswordResult.isFailure)
    }

    @Test
    fun testDuplicateAccount_prevention() {
        // Attempt creating account with default officer email
        val dupEmail = authRepository.createAccount(
            fullName = "Duplicate User",
            mobileNumber = "9988776655",
            email = "officer@elephantguard.org",
            password = "password123"
        )
        assertTrue(dupEmail.isFailure)

        // Attempt creating account with default officer mobile
        val dupMobile = authRepository.createAccount(
            fullName = "Duplicate User",
            mobileNumber = "9876543210",
            email = "unique@forest.org",
            password = "password123"
        )
        assertTrue(dupMobile.isFailure)
    }

    @Test
    fun testLogout_clearsSession() {
        authRepository.loginAsDemo()
        assertNotNull(authRepository.getCurrentUser())

        authRepository.logout()
        assertNull(authRepository.getCurrentUser())
    }

    @Test
    fun testValidationHelpers() {
        // Email validation
        assertTrue(AuthRepository.validateEmail("guard@elephantguard.org"))
        assertTrue(AuthRepository.validateEmail("user.name+tag@sub.domain.co"))
        assertFalse(AuthRepository.validateEmail("plainaddress"))
        assertFalse(AuthRepository.validateEmail("@missinguser.com"))
        assertFalse(AuthRepository.validateEmail("missingdomain@"))

        // Mobile validation
        assertTrue(AuthRepository.validateMobile("9876543210"))
        assertTrue(AuthRepository.validateMobile("+91 9876543210"))
        assertTrue(AuthRepository.validateMobile("98765-43210"))
        assertFalse(AuthRepository.validateMobile("123"))
        assertFalse(AuthRepository.validateMobile("abcdefghij"))
    }
}
