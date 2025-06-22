package etf.ri.rma.newsfeedapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import etf.ri.rma.newsfeedapp.data.local.NewsEntity
import etf.ri.rma.newsfeedapp.data.local.NewsTagCrossRef
import etf.ri.rma.newsfeedapp.data.local.NewsWithTags
import etf.ri.rma.newsfeedapp.data.local.TagEntity
import etf.ri.rma.newsfeedapp.data.local.toEntity
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.model.TagWrapper

@Dao
interface SavedNewsDAO {
    @Transaction
    @Query("SELECT * FROM News")
    suspend fun getAllNewsWithTags(): List<NewsWithTags>

    suspend fun allNews(): List<NewsItem> {
        return getAllNewsWithTags().map { it.toNewsItem() }
    }

    @Transaction
    @Query("SELECT * FROM News WHERE category = :category")
    suspend fun getNewsWithCategoryRaw(category: String): List<NewsWithTags>

    suspend fun getNewsWithCategory(category: String): List<NewsItem> {
        return getNewsWithCategoryRaw(category).map { it.toNewsItem() }
    }

    @Query("SELECT id FROM News WHERE uuid = :uuid")
    suspend fun getNewsIdByUuid(uuid: String): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNews(news: NewsEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewsTagCrossRef(crossRef: NewsTagCrossRef)

    @Transaction
    suspend fun saveNewsAndReturnId(newsItem: NewsItem): Int? {
        val entity = newsItem.toEntity()
        val id = insertNews(entity)
        return if (id != -1L) id.toInt() else getNewsIdByUuid(newsItem.uuid)
    }

    suspend fun saveNews(newsItem: NewsItem): Boolean {
        val existing = getNewsIdByUuid(newsItem.uuid)
        if (existing != null) return false
        val id = insertNews(newsItem.toEntity())
        return id != -1L
    }

    @Transaction
    suspend fun addTags(tags: List<String>, newsId: Int): Int {
        var added = 0
        for (tagValue in tags.distinct()) {
            val trimmed = tagValue.trim()
            if (trimmed.isEmpty()) continue
            val existingTagId = getTagIdByValue(trimmed)?.toInt()
            val tagId = if (existingTagId != null) {
                existingTagId
            } else {
                val insertedId = insertTag(TagEntity(value = trimmed))
                if (insertedId != -1L) insertedId.toInt() else getTagIdByValue(trimmed)?.toInt() ?: continue
            }
            try {
                insertNewsTagCrossRef(NewsTagCrossRef(newsId, tagId))
                added++
            } catch (_: Exception) {
            }
        }
        return added
    }

    @Query("SELECT COUNT(*) FROM NewsTags WHERE newsId = :newsId AND tagId = :tagId")
    suspend fun checkNewsTagExists(newsId: Int, tagId: Int): Boolean

    @Query("SELECT id FROM Tags WHERE value = :value")
    suspend fun getTagIdByValue(value: String): Long?

    @Transaction
    @Query("SELECT value FROM Tags INNER JOIN NewsTags ON Tags.id = NewsTags.tagId WHERE NewsTags.newsId = :newsId")
    suspend fun getTags(newsId: Int): List<String>

    @Transaction
    @Query("""
    SELECT DISTINCT News.* FROM News
    INNER JOIN NewsTags ON News.id = NewsTags.newsId
    INNER JOIN Tags ON Tags.id = NewsTags.tagId
    WHERE Tags.value IN (:tags)
    ORDER BY publishedDate DESC
""")
    suspend fun getSimilarNews(tags: List<String>): List<NewsItem> {
        return getSimilarNewsRaw(tags).map { it.toNewsItem() }
    }

    @Transaction
    @Query("""
    SELECT DISTINCT News.* FROM News
    INNER JOIN NewsTags ON News.id = NewsTags.newsId
    INNER JOIN Tags ON Tags.id = NewsTags.tagId
    WHERE Tags.value IN (:tags)
    ORDER BY publishedDate DESC
""")
    suspend fun getSimilarNewsRaw(tags: List<String>): List<NewsWithTags>

}


fun NewsWithTags.toNewsItem(): NewsItem {
    return NewsItem(
        uuid = news.uuid,
        title = news.title,
        snippet = news.snippet,
        imageUrl = news.imageUrl,
        category = news.category,
        isFeatured = news.isFeatured,
        source = news.source,
        publishedDate = news.publishedDate,
        imageTags = ArrayList(tags.map { TagWrapper(it.value) })
    )
}
