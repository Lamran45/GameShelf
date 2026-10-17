package com.ryan.gameshelf.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ryan.gameshelf.User
import com.ryan.gameshelf.api.UserRepository
import com.ryan.gameshelf.api.supabase
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    user: User,
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var usernameState by remember(user.name) { mutableStateOf(user.name) }
    var newUsernameInput by remember { mutableStateOf("") }
    var isSavingUsername by remember { mutableStateOf(false) }

    LaunchedEffect(user.uuid) {
        if (user.uuid.isNotEmpty()) {
            UserRepository.loadUserProfile(user)
            usernameState = user.name
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon Profil", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Photo de profil / Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Photo de profil",
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pseudo centré avec icône Edit
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = usernameState.ifEmpty { "Joueur" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(
                        onClick = {
                            newUsernameInput = usernameState
                            showEditDialog = true
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Modifier le pseudo",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Bouton Déconnexion
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                supabase.auth.signOut()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Se déconnecter",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bouton Suppression de compte
                Button(
                    onClick = { showDeleteDialog = true },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteForever,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Supprimer mon compte",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(32.dp))
                    CircularProgressIndicator(color = Color(0xFF2196F3))
                }
            }
        }
    }

    // Dialogue de modification du pseudo
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSavingUsername) showEditDialog = false },
            title = {
                Text(
                    text = "Modifier votre pseudo",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newUsernameInput,
                        onValueChange = { newUsernameInput = it },
                        label = { Text("Pseudo", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF2196F3)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = newUsernameInput.trim()
                        if (trimmed.isNotEmpty()) {
                            coroutineScope.launch {
                                isSavingUsername = true
                                try {
                                    UserRepository.updateUsername(user, trimmed)
                                    usernameState = user.name
                                    showEditDialog = false
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                } finally {
                                    isSavingUsername = false
                                }
                            }
                        }
                    },
                    enabled = !isSavingUsername && newUsernameInput.trim().isNotEmpty()
                ) {
                    if (isSavingUsername) {
                        CircularProgressIndicator(
                            color = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Valider",
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false },
                    enabled = !isSavingUsername
                ) {
                    Text(text = "Annuler", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dialogue de confirmation de suppression de compte
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Suppression du compte",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Êtes-vous sûr de vouloir supprimer votre compte ? Toutes vos données seront définitivement perdues.",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                UserRepository.deleteUserData(user)
                                supabase.auth.signOut()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                ) {
                    Text(
                        text = "Supprimer",
                        color = Color(0xFFFF5252),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "Annuler", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
