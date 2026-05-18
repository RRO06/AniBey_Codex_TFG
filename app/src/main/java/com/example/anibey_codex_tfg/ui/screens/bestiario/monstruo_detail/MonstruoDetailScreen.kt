package com.example.anibey_codex_tfg.ui.screens.bestiario.monstruo_detail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.anibey_codex_tfg.domain.model.Monstruo
import com.example.anibey_codex_tfg.ui.common.FileUtils
import com.example.anibey_codex_tfg.ui.common.component.ErrorScreen
import com.example.anibey_codex_tfg.ui.common.component.LoadingScreen
import com.example.anibey_codex_tfg.ui.common.theme.GoldAccent
import com.example.anibey_codex_tfg.ui.common.theme.PrimaryRed

@Composable
fun MonstruoDetailScreen(
    monstruoId: String,
    viewModel: MonstruoDetailViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(monstruoId) {
        viewModel.getMonstruo(monstruoId)
    }

    when (val state = uiState) {
        is MonstruoDetailStates.Loading -> LoadingScreen()
        is MonstruoDetailStates.Error -> ErrorScreen(error = state.message, onRetry = { viewModel.getMonstruo(monstruoId) })
        is MonstruoDetailStates.Success -> {
            MonstruoDetailContent(
                monstruo = state.monstruo,
                onBackClick = onBackClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonstruoDetailContent(
    monstruo: Monstruo,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "BESTIA",
                        color = Color.Gray
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFF080808) // Casi negro
    ) { padding ->
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Cabecera con Imagen Impactante
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                AsyncImage(
                    model = FileUtils.formatDriveUrl(monstruo.imagenURL, sz = 1200),
                    contentDescription = monstruo.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Degradado para que el texto sea legible y se funda con el fondo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF080808)),
                                startY = 300f
                            )
                        )
                )

                // Etiqueta de Nivel de Peligro flotante
                DangerBadge(
                    level = monstruo.nivelPeligro,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                )
            }

            // Título y Categoría
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = monstruo.nombre.uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp,
                        color = PrimaryRed
                    ),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "— ${monstruo.categoria} —",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontStyle = FontStyle.Italic,
                        color = GoldAccent
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Cuerpo del Compendio
            InfoSection(title = "RELATO ANCESTRAL", iconText = "📜") {
                Text(
                    text = monstruo.descripcion,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp,
                        color = Color.LightGray
                    ),
                    textAlign = TextAlign.Justify
                )
            }

            InfoSection(title = "HABITAT CONOCIDO", iconText = "📍") {
                Text(
                    text = monstruo.habitat,
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                    fontWeight = FontWeight.Bold
                )
            }

            // Habilidades y Debilidades (Dos columnas o vertical)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ListCard(
                    title = "HABILIDADES",
                    items = monstruo.habilidades,
                    color = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
                ListCard(
                    title = "DEBILIDADES",
                    items = monstruo.debilidades,
                    color = PrimaryRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
            
            // Pie de página "Digital"
            Text(
                text = "CODEX ANIBEY - VERSIÓN 1.0.4\nREGISTRO DE PROPIEDAD DE LA HERMANDAD",
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.3f)
                    .padding(bottom = 32.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

@Composable
fun InfoSection(
    title: String,
    iconText: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(iconText, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = GoldAccent
                )
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 0.5.dp,
            color = GoldAccent.copy(alpha = 0.3f)
        )
        content()
    }
}

@Composable
fun ListCard(
    title: String,
    items: List<String>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF121212), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
            color = color
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (items.isEmpty()) {
            Text("Desconocido", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        } else {
            items.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("•", color = color, modifier = Modifier.padding(end = 4.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun DangerBadge(level: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .drawBehind {
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawRect(
                    color = PrimaryRed,
                    style = Stroke(width = 2.dp.toPx(), pathEffect = pathEffect)
                )
            }
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "NIVEL DE PELIGRO",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = level,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = PrimaryRed
                )
            )
        }
    }
}

@Preview
@Composable
fun MonstruoDetailPreview() {
    val mock = Monstruo(
        id = "M-999",
        nombre = "Sombra del Vacío",
        descripcion = "Una entidad que no pertenece a este plano. Se dice que aparece cuando la luz de la luna es tapada por nubes de ceniza. Aquellos que la ven pierden la capacidad de hablar.",
        categoria = "Espectro de Clase V",
        nivelPeligro = "S+",
        habitat = "Dimensiones Fracturadas",
        habilidades = listOf("Invisibilidad", "Drenaje de Alma", "Grito Silencioso"),
        debilidades = listOf("Luz de Altar", "Plata Pura")
    )
    MonstruoDetailContent(monstruo = mock, onBackClick = {})
}
