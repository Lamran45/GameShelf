package com.ryan.gameshelf

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ryan.gameshelf.api.ApiService
import com.ryan.gameshelf.api.AuthService
import com.ryan.gameshelf.api.UserRepository
import com.ryan.gameshelf.api.supabase
import com.ryan.gameshelf.ui.AuthScreen
import com.ryan.gameshelf.ui.GameDetailScreen
import com.ryan.gameshelf.ui.GameGroupScreen
import com.ryan.gameshelf.ui.GameView
import com.ryan.gameshelf.ui.SearchScreen
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.handleDeeplinks

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authService = AuthService()
        val apiService = ApiService(authService)
        val viewModel = GameView(apiService)
        viewModel.init()

        supabase.handleDeeplinks(intent)

        setContent {
            val sessionStatus by supabase.auth.sessionStatus.collectAsState()

            val loggedInUser = remember(sessionStatus) {
                when (val status = sessionStatus) {
                    is SessionStatus.Authenticated -> {
                        val session = status.session
                        User(
                            uuid = session.user?.id ?: "",
                            name = session.user?.email?.substringBefore("@") ?: "Joueur"
                        )
                    }
                    else -> null
                }
            }

            LaunchedEffect(loggedInUser?.uuid) {
                loggedInUser?.let { u ->
                    UserRepository.loadUserGames(u)
                }
            }

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    if (loggedInUser == null) {
                        AuthScreen()
                    } else {
                        MainScreen(viewModel, loggedInUser)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        supabase.handleDeeplinks(intent)
    }
}

@Composable
fun MainScreen(viewModel: GameView, user: User) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedGame by remember { mutableStateOf<Game?>(null) }
    var groupTitle by remember { mutableStateOf<String?>(null) }
    var groupGameIds by remember { mutableStateOf<List<Long>>(emptyList()) }

    if (groupTitle != null) {
        GameGroupScreen(
            title = groupTitle!!,
            gameIds = groupGameIds,
            viewModel = viewModel,
            user = user,
            onBackClick = { groupTitle = null },
            onGameClick = { game -> selectedGame = game }
        )
    } else if (selectedGame != null) {
        GameDetailScreen(
            game = selectedGame!!,
            user = user,
            onBackClick = { selectedGame = null },
            onGameGroupClick = { title, ids ->
                groupTitle = title
                groupGameIds = ids
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF1E1E1E),
                    contentColor = Color.White
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Accueil"
                            )
                        },
                        label = { Text("Accueil") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF2196F3),
                            selectedTextColor = Color(0xFF2196F3),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF2A2A2A)
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Recherche"
                            )
                        },
                        label = { Text("Recherche") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF2196F3),
                            selectedTextColor = Color(0xFF2196F3),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF2A2A2A)
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.VideoLibrary,
                                contentDescription = "Collection"
                            )
                        },
                        label = { Text("Collection") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF2196F3),
                            selectedTextColor = Color(0xFF2196F3),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF2A2A2A)
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.Bookmark,
                                contentDescription = "Wishlist"
                            )
                        },
                        label = { Text("Wishlist") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFF9800),
                            selectedTextColor = Color(0xFFFF9800),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF2A2A2A)
                        )
                    )
                }
            },
            containerColor = Color.Black
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    0 -> GameHomeScreen(
                        viewModel = viewModel,
                        user = user,
                        onGameClick = { game -> selectedGame = game }
                    )
                    1 -> SearchScreen(
                        viewModel = viewModel,
                        user = user,
                        onGameClick = { game -> selectedGame = game }
                    )
                    2 -> CollectionScreen(
                        user = user,
                        viewModel = viewModel,
                        onGameClick = { game -> selectedGame = game }
                    )
                    3 -> com.ryan.gameshelf.ui.WishlistScreen(
                        user = user,
                        viewModel = viewModel,
                        onGameClick = { game -> selectedGame = game }
                    )
                }
            }
        }
    }
}

@Composable
fun GameHomeScreen(
    viewModel: GameView,
    user: User,
    onGameClick: (Game) -> Unit
) {
    if (viewModel.gameslist.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFF8BADC))
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(viewModel.gameslist) { index, game ->
                if (index >= viewModel.gameslist.size - 4) {
                    viewModel.loadmore()
                }
                GameItem(user, game, onGameClick = onGameClick)
            }
        }
    }
}

@Composable
fun CollectionScreen(
    user: User,
    viewModel: GameView,
    onGameClick: (Game) -> Unit
) {
    var loadedGames by remember { mutableStateOf<List<Game>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Tous") }

    val allRelevantIds = remember(user.collection.toList(), user.favorites.toList(), user.gameDetails.toMap()) {
        (user.collection + user.favorites + user.gameDetails.keys).distinct()
    }

    LaunchedEffect(allRelevantIds) {
        val missingIds = allRelevantIds.filter { id -> loadedGames.none { game -> game.id == id } }
        if (missingIds.isNotEmpty()) {
            if (loadedGames.isEmpty()) {
                isLoading = true
            }
            val newGames = viewModel.getGamesByIds(missingIds)
            loadedGames = loadedGames + newGames
            isLoading = false
        }
    }

    val displayedGames = remember(loadedGames, allRelevantIds) {
        loadedGames.filter { allRelevantIds.contains(it.id) }
    }

    val filteredGames = remember(displayedGames, user.favorites.toList(), user.collection.toList(), user.gameDetails.toMap(), selectedFilter) {
        when (selectedFilter) {
            "Favoris" -> displayedGames.filter { user.favorites.contains(it.id) || user.gameDetails[it.id]?.isFavorite == true }
            "100%" -> displayedGames.filter { user.gameDetails[it.id]?.gameState.equals("100%", ignoreCase = true) }
            "En cours" -> displayedGames.filter { user.gameDetails[it.id]?.gameState.equals("DOING", ignoreCase = true) }
            "À faire" -> displayedGames.filter { user.gameDetails[it.id]?.gameState.equals("TODO", ignoreCase = true) }
            "Fini" -> displayedGames.filter { user.gameDetails[it.id]?.gameState.equals("FINISHED", ignoreCase = true) }
            "Physique" -> displayedGames.filter { user.gameDetails[it.id]?.physical == true }
            "Dématérialisé" -> displayedGames.filter { user.gameDetails[it.id]?.physical == false }
            else -> displayedGames.filter { user.collection.contains(it.id) || user.gameDetails[it.id]?.isInCollection == true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Ma Collection",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
        )

        // Filtres
        val filters = listOf("Tous", "Favoris", "En cours", "À faire", "100%", "Fini", "Physique", "Dématérialisé")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            items(filters) { filterLabel ->
                val isSelected = selectedFilter == filterLabel
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Color(0xFF2196F3) else Color(0xFF1E1E1E))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFF2196F3) else Color.Gray.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedFilter = filterLabel }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filterLabel,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color.LightGray
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2196F3))
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
                        imageVector = Icons.Outlined.VideoLibrary,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Votre collection est tristement vide...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ajoutez des jeux depuis l'accueil en appuyant sur le bouton +.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (filteredGames.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucun jeu ne correspond au filtre \"$selectedFilter\".",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(filteredGames) { _, game ->
                    GameItem(user, game, onGameClick = onGameClick)
                }
            }
        }
    }
}
