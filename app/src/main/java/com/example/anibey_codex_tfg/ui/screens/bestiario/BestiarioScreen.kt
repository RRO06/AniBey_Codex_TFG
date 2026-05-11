package com.example.anibey_codex_tfg.ui.screens.bestiario

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.example.anibey_codex_tfg.domain.model.Monstruo
import com.example.anibey_codex_tfg.ui.common.FileUtils
import com.example.anibey_codex_tfg.ui.common.component.EmptyScreen
import com.example.anibey_codex_tfg.ui.common.component.ErrorScreen
import com.example.anibey_codex_tfg.ui.common.component.LoadingScreen
import com.example.anibey_codex_tfg.ui.common.theme.GoldAccent
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed
import com.example.anibey_codex_tfg.ui.screens.lugares.LugaresSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BestiarioScreen(
    viewModel: BestiarioViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BESTIARIO", color = Color.White, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0A0A0A)) // Fondo negro profundo
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LugaresSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) }
                )

                when (val state = uiState) {
                    is BestiarioStates.Loading -> LoadingScreen()
                    is BestiarioStates.Error -> ErrorScreen(error = state.message, onRetry = { viewModel.cargarBestiario() })
                    is BestiarioStates.Empty -> EmptyScreen()
                    is BestiarioStates.Success -> BestiarioList(monstruos = state.monstruos)
                }
            }
        }
    }
}

@Composable
fun BestiarioList(monstruos: List<Monstruo>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(monstruos) { monstruo ->
            MonstruoPokedexCard(monstruo = monstruo)
        }
    }
}

@Composable
fun MonstruoPokedexCard(monstruo: Monstruo) {
    // Estilo "Pokedex" de fantasía oscura
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(listOf(PrimaryRed, Color.Black)),
                shape = RoundedCornerShape(16.dp, 4.dp, 16.dp, 4.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151515)),
        shape = RoundedCornerShape(16.dp, 4.dp, 16.dp, 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Parte izquierda: Visualizador de imagen
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .padding(12.dp)
                    .background(Color.Black, RoundedCornerShape(8.dp))
                    .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (monstruo.imagenURL.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = FileUtils.formatDriveUrl(monstruo.imagenURL),
                        contentDescription = monstruo.nombre,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        loading = { CircularProgressIndicator(color = PrimaryRed, modifier = Modifier.size(24.dp)) }
                    )
                }
            }

            // Parte derecha: Datos "técnicos"
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monstruo.nombre.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = PrimaryRed,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                    // Indicador de peligro
                    Box(
                        modifier = Modifier
                            .background(PrimaryRed.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, PrimaryRed, CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NIVEL ${monstruo.nivelPeligro}",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GoldAccent.copy(alpha = 0.3f))

                Text(
                    text = "CATEGORÍA: ${monstruo.categoria}",
                    style = MaterialTheme.typography.labelMedium.copy(color = GoldAccent)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = monstruo.descripcion,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f))

                // Detalles inferiores
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        text = "Habitat: ${monstruo.habitat}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}
