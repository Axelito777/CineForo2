package com.example.registroapp.Domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class ForoTema(
    val id: String,
    val userId: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String = "General",
    val likes: Int = 0,
    val numComentarios: Int = 0,
    val createdAt: String,
    val nombreUsuario: String? = null,
    val fotoUsuario: String? = null
)

@Serializable
data class ForoTemaSupabase(
    @SerialName("id") val id: String,
    @SerialName("user_id") val user_id: String,
    @SerialName("titulo") val titulo: String,
    @SerialName("descripcion") val descripcion: String,
    @SerialName("categoria") val categoria: String = "General",
    @SerialName("likes") val likes: Int = 0,
    @SerialName("num_comentarios") val num_comentarios: Int = 0,
    @SerialName("created_at") val created_at: String,
    @SerialName("usuarios") val usuarios: UsuarioForoInfo? = null
) {
    fun toDomain() = ForoTema(
        id = id,
        userId = user_id,
        titulo = titulo,
        descripcion = descripcion,
        categoria = categoria,
        likes = likes,
        numComentarios = num_comentarios,
        createdAt = created_at,
        nombreUsuario = usuarios?.nombre,
        fotoUsuario = usuarios?.foto_perfil_url
    )
}

@Serializable
data class UsuarioForoInfo(
    @SerialName("nombre") val nombre: String?,
    @SerialName("foto_perfil_url") val foto_perfil_url: String?
)

@Serializable
data class NuevoForoTema(
    @SerialName("user_id") val user_id: String,
    @SerialName("titulo") val titulo: String,
    @SerialName("descripcion") val descripcion: String,
    @SerialName("categoria") val categoria: String = "General"
)