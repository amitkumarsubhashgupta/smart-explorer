package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ReferralHistoryItem
import com.example.data.local.ReferralUserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.ReferralRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface RedeemState {
    object Idle : RedeemState
    object Loading : RedeemState
    data class Success(val message: String) : RedeemState
    data class Error(val error: String) : RedeemState
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReferralViewModel(
    private val referralRepository: ReferralRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Current user state from Auth
    val currentUser = authRepository.currentUser

    // Observe user profile reactively based on currently logged in User Uid
    val userProfile: StateFlow<ReferralUserProfile?> = currentUser
        .flatMapLatest { user ->
            if (user != null) {
                referralRepository.getUserProfileFlow(user.uid)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Observe referral history reactively based on currently logged in User Uid
    val referralHistory: StateFlow<List<ReferralHistoryItem>> = currentUser
        .flatMapLatest { user ->
            if (user != null) {
                referralRepository.getHistoryFlow(user.uid)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _redeemState = MutableStateFlow<RedeemState>(RedeemState.Idle)
    val redeemState: StateFlow<RedeemState> = _redeemState.asStateFlow()

    init {
        // Automatically fetch or initialize profile when user logs in
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    try {
                        referralRepository.getOrInitializeProfile(user.uid, user.name)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * Refreshes or triggers manual initialization of referral details.
     */
    fun refreshProfile() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            try {
                referralRepository.getOrInitializeProfile(user.uid, user.name)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Resets the redemption state status.
     */
    fun resetRedeemState() {
        _redeemState.value = RedeemState.Idle
    }

    /**
     * Redeems a friend's referral code.
     */
    fun redeemReferralCode(code: String) {
        val user = currentUser.value
        if (user == null) {
            _redeemState.value = RedeemState.Error("You must be logged in to redeem a code.")
            return
        }

        if (code.isBlank()) {
            _redeemState.value = RedeemState.Error("Please enter a referral code.")
            return
        }

        viewModelScope.launch {
            _redeemState.value = RedeemState.Loading
            try {
                referralRepository.redeemCode(user.uid, user.name, code)
                _redeemState.value = RedeemState.Success("Successfully redeemed code! You earned 2 welcome points!")
            } catch (e: IllegalArgumentException) {
                _redeemState.value = RedeemState.Error(e.message ?: "Invalid code.")
            } catch (e: IllegalStateException) {
                _redeemState.value = RedeemState.Error(e.message ?: "Error processing request.")
            } catch (e: Exception) {
                _redeemState.value = RedeemState.Error("Error: ${e.localizedMessage ?: "Unknown network error"}")
            }
        }
    }

    companion object {
        fun provideFactory(
            referralRepository: ReferralRepository,
            authRepository: AuthRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReferralViewModel(referralRepository, authRepository) as T
                }
            }
    }
}
