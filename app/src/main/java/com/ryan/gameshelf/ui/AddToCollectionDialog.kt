package com.ryan.gameshelf.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ryan.gameshelf.Game
import com.ryan.gameshelf.User
import kotlin.math.roundToInt

data class GameStateOption(val dbValue: String, val label: String)

val gameStateOptions = listOf(
    GameStateOption("TODO", "À faire"),
    GameStateOption("DOING", "En cours"),
    GameStateOption("PAUSED", "En pause"),
    GameStateOption("ABANDONNED", "Abandonné"),
    GameStateOption("FINISHED", "Fini"),
    GameStateOption("100%", "Fini à 100%")
)

val pcStores = listOf("Steam", "Xbox / Microsoft Store", "GOG", "Epic Games", "Ubisoft Connect", "EA App", "Autre")

private fun isPcPlatform(platformName: String): Boolean {
    val lower = platformName.lowercase()
    return lower.contains("pc") || lower.contains("windows")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCollectionDialog(
    game: Game,
    user: User,
    onDismiss: () -> Unit,
    onSave: (platform: String?, isPhysical: Boolean, gameState: String, borrowed: Boolean) -> Unit,
    onRemove: (() -> Unit)? = null
) {
    val existing = user.gameDetails[game.id]
    val isInCollection = user.collection.contains(game.id)

    val availablePlatforms = remember(game) {
        val list = game.platforms?.map { it.name }?.filter { it.isNotBlank() } ?: emptyList()
        if (list.isNotEmpty()) {
            if (!list.contains("Autre")) list + "Autre" else list
        } else {
            listOf("PC", "PlayStation 5", "Xbox Series X/S", "Nintendo Switch", "PlayStation 4", "Xbox One", "Autre")
        }
    }

    // Parse initial platform and store if existing
    val (initialPlatformBase, initialStore) = remember(existing) {
        val stored = existing?.plateforme ?: ""
        if (stored.startsWith("PC (") && stored.endsWith(")")) {
            val store = stored.substring(4, stored.length - 1)
            Pair(availablePlatforms.firstOrNull { isPcPlatform(it) } ?: "PC", store)
        } else if (stored.isNotBlank()) {
            Pair(stored, "")
        } else {
            Pair(availablePlatforms.firstOrNull() ?: "PC", "")
        }
    }

    var selectedPlatform by remember { mutableStateOf(initialPlatformBase) }
    var selectedPcStore by remember { mutableStateOf(initialStore) }
    var userChangedFormat by remember { mutableStateOf(false) }

    // Physical format default: false if PC, true otherwise
    var isPhysical by remember(existing, selectedPlatform) {
        if (existing?.physical != null) {
            mutableStateOf(existing.physical)
        } else {
            val defaultPhysical = !isPcPlatform(selectedPlatform)
            mutableStateOf(defaultPhysical)
        }
    }

    // Game progress state
    val initialIndex = remember(existing) {
        val found = gameStateOptions.indexOfFirst { it.dbValue.equals(existing?.gameState, ignoreCase = true) }
        if (found >= 0) found else 0
    }
    var stateSliderIndex by remember { mutableFloatStateOf(initialIndex.toFloat()) }

    var borrowed by remember { mutableStateOf(existing?.borrowed ?: false) }

    var platformDropdownExpanded by remember { mutableStateOf(false) }
    var storeDropdownExpanded by remember { mutableStateOf(false) }

    val blueAccent = Color(0xFF2196F3)
    val cardBackground = Color(0xFF1E1E1E)
    val dialogBackground = Color(0xFF121212)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = dialogBackground,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, blueAccent.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isInCollection) "Modifier la collection" else "Ajouter à la collection",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = game.name,
                    fontSize = 14.sp,
                    color = blueAccent,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 16.dp)
                )

                HorizontalDivider(color = blueAccent.copy(alpha = 0.3f), modifier = Modifier.padding(bottom = 16.dp))

                // --- 1. Plateforme ---
                Text(
                    text = "Plate-forme possédé",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { platformDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = cardBackground,
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, blueAccent.copy(alpha = 0.6f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Devices, contentDescription = null, tint = blueAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = selectedPlatform, fontSize = 14.sp, color = Color.White)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                        }
                    }

                    DropdownMenu(
                        expanded = platformDropdownExpanded,
                        onDismissRequest = { platformDropdownExpanded = false },
                        modifier = Modifier.background(cardBackground)
                    ) {
                        availablePlatforms.forEach { platform ->
                            DropdownMenuItem(
                                text = { Text(platform, color = Color.White) },
                                onClick = {
                                    selectedPlatform = platform
                                    platformDropdownExpanded = false
                                    // Update physical default when switching platform if user hasn't toggled manually
                                    if (!userChangedFormat) {
                                        isPhysical = !isPcPlatform(platform)
                                    }
                                }
                            )
                        }
                    }
                }

                // --- 2. Boutique PC (si PC sélectionné) ---
                if (isPcPlatform(selectedPlatform)) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Store / Client PC",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { storeDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = cardBackground,
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, blueAccent.copy(alpha = 0.6f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = blueAccent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedPcStore.isNotBlank()) selectedPcStore else "Sélectionner un store (ex: Steam)",
                                        fontSize = 14.sp,
                                        color = if (selectedPcStore.isNotBlank()) Color.White else Color.Gray
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                            }
                        }

                        DropdownMenu(
                            expanded = storeDropdownExpanded,
                            onDismissRequest = { storeDropdownExpanded = false },
                            modifier = Modifier.background(cardBackground)
                        ) {
                            pcStores.forEach { store ->
                                DropdownMenuItem(
                                    text = { Text(store, color = Color.White) },
                                    onClick = {
                                        selectedPcStore = store
                                        storeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- 3. Format (Physique vs Dématérialisé via Slider) ---
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Format du jeu",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isPhysical) "Physique 💿" else "Dématérialisé 💾",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = blueAccent
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Slider(
                    value = if (isPhysical) 0f else 1f,
                    onValueChange = { valPos ->
                        userChangedFormat = true
                        isPhysical = valPos < 0.5f
                    },
                    valueRange = 0f..1f,
                    steps = 0,
                    colors = SliderDefaults.colors(
                        thumbColor = blueAccent,
                        activeTrackColor = blueAccent,
                        inactiveTrackColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Physique", fontSize = 12.sp, color = if (isPhysical) Color.White else Color.Gray)
                    Text("Dématérialisé", fontSize = 12.sp, color = if (!isPhysical) Color.White else Color.Gray)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // --- 4. Avancement dans le jeu (Slider 6 étapes) ---
                val currentOptionIndex = stateSliderIndex.roundToInt().coerceIn(0, gameStateOptions.lastIndex)
                val currentOption = gameStateOptions[currentOptionIndex]

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Avancement",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = currentOption.label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = blueAccent
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Slider(
                    value = stateSliderIndex,
                    onValueChange = { stateSliderIndex = it },
                    valueRange = 0f..5f,
                    steps = 4,
                    colors = SliderDefaults.colors(
                        thumbColor = blueAccent,
                        activeTrackColor = blueAccent,
                        inactiveTrackColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // --- 5. Obtention temporaire (Checkbox) ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(cardBackground)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .clickable { borrowed = !borrowed }
                ) {
                    Checkbox(
                        checked = borrowed,
                        onCheckedChange = { borrowed = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = blueAccent,
                            uncheckedColor = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Obtenu de manière temporaire",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // --- Boutons d'action ---
                val isSavedInUserGames = user.gameDetails.containsKey(game.id) || isInCollection

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val finalPlatform = if (isPcPlatform(selectedPlatform) && selectedPcStore.isNotBlank()) {
                                "PC ($selectedPcStore)"
                            } else {
                                selectedPlatform
                            }
                            val finalState = currentOption.dbValue
                            onSave(finalPlatform, isPhysical, finalState, borrowed)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = blueAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSavedInUserGames) "Enregistrer les modifications" else "Enregistrer",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isSavedInUserGames && onRemove != null) {
                        OutlinedButton(
                            onClick = onRemove,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Retirer de la collection", fontWeight = FontWeight.Bold)
                        }
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Annuler", color = Color.Gray)
                    }
                }
            }
        }
    }
}