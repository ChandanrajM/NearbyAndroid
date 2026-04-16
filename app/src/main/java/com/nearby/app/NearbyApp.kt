package com.nearby.app

import android.app.Application
import com.nearby.app.data.SupabaseClient

class NearbyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Supabase client singleton
        SupabaseClient.init()
    }
}
