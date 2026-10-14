package com.ryan.gameshelf.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ryan.gameshelf.Game
import com.ryan.gameshelf.GameItem
import com.ryan.gameshelf.User

@Composable
fun WishlistScreen(
    user: User,
    viewModel: GameView,
    onGameClick: (Game) -> Unit,
    modifier: Modifier = Modifier
) {
    var loadedGames by remember { mutableStateOf<List<Game>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val wishlistIds = user.wishlist.toList()

    LaunchedEffect(wishlistIds) {
        val missingIds = wishlistIds.filter { id -> loadedGames.none { game -> game.id == id } }
        if (missingIds.isNotEmpty()) {
            if (loadedGames.isEmpty()) {
                isLoading = true
            }
            val newGames = viewModel.getGamesByIds(missingIds)
            loadedGames = loadedGames + newGames
            isLoading = false
        }
    }

    val displayedGames = remember(loadedGames, wishlistIds) {
        loadedGames.filter { user.wishlist.contains(it.id) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Ma Liste de Souhaits",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
        } else if (displayedGames.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Votre liste de souhaits est vide",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ajoutez des jeux à votre liste de souhaits avec le bouton signet.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(displayedGames) { _, game ->
                    GameItem(user, game, onGameClick = onGameClick)
                }
            }
        }
    }
}