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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.registroapp.ui.theme.RegistroAppTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegistroAppTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    val ctx = LocalContext.current

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
                "Inicia sesión",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            val textFieldColors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.9f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                focusedIndicatorColor = CineRojo,
                unfocusedIndicatorColor = Color.Gray
            )

            TextField(
                correo, { correo = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                clave, { clave = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    mensaje = when {
                        correo.isEmpty() || clave.isEmpty() -> "Completa todos los campos 😐"
                        else -> {
                            val usuario = obtenerUsuario(ctx, correo)
                            if (usuario != null && usuario.clave == clave) {
                                guardarSesion(ctx, correo)
                                ctx.startActivity(Intent(ctx, ForoActivity::class.java))
                                "✅ Bienvenido ${usuario.nombre}!"
                            } else {
                                "❌ Correo o contraseña incorrectos"
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CineRojo),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

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

            TextButton(onClick = { ctx.startActivity(Intent(ctx, MainActivity::class.java)) }) {
                Text("¿No tienes cuenta? Regístrate", color = CineDorado, fontSize = 14.sp)
            }
        }
    }
}

// Funciones de sesión
fun guardarSesion(context: Context, correo: String) {
    context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
        .edit()
        .putString("sesion_activa", correo)
        .apply()
}

fun obtenerSesionActiva(context: Context): String? =
    context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
        .getString("sesion_activa", null)

fun cerrarSesion(context: Context) {
    context.getSharedPreferences("CineForo", Context.MODE_PRIVATE)
        .edit()
        .remove("sesion_activa")
        .apply()
}