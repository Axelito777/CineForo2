package com.example.registroapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.registroapp.ui.theme.RegistroAppTheme
import kotlinx.coroutines.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegistroAppTheme {
                RegistroScreen()
            }
        }
    }
}

// Colores del tema cinematográfico
val CineRojo = Color(0xFFE50914)
val CineNegro = Color(0xFF141414)
val CineDorado = Color(0xFFFFD700)

// Modelo de datos Usuario
data class Usuario(
    val nombre: String,
    val correo: String,
    val clave: String,
    val generoFavorito: String,
    val rol: String = "Usuario"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen() {
    // Estados
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmarClave by remember { mutableStateOf("") }
    var generoFavorito by remember { mutableStateOf("Acción") }
    var expanded by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    val context = LocalContext.current
    val generos = listOf("Acción", "Comedia", "Drama", "Terror", "Ciencia Ficción",
        "Romance", "Thriller", "Animación", "Documental")

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagen de fondo con transparencia
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Fondo cinematográfico",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.2f
        )

        // Gradiente oscuro sobre la imagen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.8f),
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // Contenido
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo o icono
            Text(
                text = "🎬",
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "CineForo",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = CineRojo
            )

            Text(
                text = "Tu comunidad de cine",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Campos del formulario
            TextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                    focusedIndicatorColor = CineRojo,
                    unfocusedIndicatorColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                    focusedIndicatorColor = CineRojo,
                    unfocusedIndicatorColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = clave,
                onValueChange = { clave = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                    focusedIndicatorColor = CineRojo,
                    unfocusedIndicatorColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = confirmarClave,
                onValueChange = { confirmarClave = it },
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                    focusedIndicatorColor = CineRojo,
                    unfocusedIndicatorColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown para género favorito
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = generoFavorito,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Género favorito") },
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
                    generos.forEach { genero ->
                        DropdownMenuItem(
                            text = { Text(genero) },
                            onClick = {
                                generoFavorito = genero
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Botón de registro
            Button(
                onClick = {
                    when {
                        nombre.isEmpty() || correo.isEmpty() || clave.isEmpty() || confirmarClave.isEmpty() -> {
                            mensaje = "Todos los campos son obligatorios 😐"
                        }
                        !correo.contains("@") -> {
                            mensaje = "Correo inválido 📧"
                        }
                        clave.length < 6 -> {
                            mensaje = "La contraseña debe tener al menos 6 caracteres 🔒"
                        }
                        clave != confirmarClave -> {
                            mensaje = "Las contraseñas no coinciden 🤯"
                        }
                        else -> {
                            // Guardar usuario
                            guardarUsuario(context, nombre, correo, clave, generoFavorito)
                            mensaje = "✅ Registro exitoso! Redirigiendo al login..."

                            // Navegar al login después de 1.5 segundos
                            GlobalScope.launch {
                                delay(1500)
                                val intent = Intent(context, LoginActivity::class.java)
                                context.startActivity(intent)
                                (context as? ComponentActivity)?.finish()
                            }
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
                    "Registrarse en CineForo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mensaje de validación
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

            Spacer(modifier = Modifier.height(16.dp))

            // Link a login
            TextButton(
                onClick = {
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    (context as? ComponentActivity)?.finish()
                }
            ) {
                Text(
                    text = "¿Ya tienes cuenta? Inicia sesión",
                    color = CineDorado,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Función para guardar usuario en SharedPreferences
fun guardarUsuario(context: Context, nombre: String, correo: String, clave: String, genero: String) {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()

    val prefix = correo.replace("@", "_").replace(".", "_")
    editor.putString("${prefix}_nombre", nombre)
    editor.putString("${prefix}_correo", correo)
    editor.putString("${prefix}_clave", clave)
    editor.putString("${prefix}_genero", genero)
    editor.putString("${prefix}_rol", "Usuario")
    editor.putString("ultimo_usuario", correo)
    editor.apply()
}

// Función para obtener usuario
fun obtenerUsuario(context: Context, correo: String): Usuario? {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val prefix = correo.replace("@", "_").replace(".", "_")

    val nombre = sharedPref.getString("${prefix}_nombre", null)
    val correoGuardado = sharedPref.getString("${prefix}_correo", null)
    val clave = sharedPref.getString("${prefix}_clave", null)
    val genero = sharedPref.getString("${prefix}_genero", null)
    val rol = sharedPref.getString("${prefix}_rol", "Usuario")

    return if (nombre != null && correoGuardado != null && clave != null && genero != null) {
        Usuario(nombre, correoGuardado, clave, genero, rol ?: "Usuario")
    } else {
        null
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroPreview() {
    RegistroAppTheme {
        RegistroScreen()
    }
}