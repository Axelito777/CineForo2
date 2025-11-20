package com.example.registroapp.Domain.repository

import com.example.registroapp.Domain.model.ForoTema

interface ForoRepository {

    suspend fun obtenerTemas(): Result<List<ForoTema>>

    suspend fun crearTema(
        userId: String,
        titulo: String,
        descripcion: String,
        categoria: String
    ): Result<ForoTema>

    suspend fun obtenerTemasPorUsuario(userId: String): Result<List<ForoTema>>

    suspend fun darLike(temaId: String, userId: String): Result<Unit>
    suspend fun quitarLike(temaId: String, userId: String): Result<Unit>
    suspend fun verificarLike(temaId: String, userId: String): Result<Boolean>
    suspend fun incrementarComentarios(temaId: String): Result<Unit>
}