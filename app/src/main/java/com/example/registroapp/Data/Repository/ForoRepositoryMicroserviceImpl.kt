package com.example.registroapp.Data.Repository

import android.util.Log
import com.example.registroapp.Di.RetrofitInstance
import com.example.registroapp.Domain.model.ForoTema
import com.example.registroapp.Domain.repository.ForoRepository

class ForoRepositoryMicroserviceImpl : ForoRepository {

    private val foroApi = RetrofitInstance.foroApi

    override suspend fun obtenerTemas(): Result<List<ForoTema>> {
        return try {
            Log.d("ForoRepoMicro", "📡 Obteniendo temas del microservicio...")
            val foros = foroApi.getAllForos()
            Log.d("ForoRepoMicro", "✅ Temas obtenidos: ${foros.size}")
            Result.success(foros)
        } catch (e: Exception) {
            Log.e("ForoRepoMicro", "❌ Error: ${e.message}", e)
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
            Log.d("ForoRepoMicro", "📝 Creando tema: '$titulo'")

            val nuevoForo = ForoTema(
                id = "",
                userId = userId,
                titulo = titulo,
                descripcion = descripcion,
                categoria = categoria,
                likes = 0,
                numComentarios = 0,
                createdAt = "",
                nombreUsuario = null,
                fotoUsuario = null
            )

            val foroCreado = foroApi.createForo(nuevoForo)
            Log.d("ForoRepoMicro", "✅ Tema creado con ID: ${foroCreado.id}")
            Result.success(foroCreado)
        } catch (e: Exception) {
            Log.e("ForoRepoMicro", "❌ Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun obtenerTemasPorUsuario(userId: String): Result<List<ForoTema>> {
        return try {
            val todosForos = foroApi.getAllForos()
            val forosUsuario = todosForos.filter { it.userId == userId }
            Result.success(forosUsuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun darLike(temaId: String, userId: String): Result<Unit> {
        return try {
            foroApi.toggleLike(temaId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun quitarLike(temaId: String, userId: String): Result<Unit> {
        return try {
            foroApi.toggleLike(temaId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verificarLike(temaId: String, userId: String): Result<Boolean> {
        return Result.success(false)
    }

    override suspend fun incrementarComentarios(temaId: String): Result<Unit> {
        return Result.success(Unit)
    }
}