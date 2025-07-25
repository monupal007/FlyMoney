package com.dimts.kmpprojectdemo.android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dimts.kmpprojectdemo.android.companyList.CompanyListScreen
import com.dimts.kmpprojectdemo.android.companyList.MainViewModel
import com.dimts.kmpprojectdemo.di.UiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OutputScreen() {
    val viewModel: MainViewModel = koinViewModel()

    // Trigger fetching once when screen launches
    LaunchedEffect(Unit) {
//        viewModel.fetchNews()
        viewModel.fetchCompanyList()
    }

    val newsState by viewModel.news.collectAsState()
    val companyListState by viewModel.companyList.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .navigationBarsPadding()
            .background(Color.White),
        contentPadding = PaddingValues(top = 30.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "News",
                    style = MaterialTheme.typography.headlineMedium
                )

                when (newsState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }

                    is UiState.Success -> {
                        val news = (newsState as UiState.Success).data
                        Text(
                            text = news.body ?: "No news content",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(all = 10.dp)
                        )
                    }

                    is UiState.Error -> {
                        val message = (newsState as UiState.Error).message
                        Text(
                            text = message,
                            color = Color.Red,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Companies",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        when (companyListState) {
            is UiState.Loading -> {
                item {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }

            is UiState.Success -> {
                val companies = (companyListState as UiState.Success).data
                items(companies) { company ->
                    CompanyListScreen(company)
                }
            }

            is UiState.Error -> {
                item {
                    val message = (companyListState as UiState.Error).message
                    Text(
                        text = message,
                        color = Color.Red,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Preview(name = "HomeScreen")
@Composable
fun OutputScreenPreview() {
    OutputScreen()
}

