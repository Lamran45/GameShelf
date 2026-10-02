package com.ryan.gameshelf.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryan.gameshelf.api.ApiService
import com.ryan.gameshelf.Game
import kotlinx.coroutines.launch

class GameView(private val apiserv: ApiService): ViewModel(){
    val gameslist = mutableStateListOf<Game>()
    var currentoffset = 0
    var isloading = false
    var isinitialized = false

    fun init(){
        // Lance l'initialisation des services de l'api
        if(isinitialized) return
        viewModelScope.launch {
            try{
                apiserv.init()
                isinitialized = true
                handlescrollhome()
            } catch(e: Exception){
                e.printStackTrace()
            }
        }
    }

    suspend fun handlescrollhome(){
        // Gére les nouveaux appels api quand l'utilisateur scroll assez loin
        if(isloading) return
        isloading = true

        try{
            val response = apiserv.RecentGame(currentoffset)

            if(response.isNotEmpty()){
                gameslist.addAll(response)
                currentoffset += response.size
            }
        } catch(e: Exception){
            e.printStackTrace()
        } finally {
            isloading = false
        }
    }

    fun loadmore(){
        // Lance la récupération des nouveaux jeu
        if(!isloading && isinitialized){
            viewModelScope.launch{
                handlescrollhome()
            }
        }
    }

    suspend fun searchGames(query: String): List<Game> {
        if (query.trim().length < 3) return emptyList()
        return try {
            apiserv.searchGame(query.trim())
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}