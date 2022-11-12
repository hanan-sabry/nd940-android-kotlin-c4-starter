package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.map
import com.udacity.project4.base.BaseViewModel

class LoginViewModel(app: Application) : BaseViewModel(app) {

    enum class AuthenticateState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    val authenticateState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticateState.AUTHENTICATED
        } else {
            AuthenticateState.UNAUTHENTICATED
        }
    }
}