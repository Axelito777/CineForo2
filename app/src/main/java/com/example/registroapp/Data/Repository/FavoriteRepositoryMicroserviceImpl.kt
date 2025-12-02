package com.example.registroapp.Data.Repository

import android.util.Log
import com.example.registroapp.Di.RetrofitInstance
import com.example.registroapp.Domain.Model.Favorite
import com.example.registroapp.Domain.Repository.FavoriteRepository
import com.example.registroapp.Utils.Resource
import java.util.UUID

class FavoriteRepositoryMicroserviceImpl(
    private val getUserId: suspend () -> String?
) : FavoriteRepository {

    private val favoritoApi = RetrofitInstance.favoritoApi

    override suspend fun addFavorite(
        movieId: Int,
        movieTitle: String,
        posterPath: String?,
        rating: Double
    ): Resource<Unit> {
        return try {
            val userIdString = getUserId() ?: return Resource.Error("Usuario no autenticado")
            val userId = UUID.fromString(userIdString)

            Log.d("FavRepoMicro", "📝 Agregando favorito al microservicio: $movieTitle")

            val favorito = Favorite(
                id = "",
                userId = userIdString,
                movieId = movieId,
                movieTitle = movieTitle,
                moviePosterPath = posterPath,
                movieRating = rating,
                addedAt = ""
            )

            favoritoApi.addFavorito(favorito)
            Log.d("FavRepoMicro", "✅ Favorito agregado exitosamente")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("FavRepoMicro", "❌ Error al agregar favorito: ${e.message}", e)
            Resource.Error("Error al agregar favorito: ${e.message}")
        }
    }

    override suspend fun removeFavorite(movieId: Int): Resource<Unit> {
        return try {
            val userIdString = getUserId() ?: return Resource.Error("Usuario no autenticado")
            val userId = UUID.fromString(userIdString)

            Log.d("FavRepoMicro", "🗑️ Eliminando favorito del microservicio: movieId=$movieId")
            favoritoApi.removeFavorito(userId, movieId)
            Log.d("FavRepoMicro", "✅ Favorito eliminado")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("FavRepoMicro", "❌ Error al eliminar: ${e.message}", e)
            Resource.Error("Error al eliminar favorito: ${e.message}")
        }
    }

    override suspend fun getFavorites(): Resource<List<Favorite>> {
        return try {
            val userIdString = getUserId() ?: return Resource.Error("Usuario no autenticado")
            val userId = UUID.fromString(userIdString)

            Log.d("FavRepoMicro", "📡 Obteniendo favoritos del microservicio...")
            val favoritos = favoritoApi.getFavoritos(userId)
            Log.d("FavRepoMicro", "✅ Favoritos obtenidos: ${favoritos.size}")
            Resource.Success(favoritos)
        } catch (e: Exception) {
            Log.e("FavRepoMicro", "❌ Error: ${e.message}", e)
            Resource.Error("Error al obtener favoritos: ${e.message}")
        }
    }

    override suspend fun isFavorite(movieId: Int): Resource<Boolean> {
        return try {
            val userIdString = getUserId() ?: return Resource.Error("Usuario no autenticado")
            val userId = UUID.fromString(userIdString)

            val isFav = favoritoApi.isFavorito(userId, movieId)
            Resource.Success(isFav)
        } catch (e: Exception) {
            Log.e("FavRepoMicro", "❌ Error: ${e.message}", e)
            Resource.Error("Error al verificar favorito: ${e.message}")
        }
    }
}