package com.example.registroapp.Di

import com.example.registroapp.Data.Remote.Api.ForoApi
import com.example.registroapp.Data.Remote.Api.FavoritoApi
import com.example.registroapp.Data.Remote.Api.TmdbApi
import com.example.registroapp.Utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // TMDb API (ya existe)
    private val tmdbRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: TmdbApi by lazy {
        tmdbRetrofit.create(TmdbApi::class.java)
    }

    // MICROSERVICIO - Foros
    private const val FORO_BASE_URL = "http://10.0.2.2:8080/"

    private val foroRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(FORO_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val foroApi: ForoApi by lazy {
        foroRetrofit.create(ForoApi::class.java)
    }

    // MICROSERVICIO - Favoritos
    private const val FAVORITO_BASE_URL = "http://10.0.2.2:8081/"

    private val favoritoRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(FAVORITO_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val favoritoApi: FavoritoApi by lazy {
        favoritoRetrofit.create(FavoritoApi::class.java)
    }
}