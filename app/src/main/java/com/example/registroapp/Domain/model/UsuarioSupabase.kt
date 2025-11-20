package com.example.registroapp.Domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsuarioSupabase(
    val id: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    val email: String,
    val nombre: String,
    @SerialName("genero_favorito")
    val generoFavorito: String? = null,
    @SerialName("foto_perfil_url")
    val fotoPerfilUrl: String? = null,
    val rol: String = "Usuario"
)
