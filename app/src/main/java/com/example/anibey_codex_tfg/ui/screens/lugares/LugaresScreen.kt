package com.example.anibey_codex_tfg.ui.screens.lugares

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.anibey_codex_tfg.R
import com.example.anibey_codex_tfg.domain.model.Lugar
import com.example.anibey_codex_tfg.ui.common.component.EmptyScreen
import com.example.anibey_codex_tfg.ui.common.component.ErrorScreen
import com.example.anibey_codex_tfg.ui.common.component.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LugaresScreen(
    viewModel: LugaresViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onLugarClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            LugaresTopBar(onBackClick = onBackClick)
        }
    ) { padding ->
        LugaresScreenContent(
            uiState = uiState,
            searchQuery = searchQuery,
            onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
            onReload = viewModel::recargarLugares,
            getLugaresFiltrados = viewModel::getLugaresFiltrados,
            onLugarClick = onLugarClick,
            padding = padding
        )
    }
}

@Composable
fun LugaresScreenContent(
    uiState: LugaresStates,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onReload: () -> Unit,
    getLugaresFiltrados: () -> List<Lugar>,
    onLugarClick: (String) -> Unit,
    padding: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .paint(
                painter = painterResource(id = R.drawable.fondo_login),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.5f), BlendMode.Darken)
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LugaresSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange
            )

            // Mostrar el estado correspondiente
            when (uiState) {
                is LugaresStates.Loading -> LoadingScreen()
                is LugaresStates.Error -> ErrorScreen(error = uiState.message, onRetry = onReload)
                is LugaresStates.Empty -> EmptyScreen()
                is LugaresStates.Success -> {
                    val filtrados = getLugaresFiltrados()
                    if (filtrados.isEmpty()) {
                        EmptyScreen()
                    } else {
                        LugaresList(
                            lugares = filtrados,
                            onLugarClick = onLugarClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LugaresTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                "LUGARES",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    color = Color.White
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black.copy(alpha = 0.85f)
        )
    )
}
