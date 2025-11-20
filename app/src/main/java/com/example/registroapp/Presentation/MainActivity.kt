package com.example.registroapp.Presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.example.registroapp.Data.Repository.AuthRepository
import com.example.registroapp.R
import com.example.registroapp.ui.theme.RegistroAppTheme
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File

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

// Colores del tema
val CineRojo = Color(0xFFE50914)
val CineNegro = Color(0xFF141414)
val CineDorado = Color(0xFFFFD700)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen() {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmarClave by remember { mutableStateOf("") }
    var generoFavorito by remember { mutableStateOf("Acción") }
    var expanded by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }

    val generos = listOf("Acción", "Comedia", "Drama", "Terror", "Ciencia Ficción",
        "Romance", "Thriller", "Animación", "Documental")

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.2f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.8f),
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎬", fontSize = 64.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text("CineForo", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = CineRojo)
            Text(
                "Tu comunidad de cine",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Campos de texto
            val textFieldColors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.9f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                focusedIndicatorColor = CineRojo,
                unfocusedIndicatorColor = Color.Gray
            )

            TextField(nombre, { nombre = it }, label = { Text("Nombre completo") }, singleLine = true,
                colors = textFieldColors, modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
            Spacer(modifier = Modifier.height(16.dp))

            TextField(correo, { correo = it }, label = { Text("Correo electrónico") }, singleLine = true,
                colors = textFieldColors, modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
            Spacer(modifier = Modifier.height(16.dp))

            TextField(clave, { clave = it }, label = { Text("Contraseña") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = textFieldColors, modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
            Spacer(modifier = Modifier.height(16.dp))

            TextField(confirmarClave, { confirmarClave = it }, label = { Text("Confirmar contraseña") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = textFieldColors, modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown género
            ExposedDropdownMenuBox(expanded, { expanded = !expanded && !isLoading }) {
                TextField(
                    generoFavorito, {},
                    readOnly = true,
                    label = { Text("Género favorito") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = textFieldColors,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    enabled = !isLoading
                )

                ExposedDropdownMenu(expanded, { expanded = false }) {
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

            // Botón registrar
            Button(
                onClick = {
                    when {
                        nombre.isEmpty() || correo.isEmpty() || clave.isEmpty() || confirmarClave.isEmpty() ->
                            mensaje = "Todos los campos son obligatorios 😐"
                        !correo.contains("@") -> mensaje = "Correo inválido 📧"
                        clave.length < 6 -> mensaje = "La contraseña debe tener al menos 6 caracteres 🔒"
                        clave != confirmarClave -> mensaje = "Las contraseñas no coinciden 🤯"
                        else -> {
                            isLoading = true
                            mensaje = "Registrando usuario..."

                            scope.launch {
                                val result = authRepository.registrarUsuario(
                                    email = correo,
                                    password = clave,
                                    nombre = nombre,
                                    generoFavorito = generoFavorito
                                )

                                result.fold(
                                    onSuccess = {
                                        mensaje = "✅ Registro exitoso! Redirigiendo..."
                                        delay(1500)
                                        ctx.startActivity(Intent(ctx, LoginActivity::class.java))
                                        (ctx as? ComponentActivity)?.finish()
                                    },
                                    onFailure = { error ->
                                        mensaje = "❌ Error: ${error.message}"
                                        isLoading = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CineRojo),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Registrarse en CineForo", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mensaje
            if (mensaje.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (mensaje.startsWith("✅"))
                            Color(0xFF2E7D32).copy(alpha = 0.2f) else Color(0xFFC62828).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        mensaje,
                        fontSize = 16.sp,
                        color = if (mensaje.startsWith("✅")) Color(0xFF4CAF50) else Color(0xFFEF5350),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    ctx.startActivity(Intent(ctx, LoginActivity::class.java))
                    (ctx as? ComponentActivity)?.finish()
                },
                enabled = !isLoading
            ) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = CineDorado, fontSize = 14.sp)
            }
        }
    }
}

// Mantén las funciones auxiliares existentes (no las eliminamos por si acaso)
fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    return android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT)
}

fun base64ToBitmap(base64: String): Bitmap? = try {
    val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
} catch (e: Exception) {
    null
}

data class Usuario(
    val nombre: String,
    val correo: String,
    val clave: String,
    val generoFavorito: String,
    val rol: String = "Usuario",
    val fotoPerfil: String? = null
)
// ========== FUNCIONES DE COMPATIBILIDAD (SharedPreferences) ==========
// Mantener hasta migrar todas las activities

fun obtenerSesionActiva(context: Context): String? =
    context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
        .getString("sesion_activa", null)

fun obtenerUsuario(context: Context, correo: String): Usuario? {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val prefix = correo.replace("@", "_").replace(".", "_")
    val nombre = sharedPref.getString("${prefix}_nombre", null)
    val correoGuardado = sharedPref.getString("${prefix}_correo", null)
    val clave = sharedPref.getString("${prefix}_clave", null)
    val genero = sharedPref.getString("${prefix}_genero", null)
    val rol = sharedPref.getString("${prefix}_rol", "Usuario") ?: "Usuario"
    val foto = sharedPref.getString("${prefix}_foto", "default")

    return if (nombre != null && correoGuardado != null && clave != null && genero != null) {
        Usuario(nombre, correoGuardado, clave, genero, rol, foto)
    } else null
}

fun guardarSesion(context: Context, correo: String) {
    context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
        .edit()
        .putString("sesion_activa", correo)
        .apply()
}

fun cerrarSesion(context: Context) {
    context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
        .edit()
        .remove("sesion_activa")
        .apply()
}

fun actualizarFotoPerfil(context: Context, correo: String, fotoBase64: String) {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val prefix = correo.replace("@", "_").replace(".", "_")
    sharedPref.edit().putString("${prefix}_foto", fotoBase64).apply()
}