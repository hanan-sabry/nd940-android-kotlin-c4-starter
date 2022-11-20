package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersDao: RemindersDao
    private lateinit var reminderDTO: ReminderDTO
    private lateinit var reminderList: List<ReminderDTO>

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()

        remindersDao = remindersDatabase.reminderDao()
        reminderDTO = getFakeReminderDTO()
        reminderList = getFakeReminderDTOList()
    }

    @After
    fun closeDB() {
        remindersDatabase.close()
    }

    @Test
    fun saveReminder_GetById() = runBlocking{
        //GIVEN - save fake reminder
        remindersDao.saveReminder(reminderDTO)

        //WHEN - get the reminder by id from database
        val result = remindersDao.getReminderById(reminderDTO.id)

        //THEN - the loaded data contains the expected values
        assertThat(result as ReminderDTO, CoreMatchers.notNullValue())
        assertThat(result.id, `is` (reminderDTO.id))
        assertThat(result.title, `is` (reminderDTO.title))
        assertThat(result.description, `is` (reminderDTO.description))
        assertThat(result.location, `is` (reminderDTO.location))
        assertThat(result.latitude, `is` (reminderDTO.latitude))
        assertThat(result.longitude, `is` (reminderDTO.longitude))
    }

    @Test
    fun getAllReminders_returnSameResult() = runBlocking {
        //GIVEN - save 5 reminders
        reminderList.forEach{
            remindersDao.saveReminder(it)
        }
        //WHEN - get all reminders
        val result = remindersDao.getReminders()

        //THEN - return all saved reminders
        assertThat(result.size, `is` (reminderList.size))
    }

    private fun getFakeReminderDTO() : ReminderDTO{
        return ReminderDTO(
                "Test reminder",
                "Test description",
                "test location",
                33.23,
                33.34
            )
    }

    private fun getFakeReminderDTOList(): List<ReminderDTO> {
        return listOf (
            ReminderDTO(
                "Test reminder1",
                "Test description1",
                "test location1",
                33.23,
                33.34
            ),
            ReminderDTO(
                "Test reminder2",
                "Test description2",
                "test location2",
                33.23,
                33.34
            ),
            ReminderDTO(
                "Test reminder3",
                "Test description3",
                "test location3",
                33.23,
                33.34
            ),
            ReminderDTO(
                "Test reminder4",
                "Test description4",
                "test location4",
                33.23,
                33.34
            ),
            ReminderDTO(
                "Test reminder5",
                "Test description5",
                "test location5",
                33.23,
                33.34
            )
        )
    }

}