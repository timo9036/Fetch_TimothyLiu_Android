package com.example.fetch_timothyliu_android.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.fetch_timothyliu_android.data.model.Item
import com.example.fetch_timothyliu_android.data.repository.ItemRepository
import com.example.fetch_timothyliu_android.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import java.io.IOException

@ExperimentalCoroutinesApi
class ItemViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: ItemRepository
    private lateinit var viewModel: ItemViewModel
    private lateinit var repositoryItemsLiveData: MutableLiveData<List<Item>>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockRepository = mock()

        repositoryItemsLiveData = MutableLiveData()
        whenever(mockRepository.items).thenReturn(repositoryItemsLiveData)

        viewModel = ItemViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `init - triggers initial actions if any`() = runTest {
        whenever(mockRepository.refreshItems()).thenReturn(Unit)

        viewModel = ItemViewModel(mockRepository)

        verify(mockRepository, times(1)).refreshItems()
    }

    @Test
    fun `refreshData success - calls repository refreshItems, no error`() = runTest(testDispatcher) {
        whenever(mockRepository.refreshItems()).thenReturn(Unit)

        clearInvocations(mockRepository)

        viewModel.refreshData()
        advanceUntilIdle()

        verify(mockRepository, times(1)).refreshItems()
        val errorMessage = viewModel.errorMessage.getOrAwaitValue()
        assertNull("Error message should be null on success", errorMessage)
    }

    @Test
    fun `items LiveData - observes repository items`() {
        val testData = listOf(Item(10, 1, "Test Item"))

        repositoryItemsLiveData.postValue(testData)

        val observedData = viewModel.items.getOrAwaitValue()
        assertEquals(testData, observedData)
    }

    @Test
    fun `refreshData network error - calls repository refreshItems, sets error message`() = runTest(testDispatcher) {
        val networkError = IOException("Network failed")

        doAnswer {
            throw networkError
        }.`when`(mockRepository).refreshItems()

        repositoryItemsLiveData.postValue(emptyList())
        clearInvocations(mockRepository)

        viewModel.refreshData()
        advanceUntilIdle()

        verify(mockRepository, atLeastOnce()).refreshItems()
        val errorMessage = viewModel.errorMessage.getOrAwaitValue()
        assertNotNull("Error message should not be null", errorMessage)
        assertTrue(
            "Error message should indicate network error and no cache. Was: '$errorMessage'",
            errorMessage!!.contains("Network error and no cached data available")
        )
    }

    @Test
    fun `onErrorMessageShown - clears error message LiveData`() = runTest(testDispatcher) {

        val setupError = RuntimeException("Setup error for test (Using RuntimeException)")

        doAnswer {
            throw setupError
        }.`when`(mockRepository).refreshItems()

        repositoryItemsLiveData.postValue(emptyList())
        clearInvocations(mockRepository)

        viewModel.refreshData()

        advanceUntilIdle()
        val initialError = viewModel.errorMessage.getOrAwaitValue()
        assertNotNull("PRECONDITION FAILED: Error message should be set before testing clear", initialError)

        viewModel.onErrorMessageShown()

        val finalErrorMessage = viewModel.errorMessage.getOrAwaitValue()
        assertNull("Error message should be cleared after onErrorMessageShown is called", finalErrorMessage)
    }
}