package com.example.registroapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.registroapp.ui.theme.RegistroAppTheme
import java.util.UUID

class DetalleTemaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val temaId = intent.getStringExtra("tema_id") ?: ""

        setContent {
            RegistroAppTheme {
                DetalleTemaScreen(temaId = temaId)
            }
        }
    }
}

// Modelo de datos para Comentario
data class Comentario(
    val id: String,
    val temaId: String,
    val autor: String,
    val texto: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleTemaScreen(temaId: String) {
    val context = LocalContext.current
    val correoActual = obtenerSesionActiva(context) ?: ""
    val usuarioActual = obtenerUsuario(context, correoActual)

    var tema by remember { mutableStateOf(obtenerTema(context, temaId)) }
    var comentarios by remember { mutableStateOf(obtenerComentariosTema(context, temaId)) }
    var nuevoComentario by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var esFavorito by remember { mutableStateOf(verificarFavorito(context, correoActual, temaId)) }

    // Estado del voto del usuario (null, "like", o "dislike")
    var miVoto by remember { mutableStateOf(obtenerVotoUsuario(context, correoActual, temaId)) }

    if (tema == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Tema no encontrado", color = Color.White)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Tema") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CineRojo,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    TextButton(onClick = {
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Text("← Volver", color = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(CineNegro),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Detalle del tema
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                tema!!.titulo,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                tema!!.categoria,
                                color = CineDorado,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Por: ${tema!!.autor}",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            tema!!.descripcion,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Botones de acción
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Like
                    Button(
                        onClick = {
                            val resultado = gestionarVoto(context, correoActual, temaId, "like", miVoto)
                            tema = resultado.first
                            miVoto = resultado.second
                            mensaje = when (miVoto) {
                                "like" -> "👍 Te gusta esto"
                                null -> "Like quitado"
                                else -> "👍 Cambiado a me gusta"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (miVoto == "like")
                                Color(0xFF4CAF50).copy(alpha = 0.6f)
                            else
                                Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Text("👍 ${tema!!.likes}", color = Color.White)
                    }

                    // Dislike
                    Button(
                        onClick = {
                            val resultado = gestionarVoto(context, correoActual, temaId, "dislike", miVoto)
                            tema = resultado.first
                            miVoto = resultado.second
                            mensaje = when (miVoto) {
                                "dislike" -> "👎 No te gusta esto"
                                null -> "Dislike quitado"
                                else -> "👎 Cambiado a no me gusta"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (miVoto == "dislike")
                                Color(0xFFEF5350).copy(alpha = 0.6f)
                            else
                                Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Text("👎 ${tema!!.dislikes}", color = Color.White)
                    }

                    // Favorito
                    Button(
                        onClick = {
                            if (esFavorito) {
                                quitarFavorito(context, correoActual, temaId)
                                esFavorito = false
                                mensaje = "❌ Quitado de favoritos"
                            } else {
                                agregarFavorito(context, correoActual, temaId)
                                esFavorito = true
                                mensaje = "⭐ Agregado a favoritos"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (esFavorito) CineDorado else Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(if (esFavorito) "⭐" else "☆", color = Color.White)
                    }
                }
            }

            // Botón eliminar (solo moderador)
            if (usuarioActual?.rol == "Moderador") {
                item {
                    Button(
                        onClick = {
                            eliminarTema(context, temaId)
                            (context as? ComponentActivity)?.finish()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🗑️ Eliminar Tema (Moderador)")
                    }
                }
            }

            // Mensaje
            if (mensaje.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            mensaje,
                            color = CineDorado,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Sección comentarios
            item {
                Text(
                    "💬 Comentarios (${comentarios.size})",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Agregar comentario
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TextField(
                            value = nuevoComentario,
                            onValueChange = { nuevoComentario = it },
                            label = { Text("Escribe un comentario...") },
                            minLines = 2,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.9f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                focusedIndicatorColor = CineRojo,
                                unfocusedIndicatorColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (nuevoComentario.isEmpty()) {
                                    mensaje = "❌ Escribe algo"
                                } else if (nuevoComentario.length < 3) {
                                    mensaje = "❌ Muy corto (mínimo 3 caracteres)"
                                } else {
                                    val comentario = Comentario(
                                        id = UUID.randomUUID().toString(),
                                        temaId = temaId,
                                        autor = usuarioActual?.nombre ?: "Anónimo",
                                        texto = nuevoComentario
                                    )
                                    guardarComentario(context, comentario)
                                    comentarios = obtenerComentariosTema(context, temaId)
                                    nuevoComentario = ""
                                    mensaje = "✅ Comentario publicado"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CineRojo
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Publicar Comentario")
                        }
                    }
                }
            }

            // Lista de comentarios
            items(comentarios) { comentario ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                comentario.autor,
                                color = CineDorado,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Eliminar comentario (moderador)
                            if (usuarioActual?.rol == "Moderador") {
                                TextButton(
                                    onClick = {
                                        eliminarComentario(context, comentario.id, temaId)
                                        comentarios = obtenerComentariosTema(context, temaId)
                                        mensaje = "🗑️ Comentario eliminado"
                                    }
                                ) {
                                    Text("🗑️", color = Color.Red)
                                }
                            }
                        }

                        Text(
                            comentario.texto,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// NUEVA FUNCIÓN: Gestionar votos (like/dislike) con control de usuario
fun gestionarVoto(
    context: Context,
    correo: String,
    temaId: String,
    nuevoVoto: String, // "like" o "dislike"
    votoActual: String? // null, "like" o "dislike"
): Pair<Tema?, String?> {
    val tema = obtenerTema(context, temaId) ?: return Pair(null, null)
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()
    val prefix = correo.replace("@", "_").replace(".", "_")

    var likes = tema.likes
    var dislikes = tema.dislikes
    var nuevoEstadoVoto: String? = nuevoVoto

    when {
        // Si ya tiene este mismo voto, lo quitamos
        votoActual == nuevoVoto -> {
            if (nuevoVoto == "like") likes-- else dislikes--
            nuevoEstadoVoto = null
            editor.remove("${prefix}_voto_$temaId")
        }
        // Si tiene el voto contrario, lo cambiamos
        votoActual != null && votoActual != nuevoVoto -> {
            if (votoActual == "like") likes-- else dislikes--
            if (nuevoVoto == "like") likes++ else dislikes++
            editor.putString("${prefix}_voto_$temaId", nuevoVoto)
        }
        // Si no tiene voto, agregamos uno nuevo
        else -> {
            if (nuevoVoto == "like") likes++ else dislikes++
            editor.putString("${prefix}_voto_$temaId", nuevoVoto)
        }
    }

    editor.apply()

    // Actualizar tema con los nuevos valores
    val temaActualizado = tema.copy(likes = likes.coerceAtLeast(0), dislikes = dislikes.coerceAtLeast(0))
    guardarTema(context, temaActualizado)

    return Pair(temaActualizado, nuevoEstadoVoto)
}

// NUEVA FUNCIÓN: Obtener el voto actual del usuario
fun obtenerVotoUsuario(context: Context, correo: String, temaId: String): String? {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val prefix = correo.replace("@", "_").replace(".", "_")
    return sharedPref.getString("${prefix}_voto_$temaId", null)
}

// Funciones para comentarios
fun guardarComentario(context: Context, comentario: Comentario) {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()

    editor.putString("comentario_${comentario.id}_temaId", comentario.temaId)
    editor.putString("comentario_${comentario.id}_autor", comentario.autor)
    editor.putString("comentario_${comentario.id}_texto", comentario.texto)

    // Agregar a lista de comentarios del tema
    val comentariosIds = sharedPref.getStringSet("tema_${comentario.temaId}_comentarios", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    comentariosIds.add(comentario.id)
    editor.putStringSet("tema_${comentario.temaId}_comentarios", comentariosIds)

    editor.apply()
}

fun obtenerComentariosTema(context: Context, temaId: String): List<Comentario> {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val comentariosIds = sharedPref.getStringSet("tema_${temaId}_comentarios", setOf()) ?: setOf()

    return comentariosIds.mapNotNull { id ->
        val autor = sharedPref.getString("comentario_${id}_autor", null) ?: return@mapNotNull null
        val texto = sharedPref.getString("comentario_${id}_texto", null) ?: return@mapNotNull null
        Comentario(id, temaId, autor, texto)
    }
}

fun eliminarComentario(context: Context, comentarioId: String, temaId: String) {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()

    editor.remove("comentario_${comentarioId}_temaId")
    editor.remove("comentario_${comentarioId}_autor")
    editor.remove("comentario_${comentarioId}_texto")

    val comentariosIds = sharedPref.getStringSet("tema_${temaId}_comentarios", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    comentariosIds.remove(comentarioId)
    editor.putStringSet("tema_${temaId}_comentarios", comentariosIds)

    editor.apply()
}

// Funciones para favoritos
fun agregarFavorito(context: Context, correo: String, temaId: String) {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()
    val prefix = correo.replace("@", "_").replace(".", "_")

    val favoritos = sharedPref.getStringSet("${prefix}_favoritos", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    favoritos.add(temaId)
    editor.putStringSet("${prefix}_favoritos", favoritos)
    editor.apply()
}

fun quitarFavorito(context: Context, correo: String, temaId: String) {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()
    val prefix = correo.replace("@", "_").replace(".", "_")

    val favoritos = sharedPref.getStringSet("${prefix}_favoritos", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    favoritos.remove(temaId)
    editor.putStringSet("${prefix}_favoritos", favoritos)
    editor.apply()
}

fun verificarFavorito(context: Context, correo: String, temaId: String): Boolean {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val prefix = correo.replace("@", "_").replace(".", "_")
    val favoritos = sharedPref.getStringSet("${prefix}_favoritos", setOf()) ?: setOf()
    return favoritos.contains(temaId)
}