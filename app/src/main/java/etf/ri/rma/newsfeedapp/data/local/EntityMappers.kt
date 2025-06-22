package etf.ri.rma.newsfeedapp.data.local

import etf.ri.rma.newsfeedapp.model.NewsItem

fun NewsItem.toEntity(): NewsEntity {
    return NewsEntity(
        uuid = uuid,
        title = title,
        snippet = snippet,
        imageUrl = imageUrl,
        category = category,
        isFeatured = isFeatured,
        source = source,
        publishedDate = publishedDate
    )
}
