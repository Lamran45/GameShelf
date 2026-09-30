package com.ryan.gameshelf.api

import com.ryan.gameshelf.BuildConfig
import com.ryan.gameshelf.Game
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ApiService (val authserv: AuthService) {
    val baseurlgame = "https://api.igdb.com/v4/games"
    private var accessToken : String? =  null

    var requestGameavailability: Boolean = true

    suspend fun init(){
        if(accessToken == null) {
            accessToken = authserv.getAccessToken()
        }
    }
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private val clientId = BuildConfig.CLIENTID

    // Cette fonction prend en paramètre le nom du jeu à chercher et le token d'accès
    suspend fun searchGame(gameName: String): List<Game> {

        return try {
            val response: List<Game> = client.post(baseurlgame) {
                // 1. On ajoute les headers obligatoires pour IGDB
                header("Client-ID", clientId)
                header("Authorization", "Bearer $accessToken")

                // 2. On écrit la requête textuelle exigée par IGDB pour filtrer les données
                setBody("search \"$gameName\"; fields name, summary, cover.image_id, category, first_release_date, platforms.name, involved_companies.company.name, involved_companies.developer, involved_companies.publisher; limit 30;")
            }.body()

            response
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // On renvoie une liste vide si ça échoue
        }
    }

    suspend fun RecentGame(offset : Int): List<Game>{
        // Prend en considération que les jeux de ces trois derniers mois

        if(!requestGameavailability) return emptyList()

        val currentTimestampSeconds = System.currentTimeMillis() / 1000
        //Il y a environ 3 mois
        val oneMonthLaterTimeStamp = currentTimestampSeconds + 2592000
        val threeMonthsAgoTimestamp = currentTimestampSeconds - 15552000
        val igdbQueryHome = "fields name, summary, cover.image_id, category, first_release_date, platforms.name, involved_companies.company.name, involved_companies.developer, involved_companies.publisher; limit 20; offset $offset; where first_release_date != null & first_release_date <= $oneMonthLaterTimeStamp & first_release_date >= $threeMonthsAgoTimestamp & hypes > 5;"

         try{
             val response: List<Game> = client.post(baseurlgame) {
                 header("Client-ID", clientId)
                 header("Authorization", "Bearer $accessToken")
                 setBody(igdbQueryHome)
             }.body()
             if(response.isEmpty()){
                 requestGameavailability = false
                 return emptyList()
             }
             return response
         } catch (e: Exception){
             e.printStackTrace()
             return emptyList()
         }
    }
}