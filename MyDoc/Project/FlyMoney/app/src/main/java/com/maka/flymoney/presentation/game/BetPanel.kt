package com.maka.flymoney.presentation.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maka.flymoney.domain.model.RoundState

@Composable
fun BetPanel(
    uiState: GameUiState,
    onPlaceBetA: () -> Unit,
    onCashOutA: () -> Unit,
    onCancelBetA: () -> Unit,
    onUpdateAmountA: (String) -> Unit,
    onUpdateAutoCashoutA: (String) -> Unit,
    onPlaceBetB: () -> Unit,
    onCashOutB: () -> Unit,
    onCancelBetB: () -> Unit,
    onUpdateAmountB: (String) -> Unit,
    onUpdateAutoCashoutB: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF13132A))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BetSlot(
                label = "BET A",
                amount = uiState.betAmountA,
                autoCashout = uiState.autoCashoutA,
                activeBet = uiState.activeBetA,
                roundState = uiState.roundState,
                multiplier = uiState.multiplier,
                onPlaceBet = onPlaceBetA,
                onCashOut = onCashOutA,
                onCancelBet = onCancelBetA,
                onUpdateAmount = onUpdateAmountA,
                onUpdateAutoCashout = onUpdateAutoCashoutA,
                modifier = Modifier.weight(1f)
            )
            
            BetSlot(
                label = "BET B",
                amount = uiState.betAmountB,
                autoCashout = uiState.autoCashoutB,
                activeBet = uiState.activeBetB,
                roundState = uiState.roundState,
                multiplier = uiState.multiplier,
                onPlaceBet = onPlaceBetB,
                onCashOut = onCashOutB,
                onCancelBet = onCancelBetB,
                onUpdateAmount = onUpdateAmountB,
                onUpdateAutoCashout = onUpdateAutoCashoutB,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Balance: ₹ ${String.format("%.2f", uiState.walletBalance)}",
            color = Color(0xFF00FF88),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun BetSlot(
    label: String,
    amount: String,
    autoCashout: String,
    activeBet: ActiveBet?,
    roundState: RoundState,
    multiplier: Double,
    onPlaceBet: () -> Unit,
    onCashOut: () -> Unit,
    onCancelBet: () -> Unit,
    onUpdateAmount: (String) -> Unit,
    onUpdateAutoCashout: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAutoEnabled by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        color = Color(0xFF1C1C3A),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("AUTO", color = if (isAutoEnabled) Color(0xFF00FF88) else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Checkbox(
                        checked = isAutoEnabled,
                        onCheckedChange = { isAutoEnabled = it },
                        modifier = Modifier.size(24.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF00FF88),
                            uncheckedColor = Color.Gray
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = onUpdateAmount,
                    modifier = Modifier.weight(1f).height(44.dp),
                    enabled = activeBet == null && roundState == RoundState.WAITING,
                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF0D0D1A),
                        focusedContainerColor = Color(0xFF0D0D1A),
                        disabledContainerColor = Color(0xFF0D0D1A),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color(0xFF00FF88)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Amount", fontSize = 10.sp, color = Color.Gray) }
                )

                if (isAutoEnabled) {
                    OutlinedTextField(
                        value = autoCashout,
                        onValueChange = onUpdateAutoCashout,
                        modifier = Modifier.weight(0.7f).height(44.dp),
                        enabled = activeBet == null && roundState == RoundState.WAITING,
                        textStyle = LocalTextStyle.current.copy(color = Color(0xFF00FF88), fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF0D0D1A),
                            focusedContainerColor = Color(0xFF0D0D1A),
                            disabledContainerColor = Color(0xFF0D0D1A),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color(0xFF00FF88)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        placeholder = { Text("x1.50", fontSize = 10.sp, color = Color.Gray) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val isWaiting = roundState == RoundState.WAITING
            val isRunning = roundState == RoundState.RUNNING
            
            val buttonColor = when {
                activeBet?.cashedOut == true -> Color(0xFF00FF88).copy(alpha = 0.5f)
                activeBet?.isBusted == true -> Color.Red.copy(alpha = 0.5f)
                activeBet != null && isRunning -> Color(0xFFFFCC00) // Cashout color
                activeBet != null && isWaiting -> Color(0xFFFF4444) // Cancel color
                else -> Color(0xFF00FF88) // Place bet color
            }

            Button(
                onClick = {
                    if (activeBet == null) onPlaceBet()
                    else if (isRunning && !activeBet.cashedOut) onCashOut()
                    else if (isWaiting) onCancelBet()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(8.dp),
                enabled = when {
                    activeBet == null -> isWaiting
                    activeBet.cashedOut || activeBet.isBusted -> false
                    else -> true
                }
            ) {
                val text = when {
                    activeBet?.cashedOut == true -> "CASHED OUT\n₹${String.format("%.2f", activeBet.winnings ?: 0.0)}"
                    activeBet?.isBusted == true -> "BUST"
                    activeBet != null && isRunning -> "CASH OUT\n₹${String.format("%.2f", activeBet.amount * multiplier)}"
                    activeBet != null && isWaiting -> "CANCEL"
                    else -> "BET"
                }
                Text(
                    text = text, 
                    fontWeight = FontWeight.Black, 
                    fontSize = 12.sp, 
                    color = Color.Black,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
