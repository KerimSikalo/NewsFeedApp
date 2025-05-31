package etf.ri.rma.newsfeedapp.data.network

data class NewsResponse(val data: List<NewsDto>)

data class NewsDto(
    val uuid: String,
    val title: String,
    val snippet: String,
    val image_url: String?,
    val categories: List<String>,
    val source: String,
    val published_at: String
)

