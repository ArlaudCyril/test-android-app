package com.Lyber.dev.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.Lyber.dev.models.EnterPhoneResponse
import com.Lyber.dev.models.MessageResponse
import com.Lyber.dev.network.RestClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUpViewModel : NetworkViewModel() {


    private var _mobileNumber = ""
    var mobileNumber: String
        get() = _mobileNumber
        set(value) {
            _mobileNumber = value
        }

    private var _email: String = ""
    var email
        get() = _email
        set(value) {
            _email = value
        }

    private var _countryCode = "+33"
    var countryCode: String
        get() = _countryCode
        set(value) {
            _countryCode = value
        }

    private var _password: String = ""
    var password: String
        get() = _password
        set(value) {
            _password = value
        }

    private var _createPin = ""
    var createPin: String
        get() = _createPin
        set(value) {
            _createPin = value
        }

    private var _confirmPin = ""
    var confirmPin: String
        get() = _confirmPin
        set(value) {
            _confirmPin = value
        }

    private var _forLogin: Boolean = false
    var forLogin
        get() = _forLogin
        set(value) {
            _forLogin = value
        }

    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, exception ->
        /* handle exception */
    }

    private var _enterPhoneResponse: MutableLiveData<EnterPhoneResponse> = MutableLiveData()
    val enterPhoneResponse get() = _enterPhoneResponse

    private var _enterOtpResponse: MutableLiveData<MessageResponse> = MutableLiveData()
    val enterOtpResponse get() = _enterOtpResponse

    private var _resendOtp: MutableLiveData<MessageResponse> = MutableLiveData()
    val resendOtp get() = _resendOtp

    private var _setPinResponse: MutableLiveData<MessageResponse> = MutableLiveData()
    val setPinResponse get() = _setPinResponse

    private var _activateFaceIdResponse: MutableLiveData<MessageResponse> = MutableLiveData()
    val activateFaceId get() = _activateFaceIdResponse

//    private var _enableNotificationResponse: MutableLiveData<MessageResponse> = MutableLiveData()
//    val enableNotificationResponse get() = _enableNotificationResponse


    private var _emailVerificationResponse: MutableLiveData<MessageResponse> = MutableLiveData()
    val emailVerificationResponse get() = _emailVerificationResponse

    private var _verifyPinResponse = MutableLiveData<MessageResponse>()
    val verifyPinResponse get() = _verifyPinResponse

//    fun enterPhone(deviceType: String = "ANDROID") {
//        try {
//            viewModelScope.launch(exceptionHandler) {
//                val hashMap: HashMap<String, Any> = hashMapOf()
//                hashMap["phone_no"] = mobileNumber
//                hashMap["country_code"] = countryCode
//                hashMap["device_type"] = deviceType
//                val res = RestClient.get().enterPhone(hashMap)
//                if (res.isSuccessful)
//                    _enterPhoneResponse.postValue(res.body())
//                else listener?.onRetrofitError(errorCode, res.errorBody())
//            }
//        } catch (e: Exception) {
//            listener?.onError()
//        }
//    }

//    fun login(deviceType: String = "ANDROID") {
//        try {
//            viewModelScope.launch(exceptionHandler) {
//                val hashMap: HashMap<String, Any> = hashMapOf()
//
//                if (email.isEmpty()) {
//                    hashMap["phone_no"] = mobileNumber
//                    hashMap["country_code"] = countryCode
//                } else {
//                    hashMap["email"] = email
//                }
//                hashMap["device_type"] = deviceType
//                val res = RestClient.get().login(hashMap)
//                if (res.isSuccessful)
//                    _enterPhoneResponse.postValue(res.body())
//                else listener?.onRetrofitError(errorCode, res.errorBody())
//            }
//        } catch (e: Exception) {
//            listener?.onError()
//        }
//    }

//    fun enterOtp(otp: String) {
//        try {
//            viewModelScope.launch(exceptionHandler) {
//                val res = RestClient.get().enterOtp(hashMapOf("otp" to otp.toInt()))
//                if (res.isSuccessful)
//                    _enterOtpResponse.postValue(res.body())
//                else listener?.onRetrofitError(errorCode, res.errorBody())
//            }
//        } catch (e: Exception) {
//            listener?.onError()
//        }
//    }

//    fun resendOtp() {
//        try {
//            viewModelScope.launch(exceptionHandler) {
//                val res = RestClient.get().resendOtp()
//                if (res.isSuccessful)
//                    _resendOtp.postValue(res.body())
//                else listener?.onRetrofitError(errorCode, res.errorBody())
//            }
//        } catch (e: Exception) {
//            listener?.onError()
//        }
//    }

//    fun setPin() {
//        try {
//            viewModelScope.launch(exceptionHandler) {
//                val res = RestClient.get()
//                    .setPin(hashMapOf("login_pin" to confirmPin.toInt()))
//                if (res.isSuccessful)
//                    _setPinResponse.postValue(res.body())
//                else listener?.onRetrofitError(errorCode, res.errorBody())
//            }
//        } catch (e: Exception) {
//            listener?.onError()
//        }
//    }


//    fun enableNotification(enable: Boolean = true) {
//        try {
//            viewModelScope.launch(exceptionHandler) {
//                val res = RestClient.get()
//                    .enableNotification(hashMapOf("is_push_enabled" to if (enable) "1" else "2"))
//                if (res.isSuccessful)
//                    _enableNotificationResponse.postValue(res.body())
//                else listener?.onRetrofitError(res.errorBody())
//
//            }
//        } catch (e: Exception) {
//            listener?.onError()
//        }
//    }

//    fun emailVerification() {
//        try {
//            CoroutineScope(Dispatchers.Main).launch {
//                val res = RestClient.get().checkEmailVerification()
//                if (res.isSuccessful) _emailVerificationResponse.postValue(res.body())
//                else listener?.onRetrofitError(errorCode, res.errorBody())
//            }
//        } catch (e: Exception) {
//            listener?.onError()
//        }
//    }

//    fun verifyPin(pin: String) {
//        viewModelScope.launch {
//            val res = RestClient.get().verifyPin(hashMapOf("login_pin" to pin.toInt()))
//            if (res.isSuccessful)
//                _verifyPinResponse.postValue(res.body())
//            else listener?.onRetrofitError(errorCode, res.errorBody())
//        }
//    }

    companion object {
        private const val TAG = "SignUpViewModel"
    }

}