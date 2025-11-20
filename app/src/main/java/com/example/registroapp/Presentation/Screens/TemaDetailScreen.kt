package com.example.registroapp.Presentation.Screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.registroapp.Domain.model.Comentario
import com.example.registroapp.Domain.model.ForoTema
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemaDetailScreen(
    tema: ForoTema,
    comentarios: List<Comentario>,
    isLoading: Boolean,
    error: String?,
    isSubmitting: Boolean,
    isTogglingLike: Boolean = false,  // ✅ NUEVO parámetro
    onBack: () -> Unit,
    onAddComentario: (String) -> Unit,
    onLikeTema: () -> Unit
) {
    var comentarioText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val CineRojo = Color(0xFFE50914)
    val CineNegro = Color(0xFF141414)

    // ✅ Mostrar error si existe
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "💬 Tema del Foro",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CineRojo
                )
            )
        },
        containerColor = CineNegro,
        bottomBar = {
            // Campo para comentar
            Surface(
                color = Color(0xFF1E1E1E),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = comentarioText,
                        onValueChange = { comentarioText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe tu opinión...", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.1f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = CineRojo,
                            focusedBorderColor = CineRojo,
                            unfocusedBorderColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        enabled = !isSubmitting
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (comentarioText.isNotBlank()) {
                                onAddComentario(comentarioText.trim())
                                comentarioText = ""
                                Toast.makeText(context, "Comentario enviado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = comentarioText.isNotBlank() && !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = CineRojo,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Enviar",
                                tint = if (comentarioText.isNotBlank()) CineRojo else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header del tema
                item {
                    TemaHeader(tema = tema, onLike = onLikeTema)
                }

                // Separador
                item {
                    HorizontalDivider(
                        color = Color.Gray.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Título de comentarios
                item {
                    Text(
                        text = "💬 Comentarios (${comentarios.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Estados de carga
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CineRojo)
                        }
                    }
                } else if (error != null) {
                    item {
                        ErrorCard(error = error)
                    }
                } else if (comentarios.isEmpty()) {
                    item {
                        EmptyComentariosCard()
                    }
                } else {
                    // Lista de comentarios
                    items(comentarios) { comentario ->
                        ComentarioTemaCard(comentario = comentario)
                    }
                }
            }
        }
    }
}

@Composable
fun TemaHeader(
    tema: ForoTema,
    onLike: () -> Unit
) {
    val CineRojo = Color(0xFFE50914)
    // ✅ Estado local para prevenir múltiples clicks
    var isLiking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()  // ✅ Scope para coroutines

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Usuario y fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CineRojo),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (tema.nombreUsuario?.firstOrNull() ?: 'U')
                            .uppercaseChar()
                            .toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tema.nombreUsuario ?: "Usuario",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = formatDate(tema.createdAt),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Categoría
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CineRojo.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = tema.categoria,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        color = CineRojo,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Título
            Text(
                text = tema.titulo,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Descripción
            Text(
                text = tema.descripcion,
                fontSize = 15.sp,
                color = Color.LightGray,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Botón de Like
                Button(
                    onClick = {
                        if (!isLiking) {
                            isLiking = true
                            onLike()
                            // ✅ Resetear después de un delay para evitar spam
                            scope.launch {
                                delay(1000)
                                isLiking = false
                            }
                        }
                    },
                    enabled = !isLiking,  // ✅ Deshabilitar mientras procesa
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CineRojo.copy(alpha = 0.2f),
                        disabledContainerColor = CineRojo.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLiking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = CineRojo,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Like",
                            tint = CineRojo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tema.likes.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Contador de comentarios
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comentarios",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${tema.numComentarios} respuestas",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ComentarioTemaCard(comentario: Comentario) {
    val CineRojo = Color(0xFFE50914)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CineRojo.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (comentario.userNombre ?: "U").firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = comentario.userNombre ?: "Usuario",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = formatDate(comentario.createdAt),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contenido
            Text(
                text = comentario.contenido,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun EmptyComentariosCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "💬", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No hay comentarios aún",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sé el primero en comentar",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ErrorCard(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE50914).copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFE50914)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = error,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
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