package com.example.registroapp.Domain.model

data class Movie(
    val id: Int,
    val title: String,
    val originalTitle: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val popularity: Double,
    val adult: Boolean,
    val genreIds: List<Int>  // ✅ No nullable, pero el mapper garantiza lista vacía si es null
) {
    fun getPosterUrl(): String {
        return if (posterPath != null) {
            "https://image.tmdb.org/t/p/w500$posterPath"
        } else {
            ""
        }
    }

    fun getBackdropUrl(): String {
        return if (backdropPath != null) {
            "https://image.tmdb.org/t/p/w1280$backdropPath"
        } else {
            getPosterUrl()
        }
    }

    fun getReleaseYear(): String {
        return if (releaseDate.isNotEmpty()) {
            releaseDate.substring(0, 4)
        } else {
            "N/A"
        }
    }

    fun getGenreNames(): String {
        val genreMap = mapOf(
            28 to "Acción",
            12 to "Aventura",
            16 to "Animación",
            35 to "Comedia",
            80 to "Crimen",
            99 to "Documental",
            18 to "Drama",
            10751 to "Familia",
            14 to "Fantasía",
            36 to "Historia",
            27 to "Terror",
            10402 to "Música",
            9648 to "Misterio",
            10749 to "Romance",
            878 to "Ciencia Ficción",
            10770 to "Película de TV",
            53 to "Thriller",
            10752 to "Bélica",
            37 to "Western"
        )

        return genreIds.mapNotNull { genreMap[it] }.take(3).joinToString(", ")
    }
}