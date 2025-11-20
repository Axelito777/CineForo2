package com.example.registroapp.Di

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    val client = createSupabaseClient(
        supabaseUrl = "https://mhkyinpcemjclbbhkucf.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1oa3lpbnBjZW1qY2xiYmhrdWNmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyNDk4NDIsImV4cCI6MjA3ODgyNTg0Mn0.TYyuwhIzGb2pj1nKQEkX7MEVz2hKeGEdPmxDj4y4DJk"  // Asegúrate de que esté correcta
    ) {
        install(Postgrest)
        install(Auth)
        install(Storage)
    }
}