package com.example.registroapp.Data.Repository

import com.example.registroapp.Di.SupabaseClient
import com.example.registroapp.Domain.Model.Favorite
import com.example.registroapp.Domain.Repository.FavoriteRepository
import com.example.registroapp.Utils.Resource
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteDto(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("movie_id")
    val movieId: Int,
    @SerialName("movie_title")
    val movieTitle: String,
    @SerialName("movie_poster")
    val moviePoster: String? = null,
    @SerialName("movie_rating")
    val movieRating: Double? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

class FavoriteRepositoryImpl(
    private val authRepository: AuthRepository
) : FavoriteRepository {

    private val supabase = SupabaseClient.client  // 👈 Usamos el singleton

    override suspend fun addFavorite(
        movieId: Int,
        movieTitle: String,
        posterPath: String?,
        rating: Double
    ): Resource<Unit> {
        return try {
            val result = authRepository.obtenerUsuarioActual()
            val userId = result.getOrNull()?.id
                ?: return Resource.Error("Usuario no autenticado")

            val favoriteDto = FavoriteDto(
                userId = userId,
                movieId = movieId,
                movieTitle = movieTitle,
                moviePoster = posterPath,
                movieRating = rating
            )

            supabase.from("favoritos").insert(favoriteDto)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Error al agregar favorito: ${e.message}")
        }
    }

    override suspend fun removeFavorite(movieId: Int): Resource<Unit> {
        return try {
            val result = authRepository.obtenerUsuarioActual()
            val userId = result.getOrNull()?.id
                ?: return Resource.Error("Usuario no autenticado")

            supabase.from("favoritos").delete {
                filter {
                    eq("user_id", userId)
                    eq("movie_id", movieId)
                }
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Error al eliminar favorito: ${e.message}")
        }
    }

    override suspend fun getFavorites(): Resource<List<Favorite>> {
        return try {
            val result = authRepository.obtenerUsuarioActual()
            val userId = result.getOrNull()?.id
                ?: return Resource.Error("Usuario no autenticado")

            val favorites = supabase.from("favoritos")
                .select(Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<FavoriteDto>()

            val favoriteList = favorites.map {
                Favorite(
                    id = it.id ?: "",
                    userId = it.userId,
                    movieId = it.movieId,
                    movieTitle = it.movieTitle,
                    moviePosterPath = it.moviePoster,
                    movieRating = it.movieRating ?: 0.0,
                    addedAt = it.createdAt ?: ""
                )
            }

            Resource.Success(favoriteList)
        } catch (e: Exception) {
            Resource.Error("Error al obtener favoritos: ${e.message}")
        }
    }

    override suspend fun isFavorite(movieId: Int): Resource<Boolean> {
        return try {
            val result = authRepository.obtenerUsuarioActual()
            val userId = result.getOrNull()?.id
                ?: return Resource.Error("Usuario no autenticado")

            val favorites = supabase.from("favoritos")
                .select(Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                        eq("movie_id", movieId)
                    }
                }
                .decodeList<FavoriteDto>()

            Resource.Success(favorites.isNotEmpty())
        } catch (e: Exception) {
            Resource.Error("Error al verificar favorito: ${e.message}")
        }
    }
}