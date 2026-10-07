package com.ryan.gameshelf.api

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import com.ryan.gameshelf.BuildConfig

// Client Supabase connecté à ton projet
val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.ANON_SUPABASE,
) {
    install(Auth) {
        scheme = "gameshelf"
        host = "login-callback"
    }
    install(Postgrest)
}
