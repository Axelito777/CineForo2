package com.example.registroapp.Data.Remote.Api

import com.example.registroapp.Domain.Model.Favorite
import retrofit2.http.*
import java.util.UUID

interface FavoritoApi {

    @GET("api/favoritos/user/{userId}")
    suspend fun getFavoritos(@Path("userId") userId: UUID): List<Favorite>

    @POST("api/favoritos")
    suspend fun addFavorito(@Body favorito: Favorite): Favorite

    @DELETE("api/favoritos/{userId}/{movieId}")
    suspend fun removeFavorito(
        @Path("userId") userId: UUID,
        @Path("movieId") movieId: Int
    )

    @GET("api/favoritos/{userId}/{movieId}/check")
    suspend fun isFavorito(
        @Path("userId") userId: UUID,
        @Path("movieId") movieId: Int
    ): Boolean

    @GET("api/favoritos/user/{userId}/count")
    suspend fun countFavoritos(@Path("userId") userId: UUID): Long
}