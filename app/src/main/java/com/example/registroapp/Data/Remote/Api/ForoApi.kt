package com.example.registroapp.Data.Remote.Api

import com.example.registroapp.Domain.model.ForoTema
import retrofit2.http.*

interface ForoApi {

    @GET("api/foros")
    suspend fun getAllForos(): List<ForoTema>

    @GET("api/foros/{id}")
    suspend fun getForoById(@Path("id") id: String): ForoTema

    @POST("api/foros")
    suspend fun createForo(@Body foro: ForoTema): ForoTema

    @PUT("api/foros/{id}/like")
    suspend fun toggleLike(@Path("id") id: String): ForoTema

    @DELETE("api/foros/{id}")
    suspend fun deleteForo(@Path("id") id: String)
}