package com.example.fetch_timothyliu_android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkItem(
    @Json(name = "id") val id: Int,
    @Json(name = "listId") val listId: Int,
    @Json(name = "name") val name: String?
)

@Entity(tableName = "items")
data class Item(
    @PrimaryKey val id: Int,
    val listId: Int,
    val name: String
)

fun NetworkItem.toDatabaseEntity(): Item? {
    return if (!name.isNullOrBlank()) {
        Item(id = this.id, listId = this.listId, name = this.name)
    } else {
        null
    }
}

fun List<NetworkItem>.toDatabaseEntityList(): List<Item> {
    return this.mapNotNull { it.toDatabaseEntity() }
}
