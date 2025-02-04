package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.ComponentCallbacks
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver(), ComponentCallbacks {
    private val remindersLocalRepository: ReminderDataSource by inject()
    private val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent.hasError()) {
                val errorMessage = when (geofencingEvent.errorCode) {
                    GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> context.getString(
                        R.string.geofence_not_available
                    )
                    GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> context.getString(
                        R.string.geofence_too_many_geofences
                    )
                    GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> context.getString(
                        R.string.geofence_too_many_pending_intents
                    )
                    else -> context.getString(R.string.geofence_unknown_error)
                }
                Log.e(TAG, errorMessage)
                return
            }

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.v(TAG, context.getString(R.string.geofence_entered))
                when {
                    geofencingEvent.triggeringGeofences.isNotEmpty() -> {
                        for (geofence in geofencingEvent.triggeringGeofences) {
                            val fenceId = geofence.requestId
                            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                                val result = remindersLocalRepository.getReminder(fenceId)
                                if (result is Result.Success<ReminderDTO>) {
                                    val reminderDTO = result.data
                                    //send a notification to the user with the reminder details
                                    sendNotification(
                                        context, ReminderDataItem(
                                            reminderDTO.title,
                                            reminderDTO.description,
                                            reminderDTO.location,
                                            reminderDTO.latitude,
                                            reminderDTO.longitude,
                                            reminderDTO.id
                                        )
                                    )
                                }
                            }
                            Log.e(TAG, "Geofence entered: $fenceId")
                        }
                    }
                    else -> {
                        Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                        return
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
    }

    override fun onLowMemory() {
    }
}

private const val TAG = "GeofenceReceiver"
