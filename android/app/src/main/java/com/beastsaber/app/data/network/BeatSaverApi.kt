package com.beastsaber.app.data.network

import com.beastsaber.app.data.model.MapDetail
import com.beastsaber.app.data.model.MapSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface BeatSaverApi {

    @GET("maps/latest")
    suspend fun mapsLatest(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int?,
        @Query("sort") sort: String?,
        @Query("automapper") automapper: Boolean?
    ): MapSearchResponse

    @GET("search/text/{page}")
    suspend fun searchText(
        @Path("page") page: Int,
        @QueryMap(encoded = true) options: Map<String, String>
    ): MapSearchResponse

    @GET("maps/id/{id}")
    suspend fun mapById(@Path("id") id: String): MapDetail
}
