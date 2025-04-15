package com.example.fetch_timothyliu_android.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.fetch_timothyliu_android.data.local.LocalDataSource
import com.example.fetch_timothyliu_android.data.model.Item
import com.example.fetch_timothyliu_android.data.model.NetworkItem
import com.example.fetch_timothyliu_android.data.remote.RemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.mockito.kotlin.*
import java.io.IOException
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.never
import org.mockito.kotlin.any
import org.mockito.kotlin.doSuspendableAnswer
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class ItemRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockRemoteDataSource: RemoteDataSource
    private lateinit var mockLocalDataSource: LocalDataSource
    private lateinit var mockItemsLiveData: MutableLiveData<List<Item>>
    private lateinit var repository: ItemRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRemoteDataSource = mock()
        mockLocalDataSource = mock()

        mockItemsLiveData = MutableLiveData()
        whenever(mockLocalDataSource.getItems()).thenReturn(mockItemsLiveData)

        repository = ItemRepository(mockRemoteDataSource, mockLocalDataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `refreshItems success - fetches remote, clears local, saves filtered local`() = runTest(testDispatcher) {
        val networkItems = listOf(
            NetworkItem(1, 1, "Item 1"),
            NetworkItem(2, 1, " "),
            NetworkItem(3, 2, "Item 3"),
            NetworkItem(4, 2, null)
        )
        val expectedFilteredItems = listOf(
            Item(1, 1, "Item 1"),
            Item(3, 2, "Item 3")
        )
        whenever(mockRemoteDataSource.fetchItems()).thenReturn(networkItems)

        repository.refreshItems()


        verify(mockRemoteDataSource).fetchItems()
        verify(mockLocalDataSource).clearItems()
        verify(mockLocalDataSource).saveItems(eq(expectedFilteredItems))
    }



    @Test
    fun `items LiveData - returns LiveData from local data source`() {
        val expectedData = listOf(Item(1, 1, "Test"))

        mockItemsLiveData.value = expectedData

        val actualLiveData: LiveData<List<Item>> = repository.items

        assertEquals(mockItemsLiveData, actualLiveData)
        assertEquals(expectedData, actualLiveData.value)

        verify(mockLocalDataSource, times(1)).getItems()
    }

    @Test
    fun `refreshItems network error - throws IOException, does not clear or save local`() = runTest(testDispatcher) {
        val networkError = IOException("Network failed")

        whenever(mockRemoteDataSource.fetchItems()).doSuspendableAnswer {
            throw networkError
        }

        var exceptionThrown: Exception? = null
        try {
            repository.refreshItems()
        } catch (e: IOException) {
            exceptionThrown = e
        }

        assertNotNull("IOException should have been thrown", exceptionThrown)
        assertEquals(networkError.message, exceptionThrown?.message)

        verify(mockRemoteDataSource).fetchItems()
        verify(mockLocalDataSource, never()).clearItems()
        verify(mockLocalDataSource, never()).saveItems(any())
    }

}