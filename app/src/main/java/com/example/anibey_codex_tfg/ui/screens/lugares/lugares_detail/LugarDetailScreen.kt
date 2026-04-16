package com.example.anibey_codex_tfg.ui.screens.lugares.lugares_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.anibey_codex_tfg.domain.model.Lugar
import com.example.anibey_codex_tfg.ui.common.FileUtils
import com.example.anibey_codex_tfg.ui.common.component.ErrorScreen
import com.example.anibey_codex_tfg.ui.common.component.LoadingScreen
import com.example.anibey_codex_tfg.ui.common.theme.GoldAccent
import com.example.anibey_codex_tfg.ui.common.theme.OldPaper
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed

@Composable
fun LugarDetailScreen(
    lugarId: String,
    viewModel: LugarDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(lugarId) {
        viewModel.cargarLugar(lugarId)
    }

    Scaffold(
        topBar = { LugarDetailTopBar(onBackClick) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            when (val state = uiState) {
                is LugarDetailState.Loading -> LoadingScreen()
                is LugarDetailState.Error -> ErrorScreen(
                    error = state.message,
                    onRetry = { viewModel.cargarLugar(lugarId) }
                )
                is LugarDetailState.Success -> LugarDetailContent(lugar = state.lugar)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LugarDetailTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                "DETALLES DEL LUGAR",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
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
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
    )
}

@Composable
private fun LugarDetailContent(lugar: Lugar) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        LugarHeaderSection(lugar)
        
        LugarDescriptionCard(lugar)

        if (lugar.personajes.isNotEmpty()) {
            LugarDetailSection(title = "PERSONAJES RELACIONADOS", items = lugar.personajes)
        }
        
        if (lugar.monstruos.isNotEmpty()) {
            LugarDetailSection(title = "BESTIARIO LOCAL", items = lugar.monstruos)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun LugarHeaderSection(lugar: Lugar) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        if (lugar.imagenURL.isNotEmpty()) {
            AsyncImage(
                model = FileUtils.formatDriveUrl(lugar.imagenURL),
                contentDescription = lugar.nombre,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // Gradiente para legibilidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                        startY = 400f
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = lugar.nombre.uppercase(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            )
            if (lugar.region.isNotEmpty()) {
                Text(
                    text = "📍 ${lugar.region}",
                    style = MaterialTheme.typography.titleMedium.copy(color = GoldAccent)
                )
            }
        }
    }
}

@Composable
private fun LugarDescriptionCard(lugar: Lugar) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = OldPaper),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "CRÓNICA Y DESCRIPCIÓN",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = PrimaryRed,
                    fontWeight = FontWeight.Bold
                )
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = PrimaryRed.copy(alpha = 0.3f)
            )
            
            Text(
                text = lugar.descripcion,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Justify,
                    color = Color.Black.copy(alpha = 0.8f)
                )
            )

            if (lugar.tipo.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TIPO: ",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = lugar.tipo,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
private fun LugarDetailSection(title: String, items: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(PrimaryRed)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = item, color = Color.White.copy(alpha = 0.9f))
            }
        }
    }
}
