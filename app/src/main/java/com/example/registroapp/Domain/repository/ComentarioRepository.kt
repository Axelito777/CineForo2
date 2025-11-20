package com.example.registroapp.Domain.repository

import com.example.registroapp.Domain.model.Comentario

interface ComentarioRepository {
    suspend fun obtenerComentariosPelicula(movieId: Int, userId: String): Result<List<Comentario>>
    suspend fun obtenerComentariosTema(temaId: String, userId: String): Result<List<Comentario>>
    suspend fun agregarComentario(
        userId: String,
        movieId: Int? = null,
        temaId: String? = null,
        contenido: String,
        userNombre: String
    ): Result<Comentario>
    suspend fun toggleLike(comentarioId: String, userId: String, tipo: String): Result<Unit>
}