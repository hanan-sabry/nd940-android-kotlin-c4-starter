package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private var reminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    private var shouldReturnError = false


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (!shouldReturnError) {
            return Result.Success(reminders)
        } else {
            return Result.Error("Error while retrieving reminders")
        }
    }

//    override suspend fun getReminders(): Result<List<ReminderDTO>> {
//        return try {
//            if(shouldReturnError) {
//                throw Exception("Reminders not found")
//            }
//            Result.Success(ArrayList(reminders))
//        } catch (ex: Exception) {
//            Result.Error(ex.localizedMessage)
//        }
//    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return if (!shouldReturnError) {
            val reminder = reminders.firstOrNull {
                it.id == id
            }
            if (reminder != null) {
                Result.Success(reminder)
            } else {
                Result.Error("Reminder not found!")
            }
        } else {
            Result.Error("Error, Can't retrieve reminder")
        }
    }

    override suspend fun deleteAllReminders() {
        reminders = mutableListOf()
    }

    fun setShouldReturnError(value: Boolean) {
        shouldReturnError = value
    }

}