package com.example.registroapp.Presentation.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.registroapp.Domain.model.ForoTema
import com.example.registroapp.Presentation.ViewModel.ForoViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.CircularProgressIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForoScreen(
    viewModel: ForoViewModel,
    onTemaClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    val CineRojo = Color(0xFFE50914)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "💬 Foro General",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CineRojo,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = CineRojo
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear tema",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = CineRojo
                    )
                }

                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error ?: "",
                        onRetry = { viewModel.loadTemas() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.temas.isEmpty() -> {
                    EmptyForoState(
                        onCreateClick = { showCreateDialog = true },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.temas) { tema ->
                            TemaCard(
                                tema = tema,
                                onClick = { onTemaClick(tema.id) },
                                onLikeClick = { viewModel.toggleLikeTema(tema.id) },
                                isLiking = uiState.likingTemaIds.contains(tema.id)  // ✅ NUEVO
                            )
                        }
                    }
                }
            }
        }

        // Diálogo para crear tema
        if (showCreateDialog) {
            CreateTemaDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { titulo, descripcion, categoria ->
                    viewModel.createTema(titulo, descripcion, categoria)
                    showCreateDialog = false
                },
                isLoading = uiState.isCreatingTema
            )
        }

        // Snackbar para errores
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearError()
            }
        }
    }
}

@Composable
fun TemaCard(
    tema: ForoTema,
    onClick: () -> Unit,
    onLikeClick: () -> Unit,
    isLiking: Boolean  // ✅ Sin valor por defecto
) {
    val CineRojo = Color(0xFFE50914)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)  // ✅ Solo el Column es clickeable
                .padding(16.dp)
        ) {
            // Header con avatar y usuario
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar (inicial del nombre o email)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CineRojo),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (tema.nombreUsuario?.firstOrNull() ?: tema.userId.firstOrNull() ?: 'U')
                            .uppercaseChar()
                            .toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tema.nombreUsuario ?: tema.userId.substringBefore("@"),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = formatDate(tema.createdAt),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Categoría badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CineRojo.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = tema.categoria,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = CineRojo,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Título del tema
            Text(
                text = tema.titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Descripción
            Text(
                text = tema.descripcion,
                fontSize = 14.sp,
                color = Color.LightGray,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer con stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ✅ Likes con IconButton para evitar propagación
                    IconButton(
                        onClick = onLikeClick,
                        modifier = Modifier.size(40.dp),
                        enabled = !isLiking  // ✅ Deshabilitar mientras procesa
                    ) {
                        if (isLiking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = CineRojo,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ThumbUp,
                                    contentDescription = "Likes",
                                    tint = CineRojo,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = tema.likes.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Comentarios
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = "Comentarios",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tema.numComentarios.toString(),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ver más",
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
fun EmptyForoState(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val CineRojo = Color(0xFFE50914)

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Forum,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color.Gray.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No hay temas aún",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sé el primero en crear un tema de conversación",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateClick,
            colors = ButtonDefaults.buttonColors(containerColor = CineRojo)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear Tema")
        }
    }
}

@Composable
fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val CineRojo = Color(0xFFE50914)

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = CineRojo
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = CineRojo)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTemaDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    isLoading: Boolean
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("General") }
    var expanded by remember { mutableStateOf(false) }

    val categorias = listOf("General", "Recomendaciones", "Debate", "Noticias", "Otros")
    val CineRojo = Color(0xFFE50914)

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                "Crear Nuevo Tema",
                fontWeight = FontWeight.Bold,
                color = CineRojo
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CineRojo,
                        focusedLabelColor = CineRojo
                    )
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CineRojo,
                        focusedLabelColor = CineRojo
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded && !isLoading }
                ) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CineRojo,
                            focusedLabelColor = CineRojo
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    categoria = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(titulo, descripcion, categoria) },
                enabled = !isLoading && titulo.isNotBlank() && descripcion.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CineRojo)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("Crear")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}

private fun formatDate(date: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es"))
        val parsedDate = inputFormat.parse(date)
        outputFormat.format(parsedDate ?: Date())
    } catch (e: Exception) {
        date
    }
}