package com.ryan.gameshelf

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class Platform(
    val id: Long = 0,
    val name: String = ""
)

@Serializable
data class Company(
    val id: Long = 0,
    val name: String = ""
)

@Serializable
data class InvolvedCompany(
    val id: Long = 0,
    val company: Company? = null,
    val developer: Boolean = false,
    val publisher: Boolean = false
)

@Serializable
data class GameType(
    val id: Long = 0,
    val type: String = ""
)

@Serializable
data class Franchise(
    val id: Long = 0,
    val name: String = "",
    val games: List<Long>? = null
)

@Serializable
data class Genre(
    val id: Long = 0,
    val name: String = ""
)

@Serializable
data class Game(
    val name: String,
    val summary: String? = "Aucun résumé disponible.",
    val cover: Cover? = null,
    val game_type: GameType? = null,
    val franchise: Franchise? = null,
    val franchises: List<Franchise>? = null,
    val genres: List<Genre>? = null,
    val first_release_date: Long? = null,
    val platforms: List<Platform>? = null,
    val involved_companies: List<InvolvedCompany>? = null,
    val dlcs: List<Long>? = null,
    val expansions: List<Long>? = null,
    val standalone_expansions: List<Long>? = null,
    val rating: Double = 0.0
) {
    companion object{
        private var id = 0
    }
    var favorite by mutableStateOf(false)
    var inCollection by mutableStateOf(false)
    var played: Boolean = false
    var playing: Boolean = false
    var bought: Boolean = false
    var wished: Boolean = false

    // Attribut pour la récupération du type de jeu
    val categoryLabel: String
        get() = game_type?.type ?: "Complet"

    val franchiseName: String
        get() {
            if (franchise != null && franchise.name.isNotEmpty()) return franchise.name
            if (!franchises.isNullOrEmpty()) return franchises.joinToString(", ") { it.name }
            return "Non spécifiée"
        }

    val genresListFormatted: String
        get() {
            if (genres.isNullOrEmpty()) return "Non spécifié"
            return genres.joinToString(", ") { it.name }
        }

    val formattedReleaseDate: String
        get() {
            if (first_release_date == null) return "Non communiquée"
            return try {
                val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
                sdf.format(Date(first_release_date * 1000))
            } catch (e: Exception){
                "Non communiquée"
            }
        }

    val platformsListFormatted: String
        get() {
            if (platforms.isNullOrEmpty()) return "Non spécifié"
            return platforms.joinToString(", ") { it.name }
        }

    val developerName: String
        get() {
            val devs = involved_companies?.filter { it.developer }?.mapNotNull { it.company?.name }
            if (!devs.isNullOrEmpty()) return devs.joinToString(", ")
            val anyComp = involved_companies?.mapNotNull { it.company?.name }
            if (!anyComp.isNullOrEmpty()) return anyComp.joinToString(", ")
            return "Non spécifié"
        }

    val publisherName: String
        get() {
            val pubs = involved_companies?.filter { it.publisher }?.mapNotNull { it.company?.name }
            if (!pubs.isNullOrEmpty()) return pubs.joinToString(", ")
            return "Non spécifié"
        }

    val franchiseGamesIds: List<Long>
        get() {
            val ids = mutableListOf<Long>()
            franchise?.games?.let { ids.addAll(it) }
            franchises?.forEach { fr -> fr.games?.let { ids.addAll(it) } }
            return ids.distinct()
        }

    val dlcGameIds: List<Long>
        get() {
            val ids = mutableListOf<Long>()
            dlcs?.let { ids.addAll(it) }
            expansions?.let { ids.addAll(it) }
            standalone_expansions?.let { ids.addAll(it) }
            return ids.distinct()
        }

    init{
        id += 1
    }

    fun toggleFav(user: User){
        this.favorite = !this.favorite
        println("Salut")
        if(this.favorite){
            user.favorites.add(this)
            println(user.favorites)
        } else {
            user.favorites.remove(this)
        }
    }

    fun toggleCollection(user: User){
        this.inCollection = !this.inCollection
        if(this.inCollection){
            user.collection.add(this)
        } else {
            user.collection.remove(this)
        }
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