package com.example.registroapp.Presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.registroapp.Data.Repository.ForoRepositoryImpl
import com.example.registroapp.Domain.usecase.Foro.CreateForoTemaUseCase
import com.example.registroapp.Domain.usecase.Foro.GetForoTemasUseCase
import com.example.registroapp.Domain.usecase.Foro.ToggleLikeTemaUseCase
import com.example.registroapp.Presentation.Screens.ForoScreen
import com.example.registroapp.Presentation.ViewModel.ForoViewModel
import com.example.registroapp.ui.theme.RegistroAppTheme
import android.widget.Toast

class ForoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val foroRepository = ForoRepositoryImpl()

        val viewModel = ForoViewModel(
            getForoTemasUseCase = GetForoTemasUseCase(foroRepository),
            createForoTemaUseCase = CreateForoTemaUseCase(foroRepository),
            toggleLikeTemaUseCase = ToggleLikeTemaUseCase(foroRepository)
        )

        setContent {
            RegistroAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ForoScreen(
                        viewModel = viewModel,
                        onTemaClick = { temaId ->
                            Toast.makeText(this, "Tema: $temaId", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}