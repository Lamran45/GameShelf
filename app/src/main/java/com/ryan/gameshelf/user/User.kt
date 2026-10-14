package com.ryan.gameshelf

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.ryan.gameshelf.api.UserGameRow
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class User(
    var name: String,
    var favorites: MutableList<Long> = mutableStateListOf<Long>(),
    var collection: MutableList<Long> = mutableStateListOf<Long>(),
    var wishlist: MutableList<Long> = mutableStateListOf<Long>(),
    val uuid: String = ""
) {
    @Transient
    val gameDetails: SnapshotStateMap<Long, UserGameRow> = mutableStateMapOf()
}