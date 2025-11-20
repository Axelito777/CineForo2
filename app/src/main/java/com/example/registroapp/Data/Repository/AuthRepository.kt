package com.example.registroapp.Data.Repository

import com.example.registroapp.Di.SupabaseClient
import com.example.registroapp.Domain.model.UsuarioSupabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.gotrue.user.UserInfo

class AuthRepository {

    private val supabase = SupabaseClient.client

    // ⭐ NUEVO: Obtener ID del usuario actual (para favoritos)
    suspend fun getCurrentUserId(): String? {
        return try {
            supabase.auth.currentSessionOrNull()?.user?.id
        } catch (e: Exception) {
            null
        }
    }

    // Registrar usuario
    suspend fun registrarUsuario(
        email: String,
        password: String,
        nombre: String,
        generoFavorito: String
    ): Result<UsuarioSupabase> {
        return try {
            android.util.Log.d("AUTH", "🔵 1. Iniciando registro para: $email")

            // 1. Registrar en Supabase Auth
            val authResult = supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            android.util.Log.d("AUTH", "✅ 2. Auth exitoso. AuthResult: $authResult")

            // 2. Obtener el ID del usuario creado
            val userId = authResult?.id
            android.util.Log.d("AUTH", "🔵 3. UserId obtenido: $userId")

            if (userId == null) {
                // Intentar obtenerlo de la sesión
                val session = supabase.auth.currentSessionOrNull()
                val sessionUserId = session?.user?.id
                android.util.Log.d("AUTH", "🔵 4. Intentando desde sesión: $sessionUserId")

                if (sessionUserId == null) {
                    throw Exception("No se pudo obtener el ID del usuario")
                }

                // 3. Crear perfil en tabla usuarios
                val usuario = UsuarioSupabase(
                    id = sessionUserId,
                    email = email,
                    nombre = nombre,
                    generoFavorito = generoFavorito
                )

                android.util.Log.d("AUTH", "🔵 5. Insertando usuario en tabla: $usuario")
                supabase.from("usuarios").insert(usuario)
                android.util.Log.d("AUTH", "✅ 6. Usuario insertado exitosamente")

                return Result.success(usuario)
            }

            // 3. Crear perfil en tabla usuarios
            val usuario = UsuarioSupabase(
                id = userId,
                email = email,
                nombre = nombre,
                generoFavorito = generoFavorito
            )

            android.util.Log.d("AUTH", "🔵 5. Insertando usuario en tabla: $usuario")
            supabase.from("usuarios").insert(usuario)
            android.util.Log.d("AUTH", "✅ 6. Usuario insertado exitosamente")

            Result.success(usuario)
        } catch (e: Exception) {
            android.util.Log.e("AUTH", "❌ ERROR: ${e.message}")
            android.util.Log.e("AUTH", "❌ STACK TRACE: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }

    // Iniciar sesión
    suspend fun iniciarSesion(
        email: String,
        password: String
    ): Result<UsuarioSupabase> {
        return try {
            // 1. Login con Supabase Auth
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            // 2. Obtener datos del usuario desde la tabla
            val usuarios = supabase.from("usuarios")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeList<UsuarioSupabase>()

            if (usuarios.isNotEmpty()) {
                Result.success(usuarios.first())
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cerrar sesión
    suspend fun cerrarSesion(): Result<Unit> {
        return try {
            supabase.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener usuario actual
    suspend fun obtenerUsuarioActual(): Result<UsuarioSupabase?> {
        return try {
            val session = supabase.auth.currentSessionOrNull()

            if (session != null) {
                val userId = session.user?.id ?: return Result.success(null)

                val usuarios = supabase.from("usuarios")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("id", userId)
                        }
                    }
                    .decodeList<UsuarioSupabase>()

                if (usuarios.isNotEmpty()) {
                    Result.success(usuarios.first())
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // En AuthRepository.kt, agrega estos métodos:

    // En AuthRepository.kt, reemplaza los métodos con estos:

    suspend fun actualizarPerfil(
        nombre: String,
        generoFavorito: String
    ): Result<Unit> {
        return try {
            val user = supabase.auth.currentUserOrNull()

            if (user != null) {
                // Crear un mapa con los datos a actualizar
                val updates = mapOf(
                    "nombre" to nombre,
                    "genero_favorito" to generoFavorito
                )

                supabase.from("usuarios")
                    .update(updates) {
                        filter {
                            eq("id", user.id)
                        }
                    }

                Result.success(Unit)
            } else {
                Result.failure(Exception("Usuario no autenticado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cambiarPassword(
        passwordActual: String,
        passwordNueva: String
    ): Result<Unit> {
        return try {
            val user = supabase.auth.currentUserOrNull()
            val email = user?.email ?: return Result.failure(Exception("Usuario no autenticado"))

            // Verificar password actual intentando hacer login
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = passwordActual
                }
            } catch (e: Exception) {
                return Result.failure(Exception("Contraseña actual incorrecta"))
            }

            // Actualizar password - usando modifyUser en lugar de updateUser
            supabase.auth.modifyUser {
                this.password = passwordNueva
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun subirFotoPerfil(fotoBase64: String): Result<String> {
        return try {
            val user = supabase.auth.currentUserOrNull()

            if (user != null) {
                val updates = mapOf(
                    "foto_perfil_url" to fotoBase64
                )

                supabase.from("usuarios")
                    .update(updates) {
                        filter {
                            eq("id", user.id)
                        }
                    }

                Result.success(fotoBase64)
            } else {
                Result.failure(Exception("Usuario no autenticado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun obtenerEstadisticas(): Result<Map<String, Int>> {
        return try {
            val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Usuario no autenticado"))

            // Contar favoritos
            val favoritos = try {
                SupabaseClient.client
                    .from("favoritos")
                    .select {
                        filter { eq("user_id", userId) }
                    }
                    .decodeList<Map<String, Any>>()
                    .size
            } catch (e: Exception) {
                0
            }

            // Contar comentarios
            val comentarios = try {
                SupabaseClient.client
                    .from("comentarios")
                    .select {
                        filter { eq("user_id", userId) }
                    }
                    .decodeList<Map<String, Any>>()
                    .size
            } catch (e: Exception) {
                0
            }

            // Contar likes dados
            val likes = try {
                SupabaseClient.client
                    .from("likes_comentarios")
                    .select {
                        filter { eq("user_id", userId) }
                    }
                    .decodeList<Map<String, Any>>()
                    .size
            } catch (e: Exception) {
                0
            }

            Result.success(
                mapOf(
                    "favoritos" to favoritos,
                    "comentarios" to comentarios,
                    "likes" to likes
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}