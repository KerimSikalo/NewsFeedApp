package etf.ri.rma.newsfeedapp.data.network

import android.util.Base64
import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidImageURLException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class ImaggaResponse(val result: ImaggaResult)
data class ImaggaResult(val tags: List<TagWrapper>)
data class TagWrapper(val tag: Map<String, String>)

class ImagaDAO {
    private lateinit var apiService: ImagaApiService
    private val cache = mutableMapOf<String, List<String>>()
    private val mutex = Mutex()
    fun setApiService(service: ImagaApiService) { this.apiService = service }

    suspend fun getTags(imageURL: String): List<String> = mutex.withLock {
        if (!imageURL.startsWith("http")) throw InvalidImageURLException()
        cache[imageURL]?.let { return it }

        val credentials = "acc_e5b9d98e85addbd:e838185015c38d77af1ff2cd5309fabb"
        val basicAuth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        val response = apiService.getTags(imageURL, basicAuth)
        val tags = response.result.tags.mapNotNull { it.tag["en"] }
        cache[imageURL] = tags
        return tags
    }

    companion object
}
