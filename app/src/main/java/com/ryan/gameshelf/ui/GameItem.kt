package com.ryan.gameshelf

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ryan.gameshelf.api.UserRepository
import com.ryan.gameshelf.ui.AddToCollectionDialog
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder

@Composable
fun GameItem(
    user: User,
    game: Game,
    modifier: Modifier = Modifier,
    onGameClick: ((Game) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val isInCollection = user.collection.contains(game.id)
    val isFavorite = user.favorites.contains(game.id)
    val isWishlisted = user.wishlist.contains(game.id)
    var showCollectionDialog by remember { mutableStateOf(false) }

    if (showCollectionDialog) {
        AddToCollectionDialog(
            game = game,
            user = user,
            onDismiss = { showCollectionDialog = false },
            onSave = { platform, isPhysical, gameState, borrowed ->
                showCollectionDialog = false
                game.inCollection = true
                coroutineScope.launch {
                    UserRepository.saveUserGameDetails(
                        user = user,
                        gameId = game.id,
                        platform = platform,
                        isPhysical = isPhysical,
                        gameState = gameState,
                        borrowed = borrowed
                    )
                }
            },
            onRemove = {
                showCollectionDialog = false
                game.inCollection = false
                coroutineScope.launch {
                    UserRepository.removeFromCollection(user, game.id)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (onGameClick != null) Modifier.clickable { onGameClick(game) }
                else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            val imageUrl = game.cover?.getUrl() ?: "https://images.igdb.com/igdb/image/upload/t_cover_big/nocover.jpg"

            Box(modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(10.dp))
            ) {

                // Jaquette du jeu
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Jaquette de ${game.name}",
                    placeholder = painterResource(id = R.drawable.test_cover),
                    error = painterResource(id = R.drawable.test_cover),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Bouton d'ajout à la bibliothèque
                IconButton(
                    onClick = { showCollectionDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(35.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3))
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (isInCollection) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = if (isInCollection) "In library" else "Add to library",
                        tint = Color.White
                    )
                }

                // Bouton favoris
                IconButton(
                    onClick = { game.toggleFav(user, coroutineScope) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(35.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF8BADC))
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Add to favorites",
                        tint = Color.White
                    )
                }

                // Bouton Wishlist
                IconButton(
                    onClick = { game.toggleWishlist(user, coroutineScope) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(35.dp)
                        .clip(CircleShape)
                        .background(if (isWishlisted) Color(0xFFFF9800) else Color(0x99000000))
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Wishlist",
                        tint = Color.White
                    )
                }
            }
            // Titre du jeu
            Text(
                text = game.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = game.categoryLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = Color(0xFF575757),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GamePreview() {
    val fakeUser = User("Lamran")
    val fakeCover = Cover(id = 123, imageId = "co5v9f")
    val fakeGame = Game(
        id = 1,
        name = "Tekken 8",
        summary = "Le dernier né de la saga de jeux de combat légendaire avec des graphismes époustouflants.",
        cover = fakeCover
    )
    GameItem(fakeUser, game = fakeGame)
}