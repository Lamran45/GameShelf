package com.ryan.gameshelf.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TypeSpecimen
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ryan.gameshelf.Game
import com.ryan.gameshelf.R
import com.ryan.gameshelf.User

@SuppressLint("DefaultLocale")
@Composable
fun GameDetailScreen(
    game: Game,
    user: User,
    onBackClick: () -> Unit,
    onGameGroupClick: ((String, List<Long>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val blueAccent = Color(0xFF2196F3)
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val pinkAccent = Color(0xFFF8BADC)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // --- Barre supérieure avec bouton Retour ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .border(BorderStroke(1.5.dp, blueAccent), CircleShape)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Détails du jeu",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // --- Jaquette du jeu ---
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(190.dp)
                .aspectRatio(0.72f)
                .border(BorderStroke(2.dp, blueAccent), RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
        ) {
            val imageUrl = game.cover?.getUrl() ?: "https://images.igdb.com/igdb/image/upload/t_cover_big/nocover.jpg"
            AsyncImage(
                model = imageUrl,
                contentDescription = "Jaquette de ${game.name}",
                placeholder = painterResource(id = R.drawable.test_cover),
                error = painterResource(id = R.drawable.test_cover),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Titre et Sous-titre ---
        Text(
            text = game.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "${game.categoryLabel} • ${game.formattedReleaseDate}",
            fontSize = 14.sp,
            color = Color(0xFFB0BEC5),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = blueAccent,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = String.format("%.1f", game.rating) + "%",
                color = blueAccent,
                fontWeight = FontWeight.Bold
            )
        }

        val isInCollection = user.collection.contains(game.id)
        val isFavorite = user.favorites.contains(game.id)

        // --- Boutons d'Action (Collection & Favori) ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // Bouton Collection
            Button(
                onClick = { game.toggleCollection(user, coroutineScope) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isInCollection) blueAccent else Color.Transparent,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.5.dp, blueAccent),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isInCollection) Icons.Filled.Check else Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isInCollection) "DANS LA COLLECTION" else "COLLECTION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Bouton Favori
            Button(
                onClick = { game.toggleFav(user, coroutineScope) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFavorite) pinkAccent else Color.Transparent,
                    contentColor = if (isFavorite) Color.Black else Color.White
                ),
                border = BorderStroke(1.5.dp, if (isFavorite) pinkAccent else blueAccent),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "FAVORI",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Fiches d'Informations (Plateformes, Éditeur, Date) ---
        Text(
            text = "Informations",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            border = BorderStroke(1.dp, blueAccent.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                InfoRow(
                    icon = Icons.Filled.Tv,
                    label = "Plateformes",
                    value = game.platformsListFormatted,
                    blueAccent = blueAccent
                )

                HorizontalDivider(color = blueAccent.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                InfoRow(
                    icon = Icons.Filled.Code,
                    label = "Développeur",
                    value = game.developerName,
                    blueAccent = blueAccent
                )

                HorizontalDivider(color = blueAccent.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                InfoRow(
                    icon = Icons.Filled.Business,
                    label = "Éditeur",
                    value = game.publisherName,
                    blueAccent = blueAccent
                )

                HorizontalDivider(color = blueAccent.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                InfoRow(
                    icon = Icons.Filled.CalendarToday,
                    label = "Date de sortie",
                    value = game.formattedReleaseDate,
                    blueAccent = blueAccent
                )

                HorizontalDivider(color = blueAccent.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                InfoRow(
                    icon = Icons.Filled.Category,
                    label = "Type",
                    value = game.categoryLabel,
                    blueAccent = blueAccent
                )

                HorizontalDivider(color = blueAccent.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                val hasFranchiseGames = game.franchiseGamesIds.isNotEmpty()
                InfoRow(
                    icon = Icons.Filled.Copyright,
                    label = "Franchise",
                    value = game.franchiseName,
                    blueAccent = blueAccent,
                    onClick = if (hasFranchiseGames && onGameGroupClick != null) {
                        { onGameGroupClick(game.franchiseName, game.franchiseGamesIds) }
                    } else null
                )

                HorizontalDivider(color = blueAccent.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                InfoRow(
                    icon = Icons.Filled.TypeSpecimen,
                    label = "Genre(s)",
                    value = game.genresListFormatted,
                    blueAccent = blueAccent
                )

                if (game.dlcGameIds.isNotEmpty()) {
                    HorizontalDivider(color = blueAccent.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                    InfoRow(
                        icon = Icons.Filled.Add,
                        label = "DLCs & Extensions",
                        value = "${game.dlcGameIds.size} contenu(s) disponible(s)",
                        blueAccent = blueAccent,
                        onClick = if (onGameGroupClick != null) {
                            { onGameGroupClick("DLCs & Extensions : ${game.name}", game.dlcGameIds) }
                        } else null
                    )
                }


                if (game.similar_games != null) {
                    HorizontalDivider(color = blueAccent.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                    InfoRow(
                        icon = Icons.Filled.Album,
                        label = "Jeux similaires",
                        value = "${game.similar_games.size} jeu(x) similaire(s)",
                        blueAccent = blueAccent,
                        onClick = if (onGameGroupClick != null) {
                            { onGameGroupClick("Jeux similaires : ${game.name}", game.similar_games) }
                        } else null
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Section Résumé ---
        Text(
            text = "Résumé",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            border = BorderStroke(1.dp, blueAccent.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = game.summary ?: "Aucun résumé disponible pour ce jeu.",
                fontSize = 14.sp,
                color = Color(0xFFECEFF1),
                lineHeight = 20.sp,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    blueAccent: Color,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = blueAccent,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFFB0BEC5)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (onClick != null) blueAccent else Color.White
            )
        }

        if (onClick != null) {
            Text(
                text = "▶",
                fontSize = 12.sp,
                color = blueAccent,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}