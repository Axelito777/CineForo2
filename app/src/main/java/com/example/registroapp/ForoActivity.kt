package com.example.registroapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
    val ctx = LocalContext.current
    val correo = obtenerSesionActiva(ctx) ?: ""
    var usuario by remember { mutableStateOf(obtenerUsuario(ctx, correo)) }
    var temas by remember { mutableStateOf(obtenerTodosTemas(ctx)) }

    val fotoBitmap = remember(usuario?.fotoPerfil) {
        usuario?.fotoPerfil?.takeIf { it != "default" }?.let { base64ToBitmap(it) }
    }

    // Recargar al volver a la pantalla
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                temas = obtenerTodosTemas(ctx)
                usuario = obtenerUsuario(ctx, correo)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
                    // Foto perfil
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { ctx.startActivity(Intent(ctx, PerfilActivity::class.java)) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoBitmap != null) {
                            Image(
                                bitmap = fotoBitmap.asImageBitmap(),
                                contentDescription = "Perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("👤", fontSize = 20.sp)
                        }
                    }

                    TextButton(onClick = { ctx.startActivity(Intent(ctx, FavoritosActivity::class.java)) }) {
                        Text("⭐ Favoritos", color = Color.White)
                    }

                    TextButton(onClick = {
                        cerrarSesion(ctx)
                        ctx.startActivity(Intent(ctx, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }) {
                        Text("Salir", color = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { ctx.startActivity(Intent(ctx, CrearTemaActivity::class.java)) },
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
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .border(2.dp, CineDorado, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoBitmap != null) {
                            Image(
                                bitmap = fotoBitmap.asImageBitmap(),
                                contentDescription = "Foto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("👤", fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            "Bienvenido: ${usuario?.nombre ?: "Usuario"}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Rol: ${usuario?.rol ?: "Usuario"}",
                            color = CineDorado,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Lista temas
            if (temas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                        TemaCard(tema) {
                            ctx.startActivity(Intent(ctx, DetalleTemaActivity::class.java).apply {
                                putExtra("tema_id", tema.id)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemaCard(tema: Tema, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
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
                    Text("👍 ${tema.likes}", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("👎 ${tema.dislikes}", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }
    }
}

// Funciones SharedPreferences

fun guardarTema(context: Context, tema: Tema) {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    sharedPref.edit().apply {
        putString("tema_${tema.id}_titulo", tema.titulo)
        putString("tema_${tema.id}_autor", tema.autor)
        putString("tema_${tema.id}_categoria", tema.categoria)
        putString("tema_${tema.id}_descripcion", tema.descripcion)
        putInt("tema_${tema.id}_likes", tema.likes)
        putInt("tema_${tema.id}_dislikes", tema.dislikes)

        val temasIds = sharedPref.getStringSet("lista_temas", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        temasIds.add(tema.id)
        putStringSet("lista_temas", temasIds)
        apply()
    }
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
    return temasIds.mapNotNull { obtenerTema(context, it) }.sortedByDescending { it.id }
}

fun eliminarTema(context: Context, id: String) {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    sharedPref.edit().apply {
        remove("tema_${id}_titulo")
        remove("tema_${id}_autor")
        remove("tema_${id}_categoria")
        remove("tema_${id}_descripcion")
        remove("tema_${id}_likes")
        remove("tema_${id}_dislikes")

        val temasIds = sharedPref.getStringSet("lista_temas", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        temasIds.remove(id)
        putStringSet("lista_temas", temasIds)
        apply()
    }
}