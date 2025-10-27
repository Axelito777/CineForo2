package com.example.registroapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.registroapp.ui.theme.RegistroAppTheme

class ForoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegistroAppTheme {
                ForoScreen()
            }
        }
    }
}

// Modelo de datos para Tema
data class Tema(
    val id: String,
    val titulo: String,
    val autor: String,
    val categoria: String,
    val descripcion: String,
    val likes: Int = 0,
    val dislikes: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForoScreen() {
    val context = LocalContext.current
    val correoActual = obtenerSesionActiva(context) ?: ""
    val usuarioActual = obtenerUsuario(context, correoActual)

    var temas by remember { mutableStateOf(obtenerTodosTemas(context)) }

    // Recargar automáticamente cuando la pantalla vuelve a estar visible
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                temas = obtenerTodosTemas(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎬 CineForo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CineRojo,
                    titleContentColor = Color.White
                ),
                actions = {
                    // Botón Favoritos
                    TextButton(onClick = {
                        val intent = Intent(context, FavoritosActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Text("⭐ Favoritos", color = Color.White)
                    }

                    // Botón Cerrar Sesión
                    TextButton(onClick = {
                        cerrarSesion(context)
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }) {
                        Text("Salir", color = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, CrearTemaActivity::class.java)
                    context.startActivity(intent)
                },
                containerColor = CineRojo
            ) {
                Text("➕", fontSize = 24.sp, color = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(CineNegro)
        ) {
            // Bienvenida
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Bienvenido: ${usuarioActual?.nombre ?: "Usuario"}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Rol: ${usuarioActual?.rol ?: "Usuario"}",
                        color = CineDorado,
                        fontSize = 14.sp
                    )
                }
            }

            // Lista de temas
            if (temas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay temas aún\nCrea el primero! 🎬",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(temas) { tema ->
                        TemaCard(
                            tema = tema,
                            onClick = {
                                val intent = Intent(context, DetalleTemaActivity::class.java)
                                intent.putExtra("tema_id", tema.id)
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TemaCard(tema: Tema, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    tema.titulo,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    tema.categoria,
                    color = CineDorado,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                tema.descripcion.take(100) + if (tema.descripcion.length > 100) "..." else "",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Por: ${tema.autor}",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Row {
                    Text(
                        "👍 ${tema.likes}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "👎 ${tema.dislikes}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// Funciones para manejar temas en SharedPreferences

fun guardarTema(context: Context, tema: Tema) {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()

    editor.putString("tema_${tema.id}_titulo", tema.titulo)
    editor.putString("tema_${tema.id}_autor", tema.autor)
    editor.putString("tema_${tema.id}_categoria", tema.categoria)
    editor.putString("tema_${tema.id}_descripcion", tema.descripcion)
    editor.putInt("tema_${tema.id}_likes", tema.likes)
    editor.putInt("tema_${tema.id}_dislikes", tema.dislikes)

    // Agregar ID a la lista de temas
    val temasIds = sharedPref.getStringSet("lista_temas", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    temasIds.add(tema.id)
    editor.putStringSet("lista_temas", temasIds)

    editor.apply()
}

fun obtenerTema(context: Context, id: String): Tema? {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)

    val titulo = sharedPref.getString("tema_${id}_titulo", null) ?: return null
    val autor = sharedPref.getString("tema_${id}_autor", null) ?: return null
    val categoria = sharedPref.getString("tema_${id}_categoria", null) ?: return null
    val descripcion = sharedPref.getString("tema_${id}_descripcion", null) ?: return null
    val likes = sharedPref.getInt("tema_${id}_likes", 0)
    val dislikes = sharedPref.getInt("tema_${id}_dislikes", 0)

    return Tema(id, titulo, autor, categoria, descripcion, likes, dislikes)
}

fun obtenerTodosTemas(context: Context): List<Tema> {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val temasIds = sharedPref.getStringSet("lista_temas", setOf()) ?: setOf()

    return temasIds.mapNotNull { id ->
        obtenerTema(context, id)
    }.sortedByDescending { it.id } // Más recientes primero
}

fun eliminarTema(context: Context, id: String) {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()

    // Eliminar datos del tema
    editor.remove("tema_${id}_titulo")
    editor.remove("tema_${id}_autor")
    editor.remove("tema_${id}_categoria")
    editor.remove("tema_${id}_descripcion")
    editor.remove("tema_${id}_likes")
    editor.remove("tema_${id}_dislikes")

    // Quitar de la lista
    val temasIds = sharedPref.getStringSet("lista_temas", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    temasIds.remove(id)
    editor.putStringSet("lista_temas", temasIds)

    editor.apply()
}