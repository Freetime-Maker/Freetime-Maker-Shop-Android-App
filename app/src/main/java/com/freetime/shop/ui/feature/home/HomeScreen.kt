package com.freetime.shop.ui.feature.home

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = koinViewModel()) {
    val uiState = viewModel.uiState.collectAsState().value

    when (uiState) {
        is HomeScreenUIEvents.Loading -> {
            CircularProgressIndicator()
        }

        is HomeScreenUIEvents.Success -> {
            val data = (uiState as HomeScreenUIEvents.Success).data
            LazyColumn {

            }
        }
    }
}