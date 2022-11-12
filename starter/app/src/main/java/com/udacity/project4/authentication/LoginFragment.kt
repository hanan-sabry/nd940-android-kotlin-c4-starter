package com.udacity.project4.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentLoginBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : BaseFragment() {

    override val _viewModel: LoginViewModel by viewModel()

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { res ->
            this.onSignInResult(res)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLoginBinding.inflate(inflater)
        binding.login.setOnClickListener {
            launchSignInFlow()
        }
//        observeAuthenticationState()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun launchSignInFlow() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            val providers = arrayListOf(
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.EmailBuilder().build()
            )

            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            signInLauncher.launch(signInIntent)
        } else {
            this.findNavController()
                .navigate(LoginFragmentDirections.actionLoginFragmentToRemindersActivity())
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            this.findNavController()
                .navigate(LoginFragmentDirections.actionLoginFragmentToRemindersActivity())
            Toast.makeText(
                context,
                "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                context,
                "Sign in unsuccessful ${response?.error?.errorCode}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

//    private fun observeAuthenticationState() {
//        _viewModel.authenticateState.observe(viewLifecycleOwner, Observer {
//            if (it.equals(LoginViewModel.AuthenticateState.AUTHENTICATED)) {
//                this.findNavController()
//                    .navigate(LoginFragmentDirections.actionLoginFragmentToRemindersActivity())
//            }
//        })
//    }
}