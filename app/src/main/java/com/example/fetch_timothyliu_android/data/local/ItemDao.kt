package com.example.fetch_timothyliu_android.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fetch_timothyliu_android.data.model.Item

@Dao
interface ItemDao {

    @Query("SELECT * FROM items ORDER BY listId ASC, name ASC")
    fun getItemsSorted(): LiveData<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Item>)

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}