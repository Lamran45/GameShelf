package com.ryan.gameshelf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var gamesList by mutableStateOf(listOf<Game>())
        lifecycleScope.launch {
            // Récupération du token
            val authService = AuthService()
            val token = authService.getAccessToken()

            if (token != null) {
                println("SUCCESS TOKEN : Token récupéré avec succès !")
                //Requête de recherche
                val apiService = ApiService()
                val searchResults = apiService.searchGame("Tekken 8", token)
                gamesList = searchResults;

            } else {
                println("ERROR AUTH : Impossible de récupérer le token d'accès.")
            }
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    // Notre Grille de chargement "Lazy" (fainéante)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        // On ajoute un petit espace de 4dp tout autour de la grille
                        contentPadding = PaddingValues(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // "gamesList" est la variable d'état qu'on a défini au-dessus dans le onCreate
                        items(gamesList) { game ->

                            GameItem(game = game)
                        }
                    }
                }
            }
        }
    }
}