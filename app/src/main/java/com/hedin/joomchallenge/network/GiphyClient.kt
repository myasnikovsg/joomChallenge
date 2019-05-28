package com.hedin.joomchallenge.network

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class GiphyClient {

    companion object {
        private const val BASE_URL = "https://api.giphy.com/v1/gifs/"

        private val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val service: GiphyService = retrofit.create(GiphyService::class.java)
    }
}