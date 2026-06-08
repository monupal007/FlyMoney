package com.maka.flymoney

import com.maka.flymoney.data.remote.GameWebSocketClient
import com.maka.flymoney.domain.model.Round
import com.maka.flymoney.domain.model.RoundState
import com.maka.flymoney.domain.repository.AuthRepository
import com.maka.flymoney.domain.repository.GameRepository
import com.maka.flymoney.domain.repository.UserRepository
import com.maka.flymoney.presentation.game.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var gameRepository: GameRepository
    @Mock
    private lateinit var userRepository: UserRepository
    @Mock
    private lateinit var authRepository: AuthRepository
    @Mock
    private lateinit var wsClient: GameWebSocketClient

    private lateinit var viewModel: GameViewModel
    private val wsEvents = MutableSharedFlow<GameWebSocketClient.GameEvent>()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        `when`(gameRepository.currentRound).thenReturn(flowOf(Round(state = RoundState.WAITING)))
        `when`(wsClient.events).thenReturn(wsEvents)
        `when`(authRepository.currentUser).thenReturn(flowOf(null))

        viewModel = GameViewModel(gameRepository, userRepository, authRepository, wsClient)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is WAITING`() = runTest {
        assertEquals(RoundState.WAITING, viewModel.uiState.value.roundState)
        assertEquals(1.0, viewModel.uiState.value.multiplier, 0.0)
    }

    @Test
    fun `tick event updates multiplier`() = runTest {
        wsEvents.emit(GameWebSocketClient.GameEvent.Tick(2.5))
        advanceUntilIdle()
        assertEquals(2.5, viewModel.uiState.value.multiplier, 0.0)
    }

    @Test
    fun `crash event updates state to CRASHED`() = runTest {
        wsEvents.emit(GameWebSocketClient.GameEvent.Crashed(3.0))
        advanceUntilIdle()
        assertEquals(RoundState.CRASHED, viewModel.uiState.value.roundState)
        assertEquals(3.0, viewModel.uiState.value.crashAt ?: 0.0, 0.0)
    }
}
