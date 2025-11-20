package com.example.registroapp.Presentation.Screens

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.registroapp.Presentation.Usuario
import com.example.registroapp.Presentation.base64ToBitmap
import com.example.registroapp.Presentation.obtenerUsuario

@Composable
fun PerfilScreen(correo: String, nombre: String) {
    val context = LocalContext.current
    val usuario = remember { obtenerUsuario(context, correo) }
    var isEditing by remember { mutableStateOf(false) }

    val CineRojo = Color(0xFFE50914)
    val CineNegro = Color(0xFF141414)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Foto de perfil
        val fotoBitmap = usuario?.fotoPerfil?.let {
            if (it != "default") base64ToBitmap(it) else null
        }

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(3.dp, CineRojo, CircleShape)
                .clickable { /* TODO: Cambiar foto */ },
            contentAlignment = Alignment.Center
        ) {
            if (fotoBitmap != null) {
                Image(
                    bitmap = fotoBitmap.asImageBitmap(),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("👤", fontSize = 60.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Información del usuario
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow("Nombre", usuario?.nombre ?: "")
                Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                InfoRow("Correo", usuario?.correo ?: "")
                Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                InfoRow("Género Favorito", usuario?.generoFavorito ?: "")
                Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                InfoRow("Rol", usuario?.rol ?: "Usuario")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón editar
        Button(
            onClick = { isEditing = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CineRojo)
        ) {
            Text("✏️ Editar Perfil", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón cerrar sesión
        OutlinedButton(
            onClick = { /* TODO: Cerrar sesión */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = CineRojo
            )
        ) {
            Text("🚪 Cerrar Sesión", fontSize = 16.sp)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Light
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}