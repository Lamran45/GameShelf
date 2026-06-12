package com.ryan.gameshelf

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val name: String,
    val summary: String? = "Aucun résumé disponible.",
    val cover: Cover? = null,
    val category: Int = 0,
    val first_release_date: Long? = null
) {
    companion object{
        private var id = 0
    }
    var favorite by mutableStateOf(false)
    var played: Boolean = false
    var playing: Boolean = false
    var bought: Boolean = false
    var wished: Boolean = false

    // Attribut pour la récupération du type de Jeu
    val categoryLabel: String
        get() = when (category) {
            0 -> "Jeu complet"
            1 -> "DLC"
            2 -> "Extension"
            3 -> "Bundle"
            4 -> "Extension Standalone"
            8 -> "Remake"
            9 -> "Remaster"
            else -> "Autre" // Pour couvrir les mods, portages, etc.
        }

    init{
        id += 1
    }

    fun toggleFav(){
        this.favorite = !this.favorite
    }
}


@Serializable
data class Cover(
    val id: Long,
    @SerialName("image_id") val imageId: String
) {
    // Cette fonction va nous permettre de générer l'URL de l'image au bon format
    fun getUrl(): String {
        return "https://images.igdb.com/igdb/image/upload/t_cover_big/$imageId.jpg"
    }
}