package com.example.anibey_codex_tfg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.anibey_codex_tfg.ui.common.theme.AniBey_Codex_TFGTheme
import com.example.anibey_codex_tfg.ui.navigation.AnimaNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val startDestination by viewModel.startDestination.collectAsState()

            AniBey_Codex_TFGTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Usamos remember para que el NavHost no se recree si startDestination cambia después del inicio
                    val initialDestination = remember(startDestination != null) { startDestination }

                    initialDestination?.let { destination ->
                        AnimaNavHost(
                            modifier = Modifier.padding(innerPadding),
                            startDestination = destination
                        )
                    }
                }
            }
        }
    }
}