package com.example.registroapp.Presentation.Movies

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.registroapp.Data.Repository.MovieRepositoryImpl
import com.example.registroapp.Di.RetrofitInstance
import com.example.registroapp.Domain.model.Movie
import com.example.registroapp.Domain.usecase.GetPopularMoviesUseCase
import com.example.registroapp.Domain.usecase.GetNowPlayingMoviesUseCase
import com.example.registroapp.Domain.usecase.SearchMoviesUseCase
import com.example.registroapp.Utils.Resource
import kotlinx.coroutines.launch

class MoviesViewModel : ViewModel() {

    private val repository = MovieRepositoryImpl(RetrofitInstance.api)
    private val getPopularMoviesUseCase = GetPopularMoviesUseCase(repository)
    private val getNowPlayingMoviesUseCase = GetNowPlayingMoviesUseCase(repository)
    private val searchMoviesUseCase = SearchMoviesUseCase(repository)

    private val _popularMovies = mutableStateOf<Resource<List<Movie>>>(Resource.Loading())
    val popularMovies: State<Resource<List<Movie>>> = _popularMovies

    private val _nowPlayingMovies = mutableStateOf<Resource<List<Movie>>>(Resource.Loading())
    val nowPlayingMovies: State<Resource<List<Movie>>> = _nowPlayingMovies

    private val _searchResults = mutableStateOf<Resource<List<Movie>>>(Resource.Loading())
    val searchResults: State<Resource<List<Movie>>> = _searchResults

    init {
        loadPopularMovies()
        loadNowPlayingMovies()
    }

    fun loadPopularMovies() {
        viewModelScope.launch {
            _popularMovies.value = Resource.Loading()
            _popularMovies.value = getPopularMoviesUseCase()
        }
    }

    fun loadNowPlayingMovies() {
        viewModelScope.launch {
            _nowPlayingMovies.value = Resource.Loading()
            _nowPlayingMovies.value = getNowPlayingMoviesUseCase()
        }
    }

    fun searchMovies(query: String) {
        if (query.isBlank()) {
            _searchResults.value = Resource.Success(emptyList())
            return
        }

        viewModelScope.launch {
            _searchResults.value = Resource.Loading()
            _searchResults.value = searchMoviesUseCase(query)
        }
    }
}