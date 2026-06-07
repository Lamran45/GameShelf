package com.ryan.gameshelf

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class AuthService {

    // On configure le client HTTP Ktor avec le plugin JSON
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true // Évite de crash si l'API renvoie des infos dont on n'a pas besoin
            })
        }
    }

    // Remplace par tes vraies clés récupérées sur Twitch
    private val clientId = BuildConfig.CLIENTID
    private val clientSecret = BuildConfig.CLIENTSECRET

    suspend fun getAccessToken(): String? {
        val url = "https://id.twitch.tv/oauth2/token"
        println(BuildConfig.CLIENTID)
        return try {
            // On fait une requête POST comme demandé par la doc de Twitch
            val response: TwitchResponse = client.post(url) {
                parameter("client_id", clientId)
                parameter("client_secret", clientSecret)
                parameter("grant_type", "client_credentials")
            }.body()

            response.accessToken
        } catch (e: Exception) {
            e.printStackTrace() // Permet de voir l'erreur dans la console Logcat en cas de pépin
            null
        }
    }
}