package com.hedin.joomchallenge.network

import com.hedin.joomchallenge.model.GiphyItemResponse
import com.hedin.joomchallenge.model.GiphyListResponse
import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GiphyService {

    @GET("trending")
    fun trending(
            @Query("api_key") apiKey: String,
            @Query("offset") offset: Int,
            @Query("limit") limit: Int
    ): Flowable<Response<GiphyListResponse>>

    @GET("{gif_id}")
    fun byId(
            @Path("gif_id") id: String,
            @Query("api_key") apiKey: String
    ): Single<Response<GiphyItemResponse>>
}