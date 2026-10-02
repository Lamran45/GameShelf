package com.ryan.gameshelf.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ryan.gameshelf.Game
import com.ryan.gameshelf.GameItem
import com.ryan.gameshelf.User
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: GameView,
    onGameClick: (Game) -> Unit,
    modifier: Modifier = Modifier,
    user: User = com.ryan.gameshelf.user,
    onSearch: (String) -> Unit = {},
) {
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<Game>>(emptyList()) }

    val blueAccent = Color(0xFF2196F3)

    // Effet avec Debounce de 500ms et déclenchement dès 2 caractères
    LaunchedEffect(query) {
        val trimmed = query.trim()
        if (trimmed.length >= 2) {
            delay(500L) // Attente de 500ms d'inactivité
            isSearching = true
            suggestions = viewModel.searchGames(trimmed)
            isSearching = false
        } else {
            suggestions = emptyList()
            isSearching = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .semantics { isTraversalGroup = true }
    ) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .semantics { traversalIndex = 0f },
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = {
                        query = it
                        if (it.trim().length >= 2) {
                            expanded = true
                        }
                    },
                    onSearch = {
                        onSearch(query)
                        expanded = false
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = {
                        Text(
                            text = "Rechercher un jeu...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Rechercher",
                            tint = blueAccent
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = {
                                query = ""
                                suggestions = emptyList()
                                expanded = false
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Effacer",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = SearchBarDefaults.inputFieldColors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        disabledContainerColor = Color(0xFF1E1E1E),
                        cursorColor = blueAccent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
            colors = SearchBarDefaults.colors(
                containerColor = Color.Black,
                dividerColor = Color(0xFF2A2A2A)
            )
        ) {
            // Zone des suggestions de recherche sous forme de grille identique à la page d'accueil
            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = blueAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Recherche en cours sur IGDB...",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (suggestions.isEmpty() && query.trim().length >= 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun jeu trouvé pour \"$query\"",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(suggestions) { _, game ->
                        GameItem(
                            user = user,
                            game = game,
                            onGameClick = {
                                expanded = false
                                onGameClick(game)
                            }
                        )
                    }
                }
            }
        }
    }
}