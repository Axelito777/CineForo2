package com.example.registroapp.Utils

import com.example.registroapp.Data.Remote.Dto.MovieDto
import com.example.registroapp.Domain.model.Movie

fun MovieDto.toMovie(): Movie {
    return Movie(
        id = this.id,
        title = this.title,
        originalTitle = this.originalTitle ?: "",
        overview = this.overview ?: "",
        posterPath = this.posterPath,
        backdropPath = this.backdropPath,
        releaseDate = this.releaseDate ?: "",
        voteAverage = this.voteAverage ?: 0.0,
        voteCount = this.voteCount ?: 0,
        popularity = this.popularity ?: 0.0,
        adult = this.adult ?: false,
        genreIds = this.genreIds ?: emptyList()  // ✅ Maneja null con lista vacía
    )
}