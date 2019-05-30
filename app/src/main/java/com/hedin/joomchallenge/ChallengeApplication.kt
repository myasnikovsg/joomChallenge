package com.hedin.joomchallenge

import android.app.Application
import com.google.gson.Gson
import com.squareup.picasso.LruCache
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

class ChallengeApplication : Application() {

    companion object {
        lateinit var instance: ChallengeApplication
            private set
        lateinit var GIPHY_API_KEY: String
            private set
        val gson = Gson()
    }

    override fun onCreate() {
        super.onCreate()

        initPicasso()

        instance = this
        GIPHY_API_KEY = getString(R.string.giphy_api_key)
    }

    private fun initPicasso() {
        val cache = File(cacheDir, "picasso_cache")
        if (!cache.exists()) {
            cache.mkdirs()
        }

        val okHttpClient = OkHttpClient.Builder()
                .cache(Cache(cache, CONFIG_PICASSO_DISK_CACHE_SIZE.toLong()))
                .retryOnConnectionFailure(true)
                .build()

        val builder = Picasso.Builder(this)
        if (CONFIG_PICASSO_USE_LRU_CACHE) {
            builder.memoryCache(LruCache(CONFIG_PICASSO_MEMORY_CACHE_SIZE))
        }

        val picasso = builder
                .downloader(OkHttp3Downloader(okHttpClient))
                .build()

        Picasso.setSingletonInstance(picasso)
    }
}