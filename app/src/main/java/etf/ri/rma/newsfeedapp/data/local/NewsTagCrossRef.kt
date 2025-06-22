package etf.ri.rma.newsfeedapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "NewsTags",
    primaryKeys = ["newsId", "tagId"],
    foreignKeys = [
        ForeignKey(entity = NewsEntity::class, parentColumns = ["id"], childColumns = ["newsId"]),
        ForeignKey(entity = TagEntity::class, parentColumns = ["id"], childColumns = ["tagId"])
    ]
)
data class NewsTagCrossRef(
    val newsId: Int,
    val tagId: Int
)
