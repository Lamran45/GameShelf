package com.ryan.gameshelf
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun GameItem(game: Game, modifier: Modifier = Modifier) {

    Column(
        modifier = modifier
            .padding(4.dp)
            .fillMaxWidth(),// Petite marge entre les cartes
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            val imageUrl = game.cover?.getUrl() ?: "https://images.igdb.com/igdb/image/upload/t_cover_big/nocover.jpg"

            Box(modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
            ) {

                // Jaquette du jeu
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Jaquette de ${game.name}",
                    placeholder = painterResource(id = R.drawable.test_cover),
                    error = painterResource(id = R.drawable.test_cover),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                )

                IconButton(
                    onClick = { game.toggleFav()},
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(35.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF8BADC))
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if(game.favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Add to favorites",
                        tint = Color.White
                    )
                }
            }
            // Titre du jeu
            Text(
                text = game.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1, // Permet d'écrire sur 2 lignes max si le titre est long
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


@Preview(showBackground = true) // showBackground permet de voir la carte sur fond blanc plutôt que noir
@Composable
fun GamePreview() {
    // 1. On crée un faux objet Cover
    val fakeCover = Cover(id = 123, imageId = "co5v9f") // ID d'image IGDB (ex: Tekken 8)

    // 2. On crée le faux jeu (Pas de mot-clé "new", et on utilise 'val')
    val fakeGame = Game(
        name = "Tekken 8",
        summary = "Le dernier né de la saga de jeux de combat légendaire avec des graphismes époustouflants.",
        cover = fakeCover
    )

    // 3. On appelle ton composant en lui passant le faux jeu
    GameItem(game = fakeGame)
}