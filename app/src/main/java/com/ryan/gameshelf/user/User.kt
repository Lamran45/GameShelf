package com.ryan.gameshelf

import androidx.compose.runtime.mutableStateListOf
import kotlinx.serialization.Serializable

@Serializable
data class User(
    var name: String,
    var favorites: MutableList<Long> = mutableStateListOf<Long>(),
    var collection: MutableList<Long> = mutableStateListOf<Long>(),
    val id: Int = 1,
)