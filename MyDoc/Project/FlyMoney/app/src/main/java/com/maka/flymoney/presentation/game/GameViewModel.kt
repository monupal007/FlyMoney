package com.maka.flymoney.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maka.flymoney.R
import com.maka.flymoney.data.remote.GameWebSocketClient
import com.maka.flymoney.domain.model.Bet
import com.maka.flymoney.domain.model.ChatMessage
import com.maka.flymoney.domain.model.RoundState
import com.maka.flymoney.domain.repository.AuthRepository
import com.maka.flymoney.domain.repository.GameRepository
import com.maka.flymoney.domain.repository.UserRepository
import com.maka.flymoney.util.HapticManager
import com.maka.flymoney.util.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val wsClient: GameWebSocketClient,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        observeGameData()
        wsClient.connect()
    }

    private fun observeGameData() {
        viewModelScope.launch {
            gameRepository.currentRound.collectLatest { round ->
                _uiState.update { it.copy(
                    roundState = round.state,
                    multiplier = if (round.state == RoundState.RUNNING) it.multiplier else round.multiplier,
                    countdown = round.countdownSec,
                    crashAt = round.crashAt
                ) }
                
                if (round.state == RoundState.CRASHED) {
                    handleRoundCrashed(round.multiplier)
                }
            }
        }

        viewModelScope.launch {
            gameRepository.roundHistory.collectLatest { history ->
                _uiState.update { it.copy(roundHistory = history) }
            }
        }

        viewModelScope.launch {
            wsClient.events.collect { event ->
                when (event) {
                    is GameWebSocketClient.GameEvent.Tick -> {
                        _uiState.update { it.copy(multiplier = event.multiplier) }
                    }
                    is GameWebSocketClient.GameEvent.Crashed -> {
                        _uiState.update { state -> 
                            state.copy(
                                roundState = RoundState.CRASHED, 
                                crashAt = event.crashAt
                            ) 
                        }
                        handleRoundCrashed(event.crashAt)
                    }
                    is GameWebSocketClient.GameEvent.Waiting -> {
                        _uiState.update { it.copy(roundState = RoundState.WAITING, countdown = event.countdownSec) }
                        resetActiveBets()
                    }
                    is GameWebSocketClient.GameEvent.BetPlaced -> {
                        _uiState.update { state ->
                            val newBet = Bet(uid = event.uid, username = event.username, amount = event.amount)
                            state.copy(liveBets = (state.liveBets + newBet).takeLast(50))
                        }
                    }
                    is GameWebSocketClient.GameEvent.CashedOut -> {
                        _uiState.update { state ->
                            state.copy(liveBets = state.liveBets.map { 
                                if (it.uid == event.uid) it.copy(cashedOut = true, multiplier = event.multiplier, winnings = event.winnings)
                                else it
                            })
                        }
                    }
                    is GameWebSocketClient.GameEvent.ChatReceived -> {
                        _uiState.update { state ->
                            val newMessage = ChatMessage(
                                uid = event.uid,
                                username = event.username,
                                message = event.message,
                                timestamp = event.timestamp
                            )
                            state.copy(chatMessages = (state.chatMessages + newMessage).takeLast(100))
                        }
                    }
                    else -> {}
                }
            }
        }

        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                user?.let {
                    userRepository.getUserProfile(it.uid).collectLatest { profile ->
                        _uiState.update { it.copy(walletBalance = profile?.walletBalance ?: 0.0) }
                    }
                }
            }
        }
    }

    private fun handleRoundCrashed(crashAt: Double) {
        soundManager.playSound(R.raw.crash)
        hapticManager.vibrateCrash()
        _uiState.update { state ->
            state.copy(
                activeBetA = state.activeBetA?.let { if (!it.cashedOut) it.copy(isBusted = true) else it },
                activeBetB = state.activeBetB?.let { if (!it.cashedOut) it.copy(isBusted = true) else it }
            )
        }
    }

    private fun resetActiveBets() {
        _uiState.update { it.copy(
            activeBetA = if (it.activeBetA?.cashedOut == false && !it.activeBetA.isBusted) it.activeBetA else null,
            activeBetB = if (it.activeBetB?.cashedOut == false && !it.activeBetB.isBusted) it.activeBetB else null,
            crashAt = null,
            multiplier = 1.0
        ) }
    }

    fun placeBetA() = placeBet(Slot.A)
    fun placeBetB() = placeBet(Slot.B)

    private fun placeBet(slot: Slot) {
        val amountStr = if (slot == Slot.A) _uiState.value.betAmountA else _uiState.value.betAmountB
        val amount = amountStr.toDoubleOrNull() ?: return
        if (amount <= 0 || amount > _uiState.value.walletBalance) return

        viewModelScope.launch {
            val autoCashoutStr = if (slot == Slot.A) _uiState.value.autoCashoutA else _uiState.value.autoCashoutB
            val autoCashout = autoCashoutStr.toDoubleOrNull()
            gameRepository.placeBet(amount, autoCashout).onSuccess {
                _uiState.update { state ->
                    if (slot == Slot.A) state.copy(activeBetA = ActiveBet(amount, autoCashout))
                    else state.copy(activeBetB = ActiveBet(amount, autoCashout))
                }
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun cancelBetA() = cancelBet(Slot.A)
    fun cancelBetB() = cancelBet(Slot.B)

    private fun cancelBet(slot: Slot) {
        viewModelScope.launch {
            gameRepository.cancelBet().onSuccess {
                _uiState.update { state ->
                    if (slot == Slot.A) state.copy(activeBetA = null)
                    else state.copy(activeBetB = null)
                }
            }
        }
    }

    fun cashOutA() = cashOut(Slot.A)
    fun cashOutB() = cashOut(Slot.B)

    private fun cashOut(slot: Slot) {
        viewModelScope.launch {
            gameRepository.cashOut().onSuccess { winnings ->
                soundManager.playSound(R.raw.cashout)
                hapticManager.vibrateSuccess()
                _uiState.update { state ->
                    if (slot == Slot.A) {
                        state.copy(activeBetA = state.activeBetA?.copy(cashedOut = true, winnings = winnings, cashoutMultiplier = state.multiplier))
                    } else {
                        state.copy(activeBetB = state.activeBetB?.copy(cashedOut = true, winnings = winnings, cashoutMultiplier = state.multiplier))
                    }
                }
            }
        }
    }

    fun sendChatMessage(message: String) {
        if (message.isNotBlank()) {
            wsClient.sendChat(message)
        }
    }

    fun updateBetAmountA(amount: String) = _uiState.update { it.copy(betAmountA = amount) }
    fun updateBetAmountB(amount: String) = _uiState.update { it.copy(betAmountB = amount) }

    fun updateAutoCashoutA(multiplier: String) = _uiState.update { it.copy(autoCashoutA = multiplier) }
    fun updateAutoCashoutB(multiplier: String) = _uiState.update { it.copy(autoCashoutB = multiplier) }

    enum class Slot { A, B }

    override fun onCleared() {
        super.onCleared()
        wsClient.disconnect()
        soundManager.stop()
    }
}

data class GameUiState(
    val roundState: RoundState = RoundState.WAITING,
    val multiplier: Double = 1.00,
    val crashAt: Double? = null,
    val countdown: Int = 5,
    val walletBalance: Double = 0.0,
    val activeBetA: ActiveBet? = null,
    val activeBetB: ActiveBet? = null,
    val betAmountA: String = "100",
    val betAmountB: String = "100",
    val autoCashoutA: String = "",
    val autoCashoutB: String = "",
    val liveBets: List<Bet> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val roundHistory: List<Double> = emptyList(),
    val errorMessage: String? = null,
    val isConnected: Boolean = true
)

data class ActiveBet(
    val amount: Double,
    val autoCashoutAt: Double?,
    val cashedOut: Boolean = false,
    val cashoutMultiplier: Double? = null,
    val winnings: Double? = null,
    val isBusted: Boolean = false
)
