package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersDao: RemindersDao
    private lateinit var reminderDTO: ReminderDTO
    private lateinit var remindersRepository: RemindersLocalRepository

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        remindersDao = remindersDatabase.reminderDao()
        remindersRepository = RemindersLocalRepository(remindersDao)
        reminderDTO = getFakeReminderDTO()
    }

    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        //GIVEN - a new reminder saved in the database
        remindersRepository.saveReminder(reminderDTO)

        //WHEN - Reminder is retrieved by id
        val result = remindersRepository.getReminder(reminderDTO.id)

        //THEN - same reminder is retrieved
        assertThat(result as Result.Success<ReminderDTO>, notNullValue())
        assertThat(result.data.id, `is`(reminderDTO.id))
        assertThat(result.data.title, `is`(reminderDTO.title))
        assertThat(result.data.description, `is`(reminderDTO.description))
        assertThat(result.data.location, `is`(reminderDTO.location))
        assertThat(result.data.longitude, `is`(reminderDTO.longitude))
        assertThat(result.data.latitude, `is`(reminderDTO.latitude))
    }

    @Test
    fun getReminder_ReturnDataNotFound() = runBlocking {
        //GIVEN - get reminder by id not found in database
        //WHEN - reminder is retrieved by id
        val result = remindersRepository.getReminder("not found id")

        //THEN - error is returned
        assertThat((result as Result.Error).message, `is`("Reminder not found!"))
    }

    @After
    fun closeDB() {
        remindersDatabase.close()
    }


    private fun getFakeReminderDTO(): ReminderDTO {
        return ReminderDTO(
            "Test reminder",
            "Test description",
            "test location",
            33.23,
            33.34
        )
    }
}