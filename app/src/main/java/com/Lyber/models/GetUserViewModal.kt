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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
 class GetUserViewModal() : ViewModel() {

     private var isFetching = false


     private val networkViewModel = NetworkViewModel()

     private val _userLiveData = MutableLiveData<User>()
     val userLiveData: LiveData<User> get() = _userLiveData
    init {
        startFetchingUserData()
    }

    private fun startFetchingUserData() {
        if (App.prefsManager.user?.kycStatus != "OK" || App.prefsManager.user?.yousignStatus != "SIGNED") {
           Log.d("Loop","Started")
            viewModelScope.launch {
            // Run a loop until kycOK becomes true
            while (!PortfolioHomeFragment().kycOK) {
                // Check if the access token is not empty
                if (App.prefsManager.accessToken.isNotEmpty()) {
                    getUser()
                    // Delay for 3 seconds
                    delay(3 * 1000)
                }
            }
        }}

//        if (App.prefsManager.user?.kycStatus != "OK" || App.prefsManager.user?.yousignStatus != "SIGNED") {
//            Log.d("Loop", "Started")
//            if (!isFetching) { // Check if already fetching
//                isFetching = true
//                viewModelScope.launch {
//                    // Run a loop until kycOK becomes true
//                    while (!PortfolioHomeFragment().kycOK) {
//                        // Check if the access token is not empty
//                        if (App.prefsManager.accessToken.isNotEmpty()) {
//                            getUser()
//                            // Delay for 3 seconds
//                            delay(3 * 1000)
//                        }
//                    }
//                    isFetching = false // Reset the flag once done
//                }
//            }
//        }
    }

    private suspend fun getUser() {
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
}
