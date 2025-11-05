package com.example.registroapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Base64
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

// Modelo Usuario
data class Usuario(
    val nombre: String,
    val correo: String,
    val clave: String,
    val generoFavorito: String,
    val rol: String = "Usuario",
    val fotoPerfil: String? = null
)

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
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }

    val ctx = LocalContext.current
    val generos = listOf("Acción", "Comedia", "Drama", "Terror", "Ciencia Ficción",
        "Romance", "Thriller", "Animación", "Documental")

    // Crear URI para foto
    fun crearFotoUri(): Uri? = try {
        FileProvider.getUriForFile(
            ctx,
            "${ctx.packageName}.fileprovider",
            File(ctx.cacheDir, "foto_perfil_${System.currentTimeMillis()}.jpg")
        )
    } catch (e: Exception) {
        Toast.makeText(ctx, "Error al preparar cámara", Toast.LENGTH_SHORT).show()
        null
    }

    // Launcher cámara
    val camaraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && fotoUri != null) {
            try {
                val bitmap = BitmapFactory.decodeStream(ctx.contentResolver.openInputStream(fotoUri!!))
                fotoBitmap = redimensionarImagen(corregirOrientacion(ctx, fotoUri!!, bitmap), 400, 400)
                mensaje = "✅ Foto capturada"
            } catch (e: Exception) {
                mensaje = "Error al capturar foto"
            }
        }
    }

    // Launcher permiso
    val permisoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) {
            crearFotoUri()?.let { uri ->
                fotoUri = uri
                camaraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(ctx, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher galería
    val galeriaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val bitmap = BitmapFactory.decodeStream(ctx.contentResolver.openInputStream(it))
                fotoBitmap = redimensionarImagen(bitmap, 400, 400)
                mensaje = "✅ Imagen seleccionada"
            } catch (e: Exception) {
                mensaje = "Error al cargar imagen"
            }
        }
    }

    // Diálogo foto
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Agregar foto de perfil", color = CineRojo, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("¿Cómo quieres agregar tu foto?")
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            mostrarDialogo = false
                            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                crearFotoUri()?.let { uri ->
                                    fotoUri = uri
                                    camaraLauncher.launch(uri)
                                }
                            } else {
                                permisoLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CineRojo)
                    ) {
                        Text("📷 Tomar foto con cámara")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            mostrarDialogo = false
                            galeriaLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CineDorado)
                    ) {
                        Text("🖼️ Elegir de galería", color = Color.Black)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar", color = CineRojo)
                }
            }
        )
    }

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

            // Foto perfil
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 16.dp)) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(3.dp, CineRojo, CircleShape)
                        .clickable { mostrarDialogo = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (fotoBitmap != null) {
                        Image(
                            bitmap = fotoBitmap!!.asImageBitmap(),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("👤", fontSize = 60.sp, color = Color.White.copy(alpha = 0.5f))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (fotoBitmap != null) "Toca para cambiar foto" else "Toca para agregar foto",
                    color = CineDorado,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { mostrarDialogo = true }
                )
            }

            // Campos de texto
            val textFieldColors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.9f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                focusedIndicatorColor = CineRojo,
                unfocusedIndicatorColor = Color.Gray
            )

            TextField(nombre, { nombre = it }, label = { Text("Nombre completo") }, singleLine = true,
                colors = textFieldColors, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            TextField(correo, { correo = it }, label = { Text("Correo electrónico") }, singleLine = true,
                colors = textFieldColors, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            TextField(clave, { clave = it }, label = { Text("Contraseña") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = textFieldColors, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            TextField(confirmarClave, { confirmarClave = it }, label = { Text("Confirmar contraseña") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = textFieldColors, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown género
            ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
                TextField(
                    generoFavorito, {},
                    readOnly = true,
                    label = { Text("Género favorito") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = textFieldColors,
                    modifier = Modifier.menuAnchor().fillMaxWidth()
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
                    mensaje = when {
                        nombre.isEmpty() || correo.isEmpty() || clave.isEmpty() || confirmarClave.isEmpty() ->
                            "Todos los campos son obligatorios 😐"
                        !correo.contains("@") -> "Correo inválido 📧"
                        clave.length < 6 -> "La contraseña debe tener al menos 6 caracteres 🔒"
                        clave != confirmarClave -> "Las contraseñas no coinciden 🤯"
                        else -> {
                            guardarUsuario(ctx, nombre, correo, clave, generoFavorito, fotoBitmap?.let { bitmapToBase64(it) })
                            GlobalScope.launch {
                                delay(1500)
                                ctx.startActivity(Intent(ctx, LoginActivity::class.java))
                                (ctx as? ComponentActivity)?.finish()
                            }
                            "✅ Registro exitoso! Redirigiendo al login..."
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CineRojo),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Registrarse en CineForo", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                }
            ) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = CineDorado, fontSize = 14.sp)
            }
        }
    }
}

// ========== FUNCIONES AUXILIARES ==========

fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
}

fun base64ToBitmap(base64: String): Bitmap? = try {
    val bytes = Base64.decode(base64, Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
} catch (e: Exception) {
    null
}

fun redimensionarImagen(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    val ratio = minOf(maxWidth.toFloat() / bitmap.width, maxHeight.toFloat() / bitmap.height)
    val newWidth = (bitmap.width * ratio).toInt()
    val newHeight = (bitmap.height * ratio).toInt()
    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

fun corregirOrientacion(context: Context, uri: Uri, bitmap: Bitmap): Bitmap = try {
    val inputStream = context.contentResolver.openInputStream(uri)
    val exif = inputStream?.let { ExifInterface(it) }
    inputStream?.close()

    val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        ?: ExifInterface.ORIENTATION_NORMAL

    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
    }

    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
} catch (e: Exception) {
    bitmap
}

fun guardarUsuario(context: Context, nombre: String, correo: String, clave: String, genero: String, fotoPerfil: String? = null) {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val prefix = correo.replace("@", "_").replace(".", "_")
    sharedPref.edit().apply {
        putString("${prefix}_nombre", nombre)
        putString("${prefix}_correo", correo)
        putString("${prefix}_clave", clave)
        putString("${prefix}_genero", genero)
        putString("${prefix}_rol", "Usuario")
        putString("${prefix}_foto", fotoPerfil ?: "default")
        putString("ultimo_usuario", correo)
        apply()
    }
}

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

fun actualizarFotoPerfil(context: Context, correo: String, fotoBase64: String) {
    val sharedPref = context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
    val prefix = correo.replace("@", "_").replace(".", "_")
    sharedPref.edit().putString("${prefix}_foto", fotoBase64).apply()
}