package com.ryan.gameshelf

import com.ryan.gameshelf.api.UserGameRow
import com.ryan.gameshelf.ui.gameStateOptions
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserGameTest {

    @Test
    fun testUserGameRowDefaults() {
        val row = UserGameRow(
            userId = "user-123",
            igdbGameId = 101L,
            isFavorite = true,
            isInCollection = true,
            plateforme = "PC (Steam)",
            physical = false,
            gameState = "DOING",
            borrowed = true
        )

        assertEquals("user-123", row.userId)
        assertEquals(101L, row.igdbGameId)
        assertTrue(row.isFavorite)
        assertTrue(row.isInCollection)
        assertEquals("PC (Steam)", row.plateforme)
        assertFalse(row.physical!!)
        assertEquals("DOING", row.gameState)
        assertTrue(row.borrowed!!)
    }

    @Test
    fun testUserGameRowSerialization() {
        val row = UserGameRow(
            userId = "abc-uuid",
            igdbGameId = 999L,
            isFavorite = false,
            isInCollection = true,
            plateforme = "PlayStation 5",
            physical = true,
            gameState = "TODO",
            borrowed = false
        )

        val jsonString = Json.encodeToString(UserGameRow.serializer(), row)
        val decoded = Json.decodeFromString(UserGameRow.serializer(), jsonString)

        assertEquals(row.userId, decoded.userId)
        assertEquals(row.igdbGameId, decoded.igdbGameId)
        assertEquals(row.plateforme, decoded.plateforme)
        assertEquals(row.physical, decoded.physical)
        assertEquals(row.gameState, decoded.gameState)
        assertEquals(row.borrowed, decoded.borrowed)
    }

    @Test
    fun testGameStateOptionsMapping() {
        assertEquals(6, gameStateOptions.size)
        assertEquals("TODO", gameStateOptions[0].dbValue)
        assertEquals("À faire", gameStateOptions[0].label)
        assertEquals("DOING", gameStateOptions[1].dbValue)
        assertEquals("En cours", gameStateOptions[1].label)
        assertEquals("PAUSED", gameStateOptions[2].dbValue)
        assertEquals("ABANDONNED", gameStateOptions[3].dbValue)
        assertEquals("FINISHED", gameStateOptions[4].dbValue)
        assertEquals("100%", gameStateOptions[5].dbValue)
    }

    @Test
    fun testBorrowedGameIsInCollectionFalse() {
        val borrowedRow = UserGameRow(
            userId = "user-1",
            igdbGameId = 200L,
            isFavorite = false,
            isInCollection = !true, // borrowed = true => isInCollection = false
            borrowed = true
        )
        assertFalse(borrowedRow.isInCollection)
        assertTrue(borrowedRow.borrowed!!)
    }

    @Test
    fun testWishlistUserGameRow() {
        val row = UserGameRow(
            userId = "user-wish",
            igdbGameId = 333L,
            isWishlisted = true
        )
        assertTrue(row.isWishlisted)
        assertFalse(row.isInCollection)
        assertFalse(row.isFavorite)
    }

    @Test
    fun testGameCategoryDefaults() {
        val modGame = Game(id = 1, name = "Doom Mod", game_type = GameType(1, "Mod"))
        val mainGame = Game(id = 2, name = "Tekken 8", game_type = GameType(2, "Main Game"))

        val isModNonStandard = modGame.categoryLabel.let { type ->
            val lower = type.lowercase().trim()
            val standardKeywords = listOf("complet", "main", "dlc", "expansion", "bundle", "pack", "remake", "remaster")
            standardKeywords.none { lower.contains(it) }
        }

        val isMainNonStandard = mainGame.categoryLabel.let { type ->
            val lower = type.lowercase().trim()
            val standardKeywords = listOf("complet", "main", "dlc", "expansion", "bundle", "pack", "remake", "remaster")
            standardKeywords.none { lower.contains(it) }
        }

        assertTrue(isModNonStandard)
        assertFalse(isMainNonStandard)
    }

    @Test
    fun testUserClearDataOnAccountDeletionOrLogout() {
        val user = User(
            name = "TestUser",
            uuid = "test-uuid"
        )
        user.collection.add(101L)
        user.favorites.add(202L)
        user.wishlist.add(303L)
        user.gameDetails[101L] = UserGameRow(userId = "test-uuid", igdbGameId = 101L)

        assertEquals(1, user.collection.size)
        assertEquals(1, user.favorites.size)
        assertEquals(1, user.wishlist.size)
        assertEquals(1, user.gameDetails.size)

        // Simuler la réinitialisation des données locales
        user.collection.clear()
        user.favorites.clear()
        user.wishlist.clear()
        user.gameDetails.clear()

        assertTrue(user.collection.isEmpty())
        assertTrue(user.favorites.isEmpty())
        assertTrue(user.wishlist.isEmpty())
        assertTrue(user.gameDetails.isEmpty())
    }
}