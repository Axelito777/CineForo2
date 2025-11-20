package com.example.registroapp.Data.Remote.Dto

import com.google.gson.annotations.SerializedName

data class MovieDto(
    val id: Int,
    val title: String,
    @SerializedName("original_title")
    val originalTitle: String? = "",
    val overview: String? = "",
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String? = "",
    @SerializedName("vote_average")
    val voteAverage: Double? = 0.0,
    @SerializedName("vote_count")
    val voteCount: Int? = 0,
    val popularity: Double? = 0.0,
    val adult: Boolean? = false,
    @SerializedName("genre_ids")
    val genreIds: List<Int>? = null
)

data class MoviesResponse(
    val page: Int,
    val results: List<MovieDto>,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)