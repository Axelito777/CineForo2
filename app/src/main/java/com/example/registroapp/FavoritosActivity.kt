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

class FavoritosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegistroAppTheme {
                FavoritosScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen() {
    val context = LocalContext.current
    val correoActual = obtenerSesionActiva(context) ?: ""

    var temasFavoritos by remember { mutableStateOf(obtenerFavoritos(context, correoActual)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⭐ Mis Favoritos") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(CineNegro)
        ) {
            if (temasFavoritos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No tienes favoritos aún\n⭐ Agrega temas desde el detalle",
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
                    items(temasFavoritos) { tema ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        tema.titulo,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        tema.categoria,
                                        color = CineDorado,
                                        fontSize = 12.sp
                                    )
                                }

                                Row {
                                    // Ver detalle
                                    TextButton(
                                        onClick = {
                                            val intent = Intent(context, DetalleTemaActivity::class.java)
                                            intent.putExtra("tema_id", tema.id)
                                            context.startActivity(intent)
                                        }
                                    ) {
                                        Text("Ver", color = CineDorado)
                                    }

                                    // Quitar de favoritos
                                    TextButton(
                                        onClick = {
                                            quitarFavorito(context, correoActual, tema.id)
                                            temasFavoritos = obtenerFavoritos(context, correoActual)
                                        }
                                    ) {
                                        Text("❌", fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun obtenerFavoritos(context: Context, correo: String): List<Tema> {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val prefix = correo.replace("@", "_").replace(".", "_")
    val favoritosIds = sharedPref.getStringSet("${prefix}_favoritos", setOf()) ?: setOf()

    return favoritosIds.mapNotNull { id ->
        obtenerTema(context, id)
    }
}