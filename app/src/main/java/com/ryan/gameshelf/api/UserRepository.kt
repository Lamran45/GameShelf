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
    @SerialName("is_wishlisted") val isWishlisted: Boolean = false,
    @SerialName("status") val status: String? = "NONE",
    @SerialName("plateforme") val plateforme: String? = null,
    @SerialName("game_state") val gameState: String? = "TODO",
    @SerialName("borrowed") val borrowed: Boolean? = false,
    @SerialName("physical") val physical: Boolean? = true
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
            user.wishlist.clear()
            user.gameDetails.clear()

            userGames.forEach { row ->
                user.gameDetails[row.igdbGameId] = row
                if (row.isInCollection && !user.collection.contains(row.igdbGameId)) {
                    user.collection.add(row.igdbGameId)
                }
                if (row.isFavorite && !user.favorites.contains(row.igdbGameId)) {
                    user.favorites.add(row.igdbGameId)
                }
                if (row.isWishlisted && !user.wishlist.contains(row.igdbGameId)) {
                    user.wishlist.add(row.igdbGameId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveUserGameDetails(
        user: User,
        gameId: Long,
        platform: String?,
        isPhysical: Boolean,
        gameState: String,
        borrowed: Boolean
    ) {
        if (user.uuid.isEmpty()) return
        val inCollection = !borrowed
        if (inCollection) {
            if (!user.collection.contains(gameId)) {
                user.collection.add(gameId)
            }
        } else {
            user.collection.remove(gameId)
        }
        val isFav = user.favorites.contains(gameId)
        val isWish = user.wishlist.contains(gameId)
        val row = UserGameRow(
            userId = user.uuid,
            igdbGameId = gameId,
            isFavorite = isFav,
            isInCollection = inCollection,
            isWishlisted = isWish,
            plateforme = platform,
            physical = isPhysical,
            gameState = gameState,
            borrowed = borrowed
        )
        user.gameDetails[gameId] = row

        try {
            supabase.from("user_games").upsert(row)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun removeFromCollection(user: User, gameId: Long) {
        if (user.uuid.isEmpty()) return
        user.collection.remove(gameId)
        val isFav = user.favorites.contains(gameId)
        val isWish = user.wishlist.contains(gameId)
        val current = user.gameDetails[gameId]

        try {
            if (!isFav && !isWish) {
                supabase.from("user_games").delete {
                    filter {
                        eq("user_id", user.uuid)
                        eq("igdb_game_id", gameId)
                    }
                }
                user.gameDetails.remove(gameId)
            } else {
                val updatedRow = current?.copy(isInCollection = false) ?: UserGameRow(
                    userId = user.uuid,
                    igdbGameId = gameId,
                    isFavorite = isFav,
                    isInCollection = false,
                    isWishlisted = isWish
                )
                user.gameDetails[gameId] = updatedRow
                supabase.from("user_games").upsert(updatedRow)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncGameStatus(user: User, gameId: Long) {
        if (user.uuid.isEmpty()) return
        val isFav = user.favorites.contains(gameId)
        val inColl = user.collection.contains(gameId)
        val isWish = user.wishlist.contains(gameId)
        val existing = user.gameDetails[gameId]

        try {
            if (!isFav && !inColl && !isWish) {
                supabase.from("user_games").delete {
                    filter {
                        eq("user_id", user.uuid)
                        eq("igdb_game_id", gameId)
                    }
                }
                user.gameDetails.remove(gameId)
            } else {
                val row = existing?.copy(
                    isFavorite = isFav,
                    isInCollection = inColl,
                    isWishlisted = isWish
                ) ?: UserGameRow(
                    userId = user.uuid,
                    igdbGameId = gameId,
                    isFavorite = isFav,
                    isInCollection = inColl,
                    isWishlisted = isWish
                )
                user.gameDetails[gameId] = row
                supabase.from("user_games").upsert(row)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteUserData(user: User) {
        if (user.uuid.isEmpty()) return
        try {
            supabase.from("user_games").delete {
                filter {
                    eq("user_id", user.uuid)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try{
            supabase.from("profiles").delete {
                filter{
                    eq("id", user.uuid)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        user.collection.clear()
        user.favorites.clear()
        user.wishlist.clear()
        user.gameDetails.clear()
    }
}
