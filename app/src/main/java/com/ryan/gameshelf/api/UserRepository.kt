package com.ryan.gameshelf.api

import com.ryan.gameshelf.User
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserGameRow(
    @SerialName("user_id") val userId: String,
    @SerialName("igdb_game_id") val igdbGameId: Long,
    @SerialName("is_favorite") val isFavorite: Boolean = false,
    @SerialName("is_in_collection") val isInCollection: Boolean = false,
    @SerialName("status") val status: String = "NONE"
)

object UserRepository {

    suspend fun loadUserGames(user: User) {
        if (user.uuid.isEmpty()) return
        try {
            val userGames = supabase.from("user_games")
                .select {
                    filter {
                        eq("user_id", user.uuid)
                    }
                }.decodeList<UserGameRow>()

            user.collection.clear()
            user.favorites.clear()

            userGames.forEach { row ->
                if (row.isInCollection && !user.collection.contains(row.igdbGameId)) {
                    user.collection.add(row.igdbGameId)
                }
                if (row.isFavorite && !user.favorites.contains(row.igdbGameId)) {
                    user.favorites.add(row.igdbGameId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncGameStatus(user: User, gameId: Long) {
        if (user.uuid.isEmpty()) return
        val isFav = user.favorites.contains(gameId)
        val inColl = user.collection.contains(gameId)

        try {
            if (!isFav && !inColl) {
                supabase.from("user_games").delete {
                    filter {
                        eq("user_id", user.uuid)
                        eq("igdb_game_id", gameId)
                    }
                }
            } else {
                supabase.from("user_games").upsert(
                    UserGameRow(
                        userId = user.uuid,
                        igdbGameId = gameId,
                        isFavorite = isFav,
                        isInCollection = inColl
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
