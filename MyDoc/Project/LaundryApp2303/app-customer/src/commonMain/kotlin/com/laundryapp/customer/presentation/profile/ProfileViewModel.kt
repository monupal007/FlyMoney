package com.laundryapp.customer.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.User
import com.laundryapp.core.data.model.UserRole
import com.laundryapp.core.data.repository.AuthRepository
import com.laundryapp.core.data.repository.OrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val orderCount: Int = 0,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val isUploadingImage: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isEditMode: Boolean = false
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val userResult = authRepository.getUserProfile(userId)
            val orders = orderRepository.getCustomerOrders(userId).first()
            
            userResult.onSuccess { user ->
                _uiState.update { it.copy(
                    user = user, 
                    orderCount = orders.size,
                    isLoading = false
                ) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode, successMessage = null, error = null) }
    }

    fun updateProfile(name: String, email: String, phone: String) {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }
            val updates = mapOf(
                "name" to name,
                "email" to email,
                "phone" to phone
            )
            authRepository.updateProfile(userId, updates, UserRole.CUSTOMER)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isUpdating = false, 
                            isEditMode = false,
                            user = it.user?.copy(name = name, email = email, phone = phone),
                            successMessage = "Profile updated successfully"
                        ) 
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isUpdating = false, error = e.message) }
                }
        }
    }

    fun uploadProfilePicture(imageData: Any) {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingImage = true, error = null) }
            authRepository.uploadProfilePicture(userId, imageData, UserRole.CUSTOMER)
                .onSuccess { imageUrl ->
                    _uiState.update { 
                        it.copy(
                            isUploadingImage = false, 
                            user = it.user?.copy(profileImageUrl = imageUrl),
                            successMessage = "Profile picture updated"
                        ) 
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isUploadingImage = false, error = e.message) }
                }
        }
    }

    fun logout() = authRepository.signOut()
    
    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, error = null) }
    }
}
