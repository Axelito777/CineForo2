package com.example.registroapp.Presentation.Movies

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.registroapp.Domain.model.Movie
import com.example.registroapp.Utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    viewModel: MoviesViewModel = viewModel(),
    onMovieClick: (Int) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val CineRojo = Color(0xFFE50914)
    val CineNegro = Color(0xFF141414)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🎬 CineForo",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CineRojo
                )
            )
        },
        containerColor = CineNegro
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Barra de búsqueda
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchMovies(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Buscar películas...", color = Color.Gray) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = CineRojo,
                        focusedIndicatorColor = CineRojo,
                        unfocusedIndicatorColor = Color.Gray
                    )
                )
            }

            // Películas Populares
            item {
                Text(
                    text = "Películas Populares",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            item {
                val popularState = viewModel.popularMovies.value
                when (popularState) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CineRojo)
                        }
                    }
                    is Resource.Success -> {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(popularState.data ?: emptyList()) { movie ->
                                MovieCard(
                                    movie = movie,
                                    onClick = { onMovieClick(movie.id) }
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        Text(
                            text = "Error: ${popularState.message}",
                            color = CineRojo
                        )
                    }
                }
            }

            // En Cartelera
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "En Cartelera",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            item {
                val nowPlayingState = viewModel.nowPlayingMovies.value
                when (nowPlayingState) {
                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CineRojo)
                        }
                    }
                    is Resource.Success -> {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(nowPlayingState.data ?: emptyList()) { movie ->
                                MovieCard(
                                    movie = movie,
                                    onClick = { onMovieClick(movie.id) }
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        Text(
                            text = "Error: ${nowPlayingState.message}",
                            color = CineRojo
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MovieCard(
    movie: Movie,
    onClick: () -> Unit = {}
) {
    val CineRojo = Color(0xFFE50914)

    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Column {
            AsyncImage(
                model = movie.getPosterUrl(),
                contentDescription = movie.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⭐ ${String.format("%.1f", movie.voteAverage)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CineRojo
                    )
                }
            }
        }
    }
}