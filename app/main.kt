package com.example.registroapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.registroapp.ui.theme.RegistroAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegistroAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FormularioRegistro()
                }
            }
        }
    }
}

// Colores Duoc UC
val DuocNaranja = Color(0xFFFF6600)
val DuocAzul = Color(0xFF003366)

@Composable
fun FormularioRegistro() {
    var nombreCompleto by remember { mutableStateOf("") }
    var correoElectronico by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var sede by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var datosRegistrados by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Logo Duoc UC (placeholder simple)
        Text(
            text = "DUOC UC",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = DuocAzul,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Título
        Text(
            text = "Registro de Usuario",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DuocAzul,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Campo Nombre Completo
        OutlinedTextField(
            value = nombreCompleto,
            onValueChange = { nombreCompleto = it },
            label = { Text("Nombre Completo") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Campo Correo Electrónico
        OutlinedTextField(
            value = correoElectronico,
            onValueChange = { correoElectronico = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Campo Contraseña
        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Campo Confirmar Contraseña
        OutlinedTextField(
            value = confirmarContrasena,
            onValueChange = { confirmarContrasena = it },
            label = { Text("Confirmar Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Campo Sede
        OutlinedTextField(
            value = sede,
            onValueChange = { sede = it },
            label = { Text("Sede") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Botón Registrar
        Button(
            onClick = {
                // Validación
                when {
                    nombreCompleto.isEmpty() || correoElectronico.isEmpty() ||
                            contrasena.isEmpty() || confirmarContrasena.isEmpty() || sede.isEmpty() -> {
                        mensaje = "Error: Todos los campos son obligatorios"
                        datosRegistrados = ""
                    }
                    contrasena != confirmarContrasena -> {
                        mensaje = "Error: Las contraseñas no coinciden"
                        datosRegistrados = ""
                    }
                    else -> {
                        mensaje = "Registro exitoso"
                        datosRegistrados = "Nombre: $nombreCompleto\nCorreo: $correoElectronico\nSede: $sede"
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DuocNaranja),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Registrar", color = Color.White)
        }

        // Mostrar mensaje
        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                color = if (mensaje.startsWith("Error")) Color.Red else Color.Green,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Mostrar datos si es exitoso
        if (datosRegistrados.isNotEmpty()) {
            Text(
                text = datosRegistrados,
                color = DuocAzul,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FormularioRegistroPreview() {
    RegistroAppTheme {
        FormularioRegistro()
    }
}