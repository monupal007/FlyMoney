package com.maka.flymoney.presentation.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.maka.flymoney.domain.model.User

@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val players by viewModel.leaderboardPlayers.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A))
            .padding(16.dp)
    ) {
        Text(
            text = "Leaderboard",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = when (selectedPeriod) {
                "daily" -> 0
                "weekly" -> 1
                else -> 2
            },
            containerColor = Color(0xFF13132A),
            contentColor = Color(0xFF00FF88),
            indicator = { tabPositions ->
                if (tabPositions.isNotEmpty()) {
                    val index = when (selectedPeriod) {
                        "daily" -> 0
                        "weekly" -> 1
                        else -> 2
                    }
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[index]),
                        color = Color(0xFF00FF88)
                    )
                }
            }
        ) {
            Tab(selected = selectedPeriod == "daily", onClick = { viewModel.setPeriod("daily") }) {
                Text("Today", modifier = Modifier.padding(12.dp))
            }
            Tab(selected = selectedPeriod == "weekly", onClick = { viewModel.setPeriod("weekly") }) {
                Text("Weekly", modifier = Modifier.padding(12.dp))
            }
            Tab(selected = selectedPeriod == "allTime", onClick = { viewModel.setPeriod("allTime") }) {
                Text("All Time", modifier = Modifier.padding(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(players) { index, player ->
                LeaderboardRow(rank = index + 1, player = player)
            }
        }
    }
}

@Composable
fun LeaderboardRow(rank: Int, player: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF13132A), shape = MaterialTheme.shapes.medium)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$rank",
            color = if (rank <= 3) Color(0xFFFFD700) else Color(0xFF8888AA),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp)
        )

        AsyncImage(
            model = player.avatarUrl.ifEmpty { "https://via.placeholder.com/150" },
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = player.username,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "₹${String.format("%.2f", player.walletBalance)}",
            color = Color(0xFF00FF88),
            fontWeight = FontWeight.Bold
        )
    }
}
