package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val analyticsHelper: com.example.data.analytics.AnalyticsHelper
) : ViewModel() {
    val currentUser: StateFlow<UserProfile?> = authRepository.currentUser
    val isLoading: StateFlow<Boolean> = authRepository.isLoading

    // Legacy or temporary compatibility code flow
    private val _verificationCode = MutableStateFlow<String?>(null)
    val verificationCodeState: StateFlow<String?> = _verificationCode.asStateFlow()

    fun sendVerificationCode(name: String, email: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        // Compatibility mock helper
        viewModelScope.launch {
            val code = (100000..999999).random().toString()
            _verificationCode.value = code
            onSuccess(code)
        }
    }

    fun verifyCodeAndSignIn(enteredCode: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val correctCode = _verificationCode.value
            if (correctCode != null && enteredCode.trim() == correctCode) {
                _verificationCode.value = null
                onSuccess()
            } else {
                onError("Incorrect code entered.")
            }
        }
    }

    fun clearVerificationCode() {
        _verificationCode.value = null
    }

    // --- REAL FIREBASE AUTHENTICATION FLOWS ---

    fun registerWithEmail(name: String, email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.registerWithEmail(name, email, password)
            if (result.isSuccess) {
                analyticsHelper.logAuthEvent("email_password_registration", true)
                onSuccess()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "An unknown error occurred."
                analyticsHelper.logAuthEvent("email_password_registration", false, errorMsg)
                onError(errorMsg)
            }
        }
    }

    fun signInWithEmail(email: String, password: String, onSuccess: (UserProfile) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, password)
            if (result.isSuccess) {
                val user = result.getOrNull() ?: throw Exception("Failed to retrieve user profile.")
                analyticsHelper.logAuthEvent("email_password_login", true)
                onSuccess(user)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "An unknown error occurred."
                analyticsHelper.logAuthEvent("email_password_login", false, errorMsg)
                onError(errorMsg)
            }
        }
    }

    fun sendEmailVerification(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.sendEmailVerification()
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "An unknown error occurred.")
            }
        }
    }

    fun refreshVerificationStatus(onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.refreshVerificationStatus()
            if (result.isSuccess) {
                val user = result.getOrNull()
                onSuccess(user?.emailVerified == true)
            } else {
                onError(result.exceptionOrNull()?.message ?: "An unknown error occurred.")
            }
        }
    }

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "An unknown error occurred.")
            }
        }
    }

    fun signInAsGuest(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.signInAsGuest()
            if (result.isSuccess) {
                analyticsHelper.logAuthEvent("guest_mode_entry", true)
                onSuccess()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "An unknown error occurred."
                analyticsHelper.logAuthEvent("guest_mode_entry", false, errorMsg)
                onError(errorMsg)
            }
        }
    }

    fun signInWithGoogle(uid: String, name: String, email: String, photoUrl: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(uid, name, email, photoUrl)
            if (result.isSuccess) {
                analyticsHelper.logAuthEvent("google_sign_in", true)
                onSuccess()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "An unknown error occurred."
                analyticsHelper.logAuthEvent("google_sign_in", false, errorMsg)
                onError(errorMsg)
            }
        }
    }

    fun signOut(onSuccess: () -> Unit = {}, onComplete: () -> Unit = onSuccess) {
        viewModelScope.launch {
            analyticsHelper.logEvent("sign_out")
            authRepository.signOut()
            onComplete()
        }
    }

    fun deleteAccount(password: String?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.deleteAccount(password)
            if (result.isSuccess) {
                analyticsHelper.logEvent("account_deleted")
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "An unknown error occurred.")
            }
        }
    }

    fun updateProfile(name: String, email: String, photoUrl: String) {
        authRepository.updateProfile(name, email, photoUrl)
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            analyticsHelper: com.example.data.analytics.AnalyticsHelper
        ): androidx.lifecycle.ViewModelProvider.Factory =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(authRepository, analyticsHelper) as T
                }
            }
    }
}
