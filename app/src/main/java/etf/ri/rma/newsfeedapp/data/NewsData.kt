package etf.ri.rma.newsfeedapp.data

/*

import etf.ri.rma.newsfeedapp.model.NewsItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object NewsData {
    fun getAllNews(): List<NewsItem> {
        return List(20) { index ->
            val category = when (index % 3) {
                0 -> "Politika"
                1 -> "Sport"
                else -> "Nauka/tehnologija"
            }
            val date = LocalDate.of(2025, 1, (index % 30) + 1)
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val formattedDate = date.format(formatter)

            NewsItem(
                id = index.toString(),
                title = "Vijest $index: naslov specifičan za $index",
                snippet = "Opis $index: detalji za vijest $index...",
                imageUrl = null,
                category = category,
                isFeatured = index % 5 == 0,
                source = "Izvor: $index",
                publishedDate = formattedDate
            )
        }
    }
}
*/