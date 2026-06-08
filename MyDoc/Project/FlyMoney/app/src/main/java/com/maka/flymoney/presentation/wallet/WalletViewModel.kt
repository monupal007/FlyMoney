package com.maka.flymoney.presentation.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maka.flymoney.domain.model.Transaction
import com.maka.flymoney.domain.repository.AuthRepository
import com.maka.flymoney.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    val balance: Flow<Double> = authRepository.currentUser.flatMapLatest { user ->
        user?.let {
            userRepository.getUserProfile(it.uid).map { it?.walletBalance ?: 0.0 }
        } ?: flowOf(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val transactions: Flow<List<Transaction>> = authRepository.currentUser.flatMapLatest { user ->
        user?.let {
            userRepository.getTransactionHistory(it.uid)
        } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deposit(amount: Double) {
        viewModelScope.launch {
            val uid = authRepository.currentUser.first()?.uid ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            userRepository.deposit(uid, amount)
                .onSuccess { _uiState.update { it.copy(isLoading = false, message = "Deposit successful") } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun withdraw(amount: Double) {
        viewModelScope.launch {
            val uid = authRepository.currentUser.first()?.uid ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            userRepository.withdraw(uid, amount)
                .onSuccess { _uiState.update { it.copy(isLoading = false, message = "Withdrawal successful") } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}

data class WalletUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
