package com.example.registroapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.registroapp.ui.theme.RegistroAppTheme
import java.io.File

class PerfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegistroAppTheme {
                PerfilScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen() {
    val ctx = LocalContext.current
    val correo = obtenerSesionActiva(ctx) ?: ""
    var usuario by remember { mutableStateOf(obtenerUsuario(ctx, correo)) }
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }

    // Cargar foto inicial
    LaunchedEffect(usuario) {
        usuario?.fotoPerfil?.takeIf { it != "default" }?.let {
            fotoBitmap = base64ToBitmap(it)
        }
    }

    // Función para actualizar foto
    fun actualizarFoto(bitmap: Bitmap) {
        fotoBitmap = redimensionarImagen(bitmap, 400, 400)
        actualizarFotoPerfil(ctx, correo, bitmapToBase64(fotoBitmap!!))
        usuario = obtenerUsuario(ctx, correo)
        Toast.makeText(ctx, "✅ Foto actualizada", Toast.LENGTH_SHORT).show()
    }

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
                actualizarFoto(corregirOrientacion(ctx, fotoUri!!, bitmap))
            } catch (e: Exception) {
                Toast.makeText(ctx, "Error al capturar foto", Toast.LENGTH_SHORT).show()
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
                actualizarFoto(bitmap)
            } catch (e: Exception) {
                Toast.makeText(ctx, "Error al cargar imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Diálogo foto
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Cambiar foto de perfil", color = CineRojo, fontWeight = FontWeight.Bold) },
            text = {
                Column {
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
                        Text("📷 Tomar nueva foto")
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CineRojo,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { (ctx as? ComponentActivity)?.finish() }) {
                        Text("←", fontSize = 24.sp, color = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(CineNegro, Color.Black.copy(alpha = 0.9f))))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Foto perfil
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(4.dp, CineRojo, CircleShape),
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
                        Text("👤", fontSize = 80.sp, color = Color.White.copy(alpha = 0.5f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { mostrarDialogo = true }) {
                    Text("📷 Cambiar foto de perfil", color = CineDorado, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))

                // Card info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        InfoRow("👤", "Nombre", usuario?.nombre ?: "Sin nombre")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.2f))
                        InfoRow("📧", "Correo", usuario?.correo ?: "Sin correo")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.2f))
                        InfoRow("🎬", "Género favorito", usuario?.generoFavorito ?: "No definido")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.2f))
                        InfoRow("⭐", "Rol", usuario?.rol ?: "Usuario")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botón cerrar sesión
                Button(
                    onClick = {
                        cerrarSesion(ctx)
                        ctx.startActivity(Intent(ctx, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🚪 Cerrar sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Esta información no se puede editar por el momento",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun InfoRow(icono: String, titulo: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(icono, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
        Column {
            Text(titulo, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            Text(valor, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}