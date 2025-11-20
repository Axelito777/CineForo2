package com.example.registroapp.Data.Repository

import android.util.Log
import com.example.registroapp.Di.SupabaseClient
import com.example.registroapp.Domain.model.ForoTema
import com.example.registroapp.Domain.model.ForoTemaSupabase
import com.example.registroapp.Domain.repository.ForoRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class ForoRepositoryImpl : ForoRepository {

    override suspend fun obtenerTemas(): Result<List<ForoTema>> {
        return try {
            Log.d("ForoRepository", "Obteniendo temas...")

            val response = SupabaseClient.client
                .from("foro_temas")
                .select(
                    columns = Columns.raw("*, usuarios(nombre, foto_perfil_url)")
                ) {
                    order("created_at", order = Order.DESCENDING)
                }
                .decodeList<ForoTemaSupabase>()

            Log.d("ForoRepository", "Temas obtenidos: ${response.size}")
            response.forEach {
                Log.d("ForoRepository", "Tema: ${it.titulo}, Likes: ${it.likes}")
            }

            val temas = response.map { it.toDomain() }
            Result.success(temas)

        } catch (e: Exception) {
            Log.e("ForoRepository", "Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun crearTema(
        userId: String,
        titulo: String,
        descripcion: String,
        categoria: String
    ): Result<ForoTema> {
        return try {
            Log.d("ForoRepository", "📝 Creando tema: '$titulo'")
            Log.d("ForoRepository", "  - userId: $userId")
            Log.d("ForoRepository", "  - categoria: $categoria")

            val nuevoTema = mapOf(
                "user_id" to userId,
                "titulo" to titulo,
                "descripcion" to descripcion,
                "categoria" to categoria
            )

            Log.d("ForoRepository", "📤 Enviando a Supabase...")

            // Insertar el tema
            val insertedTema = SupabaseClient.client
                .from("foro_temas")
                .insert(nuevoTema) {
                    select()
                }
                .decodeSingle<ForoTemaSupabase>()

            Log.d("ForoRepository", "✅ Tema creado con ID: ${insertedTema.id}")

            // Obtener el tema completo con datos del usuario
            val temaCompleto = SupabaseClient.client
                .from("foro_temas")
                .select(
                    columns = Columns.raw("""
                        *,
                        usuarios!inner(nombre, foto_perfil_url)
                    """)
                ) {
                    filter {
                        eq("id", insertedTema.id)
                    }
                }
                .decodeSingle<ForoTemaSupabase>()

            Log.d("ForoRepository", "✅ Tema completo obtenido con datos del usuario")
            Result.success(temaCompleto.toDomain())

        } catch (e: Exception) {
            Log.e("ForoRepository", "❌ Error al crear tema: ${e.message}", e)
            Log.e("ForoRepository", "Stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }

    override suspend fun obtenerTemasPorUsuario(userId: String): Result<List<ForoTema>> {
        return try {
            Log.d("ForoRepository", "📚 Obteniendo temas del usuario: $userId")

            val response = SupabaseClient.client
                .from("foro_temas")
                .select(
                    columns = Columns.raw("""
                        *,
                        usuarios!inner(nombre, foto_perfil_url)
                    """)
                ) {
                    filter {
                        eq("user_id", userId)
                    }
                    order("created_at", order = Order.DESCENDING)
                }
                .decodeList<ForoTemaSupabase>()

            Log.d("ForoRepository", "✅ Temas del usuario obtenidos: ${response.size}")

            val temas = response.map { it.toDomain() }
            Result.success(temas)

        } catch (e: Exception) {
            Log.e("ForoRepository", "❌ Error al obtener temas del usuario: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun darLike(temaId: String, userId: String): Result<Unit> {
        return try {
            Log.d("ForoRepository", "👍 Dando like - temaId: $temaId, userId: $userId")

            val yaLeDioLike = verificarLike(temaId, userId).getOrNull() ?: false

            if (yaLeDioLike) {
                Log.w("ForoRepository", "⚠️ El usuario YA dio like")
                return Result.success(Unit)
            }

            // Insertar el like
            SupabaseClient.client
                .from("foro_tema_likes")
                .insert(
                    mapOf(
                        "tema_id" to temaId,
                        "user_id" to userId
                    )
                )

            // Contar likes totales y actualizar
            val totalLikes = SupabaseClient.client
                .from("foro_tema_likes")
                .select {
                    filter { eq("tema_id", temaId) }
                }
                .decodeList<kotlinx.serialization.json.JsonObject>()
                .size

            SupabaseClient.client
                .from("foro_temas")
                .update(mapOf("likes" to totalLikes)) {
                    filter { eq("id", temaId) }
                }

            Log.d("ForoRepository", "✅ Like agregado. Total: $totalLikes")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("ForoRepository", "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun quitarLike(temaId: String, userId: String): Result<Unit> {
        return try {
            Log.d("ForoRepository", "👎 Quitando like - temaId: $temaId, userId: $userId")

            val yaLeDioLike = verificarLike(temaId, userId).getOrNull() ?: false

            if (!yaLeDioLike) {
                Log.w("ForoRepository", "⚠️ El usuario NO tiene like")
                return Result.success(Unit)
            }

            // Eliminar el like
            SupabaseClient.client
                .from("foro_tema_likes")
                .delete {
                    filter {
                        eq("tema_id", temaId)
                        eq("user_id", userId)
                    }
                }

            // Contar likes totales y actualizar
            val totalLikes = SupabaseClient.client
                .from("foro_tema_likes")
                .select {
                    filter { eq("tema_id", temaId) }
                }
                .decodeList<kotlinx.serialization.json.JsonObject>()
                .size

            SupabaseClient.client
                .from("foro_temas")
                .update(mapOf("likes" to totalLikes)) {
                    filter { eq("id", temaId) }
                }

            Log.d("ForoRepository", "✅ Like eliminado. Total: $totalLikes")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("ForoRepository", "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun verificarLike(temaId: String, userId: String): Result<Boolean> {
        return try {
            Log.d("ForoRepository", "🔍 Verificando like - temaId: $temaId, userId: $userId")

            val like = SupabaseClient.client
                .from("foro_tema_likes")
                .select {
                    filter {
                        eq("tema_id", temaId)
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<Map<String, String>>()

            val hasLike = like != null
            Log.d("ForoRepository", "✅ Usuario ${if (hasLike) "SÍ" else "NO"} tiene like en este tema")
            Result.success(hasLike)

        } catch (e: Exception) {
            Log.e("ForoRepository", "❌ Error al verificar like: ${e.message}", e)
            Log.e("ForoRepository", "Stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }

    override suspend fun incrementarComentarios(temaId: String): Result<Unit> {
        return try {
            Log.d("ForoRepository", "💬 Incrementando comentarios en tema: $temaId")

            // Obtener el tema actual
            val temaActual = SupabaseClient.client
                .from("foro_temas")
                .select {
                    filter {
                        eq("id", temaId)
                    }
                }
                .decodeSingle<ForoTemaSupabase>()

            // Incrementar número de comentarios
            SupabaseClient.client
                .from("foro_temas")
                .update(
                    mapOf(
                        "num_comentarios" to (temaActual.num_comentarios + 1)
                    )
                ) {
                    filter {
                        eq("id", temaId)
                    }
                }

            Log.d("ForoRepository", "✅ Comentarios incrementados: ${temaActual.num_comentarios} → ${temaActual.num_comentarios + 1}")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("ForoRepository", "❌ Error al incrementar comentarios: ${e.message}", e)
            Log.e("ForoRepository", "Stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }
}