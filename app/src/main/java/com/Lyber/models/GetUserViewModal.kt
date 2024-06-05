package com.Lyber.models

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyber.ui.portfolio.fragment.PortfolioHomeFragment
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.viewmodels.NetworkViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GetUserViewModal() : ViewModel() {

    private val networkViewModel = NetworkViewModel()

    private val _userLiveData = MutableLiveData<com.Lyber.models.User>()
    val userLiveData: LiveData<com.Lyber.models.User> get() = _userLiveData
    private var fetchingJob: Job? = null

    init {
        startFetchingUserData()
    }

     fun startFetchingUserData() {
        try {
            if (fetchingJob != null)
                fetchingJob?.cancel()
        } catch (_: Exception) {

        }
        if (App.prefsManager.user?.kycStatus != "OK" || App.prefsManager.user?.yousignStatus != "SIGNED") {
            Log.d("Loop", "Started")
            fetchingJob = viewModelScope.launch {
                // Run a loop until kycOK becomes true
                val kycOK =
                    PortfolioHomeFragment().kycOK  // This should be passed or observed correctly
                while (!kycOK) {
                    // Check if the access token is not empty
                    if (App.prefsManager.accessToken.isNotEmpty()) {
                        getUser()
                        // Delay for 3 seconds
                        delay(3 * 1000)
                    }
                }
            }
        }

    }

    private fun getUser() {
        // Fetch user data logic here
        // Update kycOK based on the fetched data
        // For example:
        // kycOK = fetchedData.kycStatus == "COMPLETED"

        networkViewModel.getUser() // Assuming this returns a User object
        networkViewModel.getUserResponse.observeForever { user ->
            _userLiveData.postValue(user.data)
            // Assuming user?.kycStatus is the desired check for kycOK
        }
    }

    fun stopFetchingUserData() {
        try {
            if (fetchingJob != null)
                fetchingJob?.cancel()
        } catch (_: Exception) {

        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            if (fetchingJob != null)
                fetchingJob?.cancel()
        } catch (_: Exception) {

        }
    }
}