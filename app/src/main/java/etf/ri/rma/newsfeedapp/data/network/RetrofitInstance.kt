package etf.ri.rma.newsfeedapp.data.network

import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val newsApiService: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.thenewsapi.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }
    val imaggaApiService: ImagaApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.imagga.com/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImagaApiService::class.java)
    }
    val defaultNewsDAO: NewsDAO by lazy { NewsDAO().apply { setApiService(newsApiService) } }
    val defaultImagaDAO: ImagaDAO by lazy { ImagaDAO().apply { setApiService(imaggaApiService) } }
}