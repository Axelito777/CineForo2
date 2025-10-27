package com.example.registroapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.registroapp.ui.theme.RegistroAppTheme
import java.util.UUID

class CrearTemaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegistroAppTheme {
                CrearTemaScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearTemaScreen() {
    val context = LocalContext.current
    val correoActual = obtenerSesionActiva(context) ?: ""
    val usuarioActual = obtenerUsuario(context, correoActual)

    var titulo by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Noticias") }
    var descripcion by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    val categorias = listOf("Noticias", "Preguntas", "Eventos")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Tema") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            TextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del tema") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                    focusedIndicatorColor = CineRojo,
                    unfocusedIndicatorColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Categoría
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = categoria,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.9f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                        focusedIndicatorColor = CineRojo,
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
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

            // Descripción
            TextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                minLines = 5,
                maxLines = 10,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                    focusedIndicatorColor = CineRojo,
                    unfocusedIndicatorColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Botón Guardar
            Button(
                onClick = {
                    when {
                        titulo.isEmpty() -> {
                            mensaje = "❌ El título es obligatorio"
                        }
                        titulo.length < 5 -> {
                            mensaje = "❌ El título debe tener al menos 5 caracteres"
                        }
                        descripcion.isEmpty() -> {
                            mensaje = "❌ La descripción es obligatoria"
                        }
                        descripcion.length < 10 -> {
                            mensaje = "❌ La descripción debe tener al menos 10 caracteres"
                        }
                        else -> {
                            val nuevoTema = Tema(
                                id = UUID.randomUUID().toString(),
                                titulo = titulo,
                                autor = usuarioActual?.nombre ?: "Anónimo",
                                categoria = categoria,
                                descripcion = descripcion
                            )
                            guardarTema(context, nuevoTema)
                            mensaje = "✅ Tema creado exitosamente!"

                            // Volver al foro después de 1 segundo
                            (context as? ComponentActivity)?.finish()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CineRojo,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Publicar Tema",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Mensaje
            if (mensaje.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (mensaje.startsWith("✅"))
                            Color(0xFF2E7D32).copy(alpha = 0.2f)
                        else
                            Color(0xFFC62828).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = mensaje,
                        fontSize = 16.sp,
                        color = if (mensaje.startsWith("✅")) Color(0xFF4CAF50) else Color(0xFFEF5350),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
