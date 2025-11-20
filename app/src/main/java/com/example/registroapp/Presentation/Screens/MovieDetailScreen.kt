package com.example.registroapp.Presentation.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.registroapp.Domain.model.Movie
import com.example.registroapp.Presentation.ViewModel.MovieDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Int,
    viewModel: MovieDetailViewModel,
    onBack: () -> Unit,
    onGoToForum: (Movie) -> Unit
) {
    val state = viewModel.state.value
    val cineRojo = Color(0xFFE50914)
    val cineNegro = Color(0xFF141414)
    val cineDorado = Color(0xFFFFD700)

    // Cargar película al iniciar
    LaunchedEffect(movieId) {
        viewModel.loadMovieDetails(movieId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Compartir */ }) {
                        Icon(Icons.Default.Share, "Compartir", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cineRojo,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = cineNegro
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = cineRojo
                    )
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "❌ Error",
                            fontSize = 24.sp,
                            color = cineRojo,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.error,
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                state.movie != null -> {
                    MovieDetailContent(
                        movie = state.movie,
                        isFavorite = state.isFavorite,
                        favoriteLoading = state.favoriteLoading,
                        onToggleFavorite = { viewModel.toggleFavorite() },
                        onGoToForum = onGoToForum,
                        cineRojo = cineRojo,
                        cineNegro = cineNegro,
                        cineDorado = cineDorado
                    )
                }
            }
        }
    }
}

@Composable
fun MovieDetailContent(
    movie: Movie,
    isFavorite: Boolean,
    favoriteLoading: Boolean,
    onToggleFavorite: () -> Unit,
    onGoToForum: (Movie) -> Unit,
    cineRojo: Color,
    cineNegro: Color,
    cineDorado: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Backdrop Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            AsyncImage(
                model = movie.getBackdropUrl(),
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                cineNegro.copy(alpha = 0.7f),
                                cineNegro
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = movie.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = movie.getReleaseYear(),
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = cineRojo.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "⭐ ${String.format("%.1f", movie.voteAverage)}",
                        fontSize = 14.sp,
                        color = cineRojo,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "${movie.voteCount} votos",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (movie.getGenreNames().isNotEmpty()) {
                Text(
                    text = movie.getGenreNames(),
                    fontSize = 14.sp,
                    color = cineDorado,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón Favoritos
                Button(
                    onClick = onToggleFavorite,
                    modifier = Modifier.weight(1f),
                    enabled = !favoriteLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFavorite) cineRojo else Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    if (favoriteLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) Color.White else cineRojo
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isFavorite) "En Favoritos" else "Favorito",
                            color = if (isFavorite) Color.White else Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                // Botón Comentar
                OutlinedButton(
                    onClick = { onGoToForum(movie) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = cineDorado
                    )
                ) {
                    Text("💬 Comentar")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Sinopsis",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (movie.overview.isNotEmpty()) movie.overview else "Sin sinopsis disponible.",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}