package etf.ri.rma.newsfeedapp.data.local

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.model.TagWrapper

data class NewsWithTags(
    @Embedded val news: NewsEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NewsTagCrossRef::class,
            parentColumn = "newsId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)
