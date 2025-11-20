package com.example.registroapp.Data.Repository

import android.util.Log
import com.example.registroapp.Di.SupabaseClient
import com.example.registroapp.Domain.model.Comentario
import com.example.registroapp.Domain.model.ComentarioSupabase
import com.example.registroapp.Domain.model.NuevoComentario
import com.example.registroapp.Domain.repository.ComentarioRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order


class ComentarioRepositoryImpl : ComentarioRepository {

    override suspend fun obtenerComentariosPelicula(movieId: Int, userId: String): Result<List<Comentario>> {
        return try {
            Log.d("ComentarioRepo", "Obteniendo comentarios para película: $movieId")

            // Obtener comentarios
            val comentarios = SupabaseClient.client
                .from("comentarios")
                .select {
                    filter {
                        eq("movie_id", movieId)
                    }
                    order("created_at", order = Order.DESCENDING)
                }
                .decodeList<ComentarioSupabase>()

            // Obtener likes del usuario
            val userLikes = try {
                SupabaseClient.client
                    .from("likes_comentarios")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<Map<String, String>>()
            } catch (e: Exception) {
                emptyList()
            }

            // Mapear con el estado de like del usuario
            val comentariosConLikes = comentarios.map { comentario ->
                val userLike = userLikes.find { it["comentario_id"] == comentario.id }
                comentario.toDomain(userLikeStatus = userLike?.get("tipo"))
            }

            Log.d("ComentarioRepo", "Comentarios obtenidos: ${comentariosConLikes.size}")
            Result.success(comentariosConLikes)

        } catch (e: Exception) {
            Log.e("ComentarioRepo", "Error al obtener comentarios: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun obtenerComentariosTema(temaId: String, userId: String): Result<List<Comentario>> {
        return try {
            val comentarios = SupabaseClient.client
                .from("comentarios")
                .select {
                    filter {
                        eq("tema_id", temaId)
                    }
                    order("created_at", order = Order.DESCENDING)
                }
                .decodeList<ComentarioSupabase>()

            val userLikes = try {
                SupabaseClient.client
                    .from("likes_comentarios")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<Map<String, String>>()
            } catch (e: Exception) {
                emptyList()
            }

            val comentariosConLikes = comentarios.map { comentario ->
                val userLike = userLikes.find { it["comentario_id"] == comentario.id }
                comentario.toDomain(userLikeStatus = userLike?.get("tipo"))
            }

            Result.success(comentariosConLikes)

        } catch (e: Exception) {
            Log.e("ComentarioRepo", "Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun agregarComentario(
        userId: String,
        movieId: Int?,
        temaId: String?,
        contenido: String,
        userNombre: String
    ): Result<Comentario> {
        return try {
            Log.d("ComentarioRepo", "Agregando comentario - movieId: $movieId, temaId: $temaId")

            val nuevoComentario = NuevoComentario(
                user_id = userId,
                movie_id = movieId,
                tema_id = temaId,
                contenido = contenido,
                user_nombre = userNombre
            )

            // Insertar sin esperar respuesta decodificada
            SupabaseClient.client
                .from("comentarios")
                .insert(nuevoComentario)

            Log.d("ComentarioRepo", "Comentario insertado exitosamente")

            // Devolver comentario temporal
            val comentarioTemporal = Comentario(
                id = "",
                userId = userId,
                movieId = movieId,
                temaId = temaId,
                contenido = contenido,
                likes = 0,
                dislikes = 0,
                createdAt = "",
                userNombre = userNombre,
                userLikeStatus = null
            )

            Result.success(comentarioTemporal)

        } catch (e: Exception) {
            Log.e("ComentarioRepo", "Error al agregar comentario: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun toggleLike(comentarioId: String, userId: String, tipo: String): Result<Unit> {
        return try {
            // Verificar si ya existe un like
            val existingLike = SupabaseClient.client
                .from("likes_comentarios")
                .select {
                    filter {
                        eq("comentario_id", comentarioId)
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<Map<String, String>>()

            if (existingLike != null) {
                // Si es el mismo tipo, eliminar
                if (existingLike["tipo"] == tipo) {
                    SupabaseClient.client
                        .from("likes_comentarios")
                        .delete {
                            filter {
                                eq("comentario_id", comentarioId)
                                eq("user_id", userId)
                            }
                        }
                } else {
                    // Si es diferente, actualizar
                    SupabaseClient.client
                        .from("likes_comentarios")
                        .update(mapOf("tipo" to tipo)) {
                            filter {
                                eq("comentario_id", comentarioId)
                                eq("user_id", userId)
                            }
                        }
                }
            } else {
                // Insertar nuevo like
                SupabaseClient.client
                    .from("likes_comentarios")
                    .insert(mapOf(
                        "comentario_id" to comentarioId,
                        "user_id" to userId,
                        "tipo" to tipo
                    ))
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("ComentarioRepo", "Error al toggle like: ${e.message}", e)
            Result.failure(e)
        }
    }
}