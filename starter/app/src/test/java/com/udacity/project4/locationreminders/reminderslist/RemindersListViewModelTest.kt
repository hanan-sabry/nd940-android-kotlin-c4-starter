package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.husseinelfeky.moviesexplorer.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.utils.SingleLiveEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var remindersDataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecuteRule = InstantTaskExecutorRule()

    //initialize koin related code to be able to use it in the testing
    @Before
    fun init() {
        stopKoin()
        remindersDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            remindersDataSource
        )
    }

    @Test
    fun loadReminders_shouldReturnError() = runTest {
        //WHEN - An error occurs when retrieving reminders
        remindersDataSource.setShouldReturnError(true)

        //THEN - verify that an error occurred
        val result: Result<List<ReminderDTO>> = remindersDataSource.getReminders()

        assertThat(result, notNullValue())
        assertThat(
            result, `is`(Result.Error("Error while retrieving reminders"))
        )
    }

    @Test
    fun loadReminders_checkLoading(){
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.value, `is` (true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.value, `is` (false))
    }
}