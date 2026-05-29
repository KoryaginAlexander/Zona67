package com.retailstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.retailstore.presentation.navigation.RetailStoreNavGraph
import com.retailstore.presentation.theme.RetailStoreTheme
import com.retailstore.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()
            RetailStoreTheme(isDarkTheme = isDarkTheme) {
                RetailStoreNavGraph()
            }
        }
    }
}
