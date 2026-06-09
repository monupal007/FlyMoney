package com.maka.flymoney.presentation.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 for Live Bets, 1 for Chat
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            RoundHistoryBar(history = uiState.roundHistory)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFF0D0D1A))
        ) {
            CurveCanvas(
                multiplier = uiState.multiplier,
                roundState = uiState.roundState,
                countdown = uiState.countdown,
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxWidth()
            )

            // Tab Switcher for Live Bets / Chat
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color(0xFF13132A)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabItem(
                    text = "LIVE BETS",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabItem(
                    text = "CHAT",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Box(modifier = Modifier.height(150.dp)) {
                if (selectedTab == 0) {
                    LiveBetsPanel(bets = uiState.liveBets)
                } else {
                    ChatPanel(
                        messages = uiState.chatMessages,
                        onSendMessage = { viewModel.sendChatMessage(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            BetPanel(
                uiState = uiState,
                onPlaceBetA = { viewModel.placeBetA() },
                onCashOutA = { viewModel.cashOutA() },
                onCancelBetA = { viewModel.cancelBetA() },
                onUpdateAmountA = { viewModel.updateBetAmountA(it) },
                onUpdateAutoCashoutA = { viewModel.updateAutoCashoutA(it) },
                onPlaceBetB = { viewModel.placeBetB() },
                onCashOutB = { viewModel.cashOutB() },
                onCancelBetB = { viewModel.cancelBetB() },
                onUpdateAmountB = { viewModel.updateBetAmountB(it) },
                onUpdateAutoCashoutB = { viewModel.updateAutoCashoutB(it) },
                modifier = Modifier.weight(0.45f)
            )
        }
    }
}

@Composable
fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                color = if (isSelected) Color(0xFF00FF88) else Color(0xFF8888AA),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(Color(0xFF00FF88))
                )
            }
        }
    }
}
