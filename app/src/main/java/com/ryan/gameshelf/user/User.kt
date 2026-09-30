package com.ryan.gameshelf

import androidx.compose.runtime.mutableStateListOf
import kotlinx.serialization.Serializable

@Serializable
data class User(
    var name: String,
    var favorites: MutableList<Game> = mutableStateListOf<Game>(),
    var collection: MutableList<Game> = mutableStateListOf<Game>(),
    val id: Int = 1,
) {

}