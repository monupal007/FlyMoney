package com.maka.flymoney.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maka.flymoney.domain.model.User
import com.maka.flymoney.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow("daily")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    val leaderboardPlayers: StateFlow<List<User>> = _selectedPeriod.flatMapLatest { period ->
        userRepository.getLeaderboard(period)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setPeriod(period: String) {
        _selectedPeriod.value = period
    }
}
