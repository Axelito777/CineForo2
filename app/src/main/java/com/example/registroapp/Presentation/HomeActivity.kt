package com.example.registroapp.Presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.registroapp.Data.Repository.AuthRepository
import com.example.registroapp.Data.Repository.FavoriteRepositoryImpl
import com.example.registroapp.Data.Repository.MovieRepositoryImpl
import com.example.registroapp.Di.RetrofitInstance
import com.example.registroapp.Di.SupabaseClient
import com.example.registroapp.Domain.usecase.AddFavoriteUseCase
import com.example.registroapp.Domain.usecase.Favorite.IsFavoriteUseCase
import com.example.registroapp.Domain.usecase.Favorite.RemoveFavoriteUseCase
import com.example.registroapp.Domain.usecase.GetMovieDetailsUseCase
import com.example.registroapp.Presentation.Movies.MoviesScreen
import com.example.registroapp.Presentation.Screens.MovieDetailScreen
import com.example.registroapp.Presentation.ViewModel.MovieDetailViewModel
import com.example.registroapp.ui.theme.RegistroAppTheme
import android.widget.Toast
import com.example.registroapp.Presentation.Screens.FavoritosScreen
import com.example.registroapp.Presentation.ViewModel.FavoritosViewModel
import com.example.registroapp.Domain.usecase.Favorite.GetFavoritesUseCase
import com.example.registroapp.Presentation.Screens.ComentariosScreen
import com.example.registroapp.Presentation.ViewModel.ComentariosViewModel
import com.example.registroapp.Data.Repository.ComentarioRepositoryImpl
import com.example.registroapp.Domain.usecase.Comentarios.GetComentariosUseCase
import com.example.registroapp.Domain.usecase.Comentarios.AddComentarioUseCase
import com.example.registroapp.Domain.usecase.Comentarios.ToggleLikeUseCase
import com.example.registroapp.Presentation.Screens.PerfilEditableScreen
import com.example.registroapp.Presentation.ViewModel.PerfilViewModel
import com.example.registroapp.Data.Repository.ForoRepositoryImpl
import com.example.registroapp.Domain.usecase.Foro.CreateForoTemaUseCase
import com.example.registroapp.Domain.usecase.Foro.GetForoTemasUseCase
import com.example.registroapp.Domain.usecase.Foro.ToggleLikeTemaUseCase
import com.example.registroapp.Presentation.Screens.ForoScreen
import com.example.registroapp.Presentation.ViewModel.ForoViewModel
// ✅ IMPORTS NECESARIOS PARA TEMA DETALLE
import com.example.registroapp.Presentation.Screens.TemaDetailScreen
import com.example.registroapp.Presentation.ViewModel.TemaDetailViewModel
import com.example.registroapp.Domain.usecase.Foro.GetComentariosTemaUseCase
import com.example.registroapp.Domain.usecase.Foro.AddComentarioTemaUseCase

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val correo = intent.getStringExtra("correo") ?: ""
        val nombre = intent.getStringExtra("nombre") ?: ""

        setContent {
            RegistroAppTheme {
                HomeScreen(correo = correo, nombre = nombre)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(correo: String, nombre: String) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val CineRojo = Color(0xFFE50914)
    val CineNegro = Color(0xFF141414)

    val items = listOf(
        NavigationItem("Películas", Icons.Default.VideoLibrary, "movies"),
        NavigationItem("Foro", Icons.Default.Chat, "forum"),
        NavigationItem("Favoritos", Icons.Default.Favorite, "favorites"),
        NavigationItem("Perfil", Icons.Default.Person, "profile")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = CineNegro,
                contentColor = Color.White
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                tint = if (selectedItem == index) CineRojo else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                item.label,
                                color = if (selectedItem == index) CineRojo else Color.Gray
                            )
                        },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            navController.navigate(item.route) {
                                popUpTo("movies") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CineRojo,
                            selectedTextColor = CineRojo,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = CineNegro
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "movies",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("movies") {
                MoviesScreen(
                    onMovieClick = { movieId ->
                        navController.navigate("movie_detail/$movieId")
                    }
                )
            }

            composable("forum") {
                val foroRepository = ForoRepositoryImpl()

                val viewModel = remember {
                    ForoViewModel(
                        getForoTemasUseCase = GetForoTemasUseCase(foroRepository),
                        createForoTemaUseCase = CreateForoTemaUseCase(foroRepository),
                        toggleLikeTemaUseCase = ToggleLikeTemaUseCase(foroRepository)
                    )
                }

                ForoScreen(
                    viewModel = viewModel,
                    onTemaClick = { temaId ->
                        navController.navigate("tema_detalle/$temaId")
                    }
                )
            }

            composable("favorites") {
                val authRepository = AuthRepository()
                val favoriteRepository = FavoriteRepositoryImpl(authRepository = authRepository)

                val viewModel = remember {
                    FavoritosViewModel(
                        getFavoritesUseCase = GetFavoritesUseCase(favoriteRepository),
                        removeFavoriteUseCase = RemoveFavoriteUseCase(favoriteRepository)
                    )
                }

                FavoritosScreen(
                    viewModel = viewModel,
                    onMovieClick = { movieId ->
                        navController.navigate("movie_detail/$movieId")
                    }
                )
            }

            composable("profile") {
                val authRepository = AuthRepository()

                val viewModel = remember {
                    PerfilViewModel(authRepository)
                }

                PerfilEditableScreen(
                    viewModel = viewModel,
                    onLogout = {
                        context.startActivity(Intent(context, LoginActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        })
                        (context as? ComponentActivity)?.finish()
                    }
                )
            }

            composable(
                route = "movie_detail/{movieId}",
                arguments = listOf(
                    navArgument("movieId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: return@composable

                val movieRepository = MovieRepositoryImpl(RetrofitInstance.api)
                val authRepository = AuthRepository()
                val favoriteRepository = FavoriteRepositoryImpl(
                    authRepository = authRepository
                )

                val viewModel = remember {
                    MovieDetailViewModel(
                        getMovieDetailsUseCase = GetMovieDetailsUseCase(movieRepository),
                        addFavoriteUseCase = AddFavoriteUseCase(favoriteRepository),
                        removeFavoriteUseCase = RemoveFavoriteUseCase(favoriteRepository),
                        isFavoriteUseCase = IsFavoriteUseCase(favoriteRepository)
                    )
                }

                MovieDetailScreen(
                    movieId = movieId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onGoToForum = { movie ->
                        navController.navigate("comentarios/${movie.id}/${movie.title}")
                    }
                )
            }

            composable(
                route = "comentarios/{movieId}/{movieTitle}",
                arguments = listOf(
                    navArgument("movieId") { type = NavType.IntType },
                    navArgument("movieTitle") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: return@composable
                val movieTitle = backStackEntry.arguments?.getString("movieTitle") ?: ""

                val comentarioRepository = ComentarioRepositoryImpl()

                val viewModel = remember {
                    ComentariosViewModel(
                        movieId = movieId,
                        getComentariosUseCase = GetComentariosUseCase(comentarioRepository),
                        addComentarioUseCase = AddComentarioUseCase(comentarioRepository),
                        toggleLikeUseCase = ToggleLikeUseCase(comentarioRepository)
                    )
                }

                ComentariosScreen(
                    movieTitle = movieTitle,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // NUEVA PANTALLA: Detalle del tema con comentarios
            composable(
                route = "tema_detalle/{temaId}",
                arguments = listOf(navArgument("temaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val temaId = backStackEntry.arguments?.getString("temaId") ?: return@composable
                val foroRepository = ForoRepositoryImpl()
                val comentarioRepository = ComentarioRepositoryImpl()

                val viewModel = remember {
                    TemaDetailViewModel(
                        temaId = temaId,
                        getForoTemasUseCase = GetForoTemasUseCase(foroRepository),
                        getComentariosTemaUseCase = GetComentariosTemaUseCase(comentarioRepository),
                        addComentarioTemaUseCase = AddComentarioTemaUseCase(comentarioRepository),
                        toggleLikeTemaUseCase = ToggleLikeTemaUseCase(foroRepository)
                    )
                }

                val uiState = viewModel.uiState.value

                if (uiState.tema != null) {
                    TemaDetailScreen(
                        tema = uiState.tema,
                        comentarios = uiState.comentarios,
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        isSubmitting = uiState.isSubmitting,
                        isTogglingLike = uiState.isTogglingLike,  // ✅ NUEVO
                        onBack = { navController.popBackStack() },
                        onAddComentario = { contenido -> viewModel.addComentario(contenido) },
                        onLikeTema = { viewModel.toggleLike() }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFE50914))
                    }
                }
            }

        }
    }
}

data class NavigationItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)