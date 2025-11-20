package com.example.registroapp.Presentation.Screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.registroapp.Presentation.ViewModel.PerfilViewModel
import com.example.registroapp.Presentation.base64ToBitmap
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilEditableScreen(
    viewModel: PerfilViewModel,
    onLogout: () -> Unit
) {
    val state = viewModel.state.value
    val context = LocalContext.current

    val cineRojo = Color(0xFFE50914)
    val cineNegro = Color(0xFF141414)
    val cineDorado = Color(0xFFFFD700)

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var generoFavorito by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val generos = listOf(
        "Acción", "Comedia", "Drama", "Terror", "Ciencia Ficción",
        "Romance", "Thriller", "Animación", "Documental"
    )

    // Cargar datos del usuario cuando cambie el estado
    LaunchedEffect(state.usuario) {
        state.usuario?.let { usuario ->
            nombre = usuario.nombre
            generoFavorito = usuario.generoFavorito ?: "Acción"
        }
    }

    // Mostrar mensajes
    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    // Launcher para galería
    val galeriaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = BitmapFactory.decodeStream(
                    context.contentResolver.openInputStream(it)
                )
                viewModel.subirFoto(bitmap)
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher para cámara (PRIMERO)
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && fotoUri != null) {
            try {
                val bitmap = BitmapFactory.decodeStream(
                    context.contentResolver.openInputStream(fotoUri!!)
                )
                viewModel.subirFoto(bitmap)
            } catch (e: Exception) {
                Toast.makeText(context, "Error al capturar foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher para permisos de cámara (DESPUÉS)
    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            // Crear URI para la foto
            val photoFile = File(context.cacheDir, "foto_perfil_${System.currentTimeMillis()}.jpg")
            val photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            fotoUri = photoUri
            cameraLauncher.launch(photoUri) // ✅ CORREGIDO: cameraLauncher en vez de camaraLauncher
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "👤 Mi Perfil",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    if (state.isEditing) {
                        // Botón Guardar
                        IconButton(
                            onClick = {
                                viewModel.actualizarPerfil(nombre, generoFavorito)
                            },
                            enabled = !state.isSaving
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Guardar",
                                tint = cineDorado
                            )
                        }
                        // Botón Cancelar
                        IconButton(
                            onClick = {
                                viewModel.setEditing(false)
                                state.usuario?.let { usuario ->
                                    nombre = usuario.nombre
                                    generoFavorito = usuario.generoFavorito ?: "Acción"
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cancelar",
                                tint = Color.White
                            )
                        }
                    } else {
                        // Botón Editar
                        IconButton(onClick = { viewModel.setEditing(true) }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cineRojo
                )
            )
        },
        containerColor = cineNegro
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = cineRojo
                    )
                }

                state.usuario != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Foto de perfil
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .border(3.dp, cineRojo, CircleShape)
                                .clickable { showPhotoDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            val fotoBitmap = state.usuario.fotoPerfilUrl?.let {
                                if (it != "default") base64ToBitmap(it) else null
                            }

                            if (fotoBitmap != null) {
                                Image(
                                    bitmap = fotoBitmap.asImageBitmap(),
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text("👤", fontSize = 60.sp, color = Color.White.copy(alpha = 0.5f))
                            }

                            // Overlay para cambiar foto
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Cambiar foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Toca para cambiar foto",
                            color = cineDorado,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Campos del perfil
                        val textFieldColors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.1f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                            disabledContainerColor = Color.White.copy(alpha = 0.05f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.White.copy(alpha = 0.5f),
                            focusedIndicatorColor = cineRojo,
                            unfocusedIndicatorColor = Color.Gray,
                            disabledIndicatorColor = Color.Gray.copy(alpha = 0.3f)
                        )

                        // Nombre
                        TextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre") },
                            enabled = state.isEditing && !state.isSaving,
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Person, "Nombre", tint = cineRojo)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email (no editable)
                        TextField(
                            value = state.usuario.email,
                            onValueChange = {},
                            label = { Text("Email") },
                            enabled = false,
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Email, "Email", tint = Color.Gray)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Género favorito
                        ExposedDropdownMenuBox(
                            expanded = expanded && state.isEditing,
                            onExpandedChange = { if (state.isEditing) expanded = !expanded }
                        ) {
                            TextField(
                                value = generoFavorito,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Género favorito") },
                                enabled = state.isEditing && !state.isSaving,
                                trailingIcon = {
                                    if (state.isEditing) {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                    }
                                },
                                colors = textFieldColors,
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                leadingIcon = {
                                    Icon(Icons.Default.Movie, "Género", tint = cineRojo)
                                }
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

                        Spacer(modifier = Modifier.height(32.dp))

                        // Botón cambiar contraseña
                        OutlinedButton(
                            onClick = { showPasswordDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = cineDorado
                            ),
                            enabled = !state.isSaving
                        ) {
                            Icon(Icons.Default.Lock, "Password")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cambiar Contraseña")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Estadísticas
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    "📊 Estadísticas",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                if (state.estadisticas != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        StatCard(
                                            icon = "⭐",
                                            count = state.estadisticas["favoritos"] ?: 0,
                                            label = "Favoritos"
                                        )
                                        StatCard(
                                            icon = "💬",
                                            count = state.estadisticas["comentarios"] ?: 0,
                                            label = "Comentarios"
                                        )
                                        StatCard(
                                            icon = "👍",
                                            count = state.estadisticas["likes"] ?: 0,
                                            label = "Likes"
                                        )
                                    }
                                } else {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.CenterHorizontally),
                                        color = cineRojo
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Botón cerrar sesión
                        Button(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red.copy(alpha = 0.8f)
                            ),
                            enabled = !state.isSaving
                        ) {
                            Icon(Icons.Default.ExitToApp, "Cerrar sesión")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cerrar Sesión")
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            // Loading overlay
            if (state.isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = cineRojo)
                }
            }
        }
    }

    // Diálogo para cambiar foto
    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Cambiar foto de perfil", color = cineRojo, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Button(
                        onClick = {
                            showPhotoDialog = false
                            // Verificar permiso de cámara
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                val photoFile = File(
                                    context.cacheDir,
                                    "foto_perfil_${System.currentTimeMillis()}.jpg"
                                )
                                fotoUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    photoFile
                                )
                                cameraLauncher.launch(fotoUri) // ✅ CORREGIDO
                            } else {
                                permisoLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = cineRojo)
                    ) {
                        Text("📷 Tomar foto")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showPhotoDialog = false
                            galeriaLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = cineDorado)
                    ) {
                        Text("📁 Elegir de galería", color = Color.Black)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoDialog = false }) {
                    Text("Cancelar", color = cineRojo)
                }
            }
        )
    }

    // Diálogo para cambiar contraseña
    if (showPasswordDialog) {
        CambiarPasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { actual, nueva, confirmar ->
                viewModel.cambiarPassword(actual, nueva, confirmar)
                showPasswordDialog = false
            }
        )
    }

    // Diálogo de confirmación para cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.cerrarSesion(onLogout)
                    }
                ) {
                    Text("Sí, cerrar sesión", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ✅ COMPONENTE StatCard FUERA (al mismo nivel que las otras funciones)
@Composable
fun StatCard(icon: String, count: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 32.sp
        )
        Text(
            text = count.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

// ✅ COMPONENTE CambiarPasswordDialog FUERA
@Composable
fun CambiarPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var passwordActual by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Contraseña") },
        text = {
            Column {
                TextField(
                    value = passwordActual,
                    onValueChange = { passwordActual = it },
                    label = { Text("Contraseña actual") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = passwordNueva,
                    onValueChange = { passwordNueva = it },
                    label = { Text("Nueva contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = confirmarPassword,
                    onValueChange = { confirmarPassword = it },
                    label = { Text("Confirmar contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(passwordActual, passwordNueva, confirmarPassword)
                }
            ) {
                Text("Cambiar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}