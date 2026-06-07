package com.ryan.gameshelf

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


class ApiService {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private val clientId = BuildConfig.CLIENTID

    // Cette fonction prend en paramètre le nom du jeu à chercher et le token d'accès
    suspend fun searchGame(gameName: String, accessToken: String): List<Game> {
        val url = "https://api.igdb.com/v4/games"

        return try {
            val response: List<Game> = client.post(url) {
                // 1. On ajoute les headers obligatoires pour IGDB
                header("Client-ID", clientId)
                header("Authorization", "Bearer $accessToken")

                // 2. On écrit la requête textuelle exigée par IGDB pour filtrer les données
                setBody("search \"$gameName\"; fields name, summary, cover.image_id, category; limit 5;")
            }.body()

            response
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // On renvoie une liste vide si ça échoue
        }
    }
}