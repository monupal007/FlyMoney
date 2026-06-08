package com.maka.flymoney.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maka.flymoney.domain.model.User
import com.maka.flymoney.domain.repository.AuthRepository
import com.maka.flymoney.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val userProfile: StateFlow<User?> = authRepository.currentUser.flatMapLatest { user ->
        user?.let {
            userRepository.getUserProfile(it.uid)
        } ?: flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            val uid = authRepository.currentUser.first()?.uid ?: return@launch
            userRepository.updateProfile(uid, newUsername, "")
        }
    }
}
