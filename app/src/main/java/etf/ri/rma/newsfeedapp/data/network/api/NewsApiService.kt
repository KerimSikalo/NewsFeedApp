package etf.ri.rma.newsfeedapp.data.network.api

import etf.ri.rma.newsfeedapp.data.network.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NewsApiService {
    @GET("news/top")
    suspend fun getTopNewsByCategory(
        @Query("categories") category: String,
        @Query("api_token") apiKey: String = "K4WG2faoRMhvESCd4oViM3FeiFKqz7Cwl7PPbf2R",
        @Query("locale") locale: String = "us",
        @Query("limit") limit: Int = 3
    ): NewsResponse
    @GET("news/all")
    suspend fun getAllNewsByCategory(
        @Query("category") category: String,
        @Query("api_token") apiKey: String = "K4WG2faoRMhvESCd4oViM3FeiFKqz7Cwl7PPbf2R",
        @Query("language") language: String = "en",
        @Query("limit") limit: Int = 1
    ): NewsResponse
    @GET("news/similar/{uuid}")
    suspend fun getSimilarStories(
        @Path("uuid") uuid: String,
        @Query("api_token") apiKey: String = "K4WG2faoRMhvESCd4oViM3FeiFKqz7Cwl7PPbf2R"
    ): NewsResponse
}