package etf.ri.rma.newsfeedapp.data

import etf.ri.rma.newsfeedapp.model.NewsItem

object NewsData {
    fun getAllNews(): List<NewsItem> {
        return List(20) { index ->
            val category = when (index % 3) {
                0 -> "Politika"
                1 -> "Sport"
                else -> "Nauka/tehnologija"
            }
            NewsItem(
                id = index.toString(),
                title = "Naslov vijesti $index",
                snippet = "Ovo je kratki opis vijesti broj $index.",
                imageUrl = null,
                category = category,
                isFeatured = index % 5 == 0,
                source = "Izvor $index",
                publishedDate = "${(index % 30) + 1}-01-2025"
            )
        }
    }
}