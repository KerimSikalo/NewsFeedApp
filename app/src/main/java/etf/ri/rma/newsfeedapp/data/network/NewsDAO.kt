package etf.ri.rma.newsfeedapp.data.network

import android.util.Log
import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidUUIDException
import etf.ri.rma.newsfeedapp.model.NewsItem
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

class NewsDAO {
    private var apiService = RetrofitInstance.newsApiService
    private val cache = mutableListOf<NewsItem>()
    private val lastFetchTimestamps = mutableMapOf<String, Long>()
    private var lastGlobalFetch: Long = 0L
    private val mutex = Mutex()
    private val similarStoriesCache = mutableMapOf<String, List<NewsItem>>()
    private val formatterInput = DateTimeFormatter.ISO_INSTANT
    private val formatterOutput = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    fun setApiService(service: NewsApiService) { this.apiService = service }

    suspend fun getTopStoriesByCategory(category: String): List<NewsItem> = mutex.withLock {
        val now = System.currentTimeMillis()
        val lastFetch = lastFetchTimestamps[category] ?: 0L
        val diffSeconds = (now - lastFetch) / 1000
        val cachedNews = cache.filter { it.category == category }.sortedByDescending { it.isFeatured }
        if (cachedNews.isNotEmpty() && diffSeconds < 30) { return cachedNews }
        val response = apiService.getTopNewsByCategory(category)
        val newItems = response.data.take(3).map { dto ->
            NewsItem(
                uuid = dto.uuid,
                title = dto.title,
                snippet = dto.snippet,
                imageUrl = dto.image_url,
                category = dto.categories.firstOrNull() ?: category,
                isFeatured = true,
                source = dto.source,
                publishedDate = formatterOutput.format(
                    Instant.parse(dto.published_at).atZone(ZoneId.systemDefault())
                )
            )
        }
        cache.forEach { it.isFeatured = false }
        val featuredNews = mutableListOf<NewsItem>()
        for (item in newItems) {
            val existing = cache.find { it.uuid == item.uuid }
            if (existing != null) {
                existing.isFeatured = true
                featuredNews.add(existing)
            } else {
                cache.add(0, item)
                featuredNews.add(item)
            }
        }
        lastFetchTimestamps[category] = now
        lastGlobalFetch = now
        return cache.filter { it.category == category }.sortedByDescending { it.isFeatured }
    }

    fun getAllStories(): List<NewsItem> = cache.toList()

    suspend fun getSimilarStories(uuid: String): List<NewsItem> = mutex.withLock {
        if (!isValidUUID(uuid)) throw InvalidUUIDException()
        similarStoriesCache[uuid]?.let { return it }
        val response = apiService.getSimilarStories(uuid)
        val similar = response.data.map { dto ->
            NewsItem(
                uuid = dto.uuid,
                title = dto.title,
                snippet = dto.snippet,
                imageUrl = dto.image_url,
                category = dto.categories.firstOrNull() ?: "general",
                isFeatured = false,
                source = dto.source,
                publishedDate = formatterOutput.format(
                    Instant.parse(dto.published_at).atZone(ZoneId.systemDefault())
                )
            )
        }
        similarStoriesCache[uuid] = similar
        return similar
    }

    private fun isValidUUID(uuid: String): Boolean = try {
        UUID.fromString(uuid)
        true
    } catch (e: Exception) {
        false
    }

    suspend fun loadInitialNews(imageDAO: ImagaDAO = RetrofitInstance.defaultImagaDAO) {
        mutex.withLock {
        if (cache.isNotEmpty()) return@withLock
        Log.d("NewsDAO", "Učitavam hardkodirane vijesti")
        val initialNews = listOf(
            NewsItem(
                uuid = "4aa14695-ac77-47cc-8e42-2f90589228d9",
                title = "Nobody Cared About This Fender Model. Then the 1990s Happened and It Was All the Rage",
                snippet = "Fender has its share of iconic guitar models, from the Telecaster to the Stratocaster to the Jazzmaster – all of which made their debut in the 1950s. But one ...",
                imageUrl = "https://www.ultimate-guitar.com/static/article/news/5/178895_0_wide_ver1748185322.jpg",
                category = "entertainment",
                isFeatured = false,
                source = "ultimate-guitar.com",
                publishedDate = "25-05-2025"
            ),
            NewsItem(
                uuid = "ef530af9-83fd-449b-993c-7bdd534ae192",
                title = "Canada Post to meet with union Sunday amid national overtime ban by mail workers",
                snippet = "Canada Post was set to meet with negotiators from its union Sunday amid warnings of mail delivery delays tied to a national ban on overtime for postal workers.\n...",
                imageUrl = "https://i.cbc.ca/1.7539615.1747782101!/cpImage/httpImage/image.jpg_gen/derivatives/16x9_1180/canada-post-strike-20241217.jpg?im=Resize%3D620",
                category = "general",
                isFeatured = false,
                source = "cbc.ca",
                publishedDate = "25-05-2025"
            ),
            NewsItem(
                uuid = "76406879-ef73-4025-ae3d-35c0ad3d52db",
                title = "Take-Two Interactive Software (TTWO) Announces Successful Stock Offering",
                snippet = "On May 20, 2025, Take-Two Interactive Software, Inc. (TTWO, Financial) entered into an underwriting agreement with J.P. Morgan Securities LLC and Goldman Sachs ...",
                imageUrl = "https://static.gurufocus.com/logos/0C00000CIG.png?5",
                category = "business",
                isFeatured = false,
                source = "gurufocus.com",
                publishedDate = "25-05-2025"
            ),
            NewsItem(
                uuid = "3c438835-26b7-4575-ad53-7e0dcdcf1777",
                title = "Amorim apologises to Man Utd fans as Emery fumes at disallowed goal after missing out on UCL",
                snippet = "Manchester United boss Ruben Amorim has apologised to fans inside Old Trafford after failing to win the Europa League, despite beating Aston Villa on the final ...",
                imageUrl = "https://www.101greatgoals.com/wp-content/uploads/2024/12/Amorim-2.jpg",
                category = "sports",
                isFeatured = false,
                source = "101greatgoals.com",
                publishedDate = "25-05-2025"
            ),
            NewsItem(
                uuid = "1680e3e9-bb2a-4e89-9198-3c124c31004b",
                title = "Post a Want",
                snippet = "How do I post a want for a part for a piece of equipment\n\nClick to expand...",
                imageUrl = "https://forums.qrz.com/styles/default/xenforo/avatars/avatar_m.png",
                category = "tech",
                isFeatured = false,
                source = "forums.qrz.com",
                publishedDate = "25-05-2025"
            ),
            NewsItem(
                uuid = "c52c160e-4a3b-4e52-9456-e86aa22a0ca1",
                title = "NZ Politics Daily - 30 November 2021",
                snippet = "Today's NZPD testimonial from, Senior Associate, Institute for Governance and Policy Studies:\n\n\"While working as a public policy advisor, NZ Politics Daily ...",
                imageUrl = "https://thedailyblog.co.nz/wp-content/uploads/2021/11/Screen-Shot-2021-11-30-at-9.36.14-AM.png",
                category = "politics",
                isFeatured = false,
                source = "thedailyblog.co.nz",
                publishedDate = "01-12-2021"
            ),
            NewsItem(
                uuid = "91af85ea-4cd3-4e5d-b297-63a5cf2f83d9",
                title = "Getting excited about science with annual science festival",
                snippet = "EAST LANSING, Mich. (WILX) - The 13th annual Michigan State University Science Festival kicked off this weekend.\n\nThe Festival held the STEAM Expo day, where th...",
                imageUrl = "https://gray-wilx-prod.gtv-cdn.com/resizer/v2/QXFIUUQNPVDMDO64RLCMJVU4RY.png?auth=c6b06f3048552636c8a2196f8747b679baf9c2c29878cb95272b0d60828e21c2&width=1200&height=600&smart=true",
                category = "science",
                isFeatured = false,
                source = "app.buzzsumo.com",
                publishedDate = "06-04-2025"
            ),
            NewsItem(
                uuid = "f5416fb4-afe2-4918-bbf1-1cabc7aadd55",
                title = "Teladoc Health Acquires Catapult Health for $65M",
                snippet = "What You Should Know:\n\n– Teladoc Health (NYSE: TDOC) today announced the acquisition of Catapult Health, a provider of virtual preventive care service in an a...",
                imageUrl = "https://hitconsultant.net/wp-content/uploads/2025/02/catapult-health-logo.png",
                category = "health",
                isFeatured = false,
                source = "hitconsultant.net",
                publishedDate = "05-02-2025"
            ),
            NewsItem(
                uuid = "c6d70ae4-e487-4cd4-9385-95d76f8c31fd",
                title = "When Fast Food Is Good Food",
                snippet = "The United States is a world leader in economics, military might and political influence. It also dominates fast-food consumption, with an annual expenditure of...",
                imageUrl = "https://progressivegrocer.com/images/v/16_x_9_480/files/2025-01/grain_bowls_2_teaser.jpg",
                category = "food",
                isFeatured = false,
                source = "progressivegrocer.com",
                publishedDate = "16-01-2025"
            ),
            NewsItem(
                uuid = "56bc7811-bdc7-4cc1-a2c4-f9488438a814",
                title = "Omicron Depresses Demand for International Travel - Travel Weekly",
                snippet = "Demand for international travel has plunged in the week since the omicron Covid-19 variant emerged as a global concern.\n\nExcerpt from Travel Weekly\n\nDemand for ...",
                imageUrl = "https://www.hotelnewsresource.com/theme/Nevistas2/images/icon.png",
                category = "travel",
                isFeatured = false,
                source = "hotelnewsresource.com",
                publishedDate = "03-12-2021"
            )
        ).toMutableList()
        for (item in initialNews) {
            val url = item.imageUrl
            if (!url.isNullOrBlank()) {
                try {
                    val tags = imageDAO.getTags(url)
                    item.imageTags.addAll(tags)
                    Log.d("NewsDAO", "Dodani tagovi za vijest ${item.uuid}: $tags")
                } catch (e: Exception) {
                    Log.e("NewsDAO", "Greška pri dohvaćanju tagova za ${item.uuid}: ${e.message}")
                }
            }
        }
        cache.addAll(initialNews)
        Log.d("NewsDAO", "Ukupno vijesti u cache: ${cache.size}")
    }
  }
}

