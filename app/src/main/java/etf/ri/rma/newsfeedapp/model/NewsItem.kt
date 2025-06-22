package etf.ri.rma.newsfeedapp.model

import etf.ri.rma.newsfeedapp.model.TagWrapper

data class NewsItem(
    val uuid: String,
    val title: String,
    val snippet: String,
    val imageUrl: String?,
    val category: String,
    var isFeatured: Boolean,
    val source: String,
    val publishedDate: String,
    val imageTags: ArrayList<TagWrapper> = arrayListOf()
) {
    val uniqueId: String get() = "$uuid-$category"
}
