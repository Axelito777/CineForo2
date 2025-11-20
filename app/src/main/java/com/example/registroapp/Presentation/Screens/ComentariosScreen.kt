package com.example.registroapp.Presentation.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.registroapp.Domain.model.Comentario
import com.example.registroapp.Presentation.ViewModel.ComentariosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComentariosScreen(
    movieTitle: String,
    viewModel: ComentariosViewModel,
    onBack: () -> Unit
) {
    val comentarios by viewModel.comentarios.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var nuevoComentario by remember { mutableStateOf("") }
    val CineRojo = Color(0xFFE50914)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comentarios: $movieTitle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CineRojo
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Campo de nuevo comentario
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nuevoComentario,
                    onValueChange = { nuevoComentario = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un comentario...") },
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (nuevoComentario.isNotBlank()) {
                            viewModel.agregarComentario(nuevoComentario)
                            nuevoComentario = ""
                        }
                    },
                    enabled = nuevoComentario.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, "Enviar")
                }
            }

            Divider()

            // Lista de comentarios
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = error ?: "Error desconocido",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { viewModel.cargarComentarios() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                comentarios.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay comentarios aún. ¡Sé el primero en comentar!")
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(comentarios) { comentario ->
                            ComentarioItem(
                                comentario = comentario,
                                onLike = { viewModel.darLike(comentario.id) },
                                onDislike = { viewModel.darDislike(comentario.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComentarioItem(
    comentario: Comentario,
    onLike: () -> Unit,
    onDislike: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comentario.userNombre ?: "Usuario",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = formatearFecha(comentario.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = comentario.contenido,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onLike) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Like",
                            tint = if (comentario.userLikeStatus == "like") {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    Text(
                        text = comentario.likes.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onDislike) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = "Dislike",
                            tint = if (comentario.userLikeStatus == "dislike") {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    Text(
                        text = comentario.dislikes.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

fun formatearFecha(fecha: String): String {
    return try {
        fecha.take(10)
    } catch (e: Exception) {
        "Fecha no válida"
    }
}