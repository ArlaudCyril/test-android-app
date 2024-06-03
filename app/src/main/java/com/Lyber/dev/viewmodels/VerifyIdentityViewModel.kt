package com.Lyber.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyber.models.InitiateKycResponse
import com.Lyber.models.KycStatusResponse
import com.Lyber.models.MessageResponse
import com.Lyber.models.UploadResponse
import com.Lyber.network.RestClient
import com.Lyber.ui.fragments.VerifyYourIdentityFragment.Companion.NONE
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class VerifyIdentityViewModel : ViewModel() {


    private var _identityPaperPhoto: String? = null
    var paperPhoto
        get() = _identityPaperPhoto
        set(value) {
            _identityPaperPhoto = value
        }

    private var _selfiePhoto: String? = null
    var selfiePhoto
        get() = _selfiePhoto
        set(value) {
            _selfiePhoto = value
        }

    private var _imageCaptured: Int = NONE
    var imageCaptured
        get() = _imageCaptured
        set(value) {
            _imageCaptured = value
        }

    var listener: RestClient.OnRetrofitError? = null

    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        /* handle exception here! */
    }
    private var _uploadResponse = MutableLiveData<UploadResponse>()
    val uploadResponse get() = _uploadResponse

    private var _kycStatusResponse = MutableLiveData<KycStatusResponse>()
    val kycStatusResponse get() = _kycStatusResponse

    private var _kycInitiatedResponse = MutableLiveData<InitiateKycResponse>()
    val kycInitiateResponse get() = _kycInitiatedResponse

    private var _treezorStatusResponse = MutableLiveData<MessageResponse>()
    val treezorStatusResponse get() = _treezorStatusResponse

    private var _verifyPinResponse = MutableLiveData<MessageResponse>()
    val verifyPinResponse get() = _verifyPinResponse


    fun upload(file: File) {
        viewModelScope.launch(exceptionHandler) {
            val fileRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            val multiPart = MultipartBody.Part.createFormData(
                "file",
                file.name,
                fileRequestBody
            )
            val res = RestClient.get().upload(multiPart)
            if (res.isSuccessful)
                _uploadResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun initiatedKyc() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().initiateKyc()
            if (res.isSuccessful)
                _kycInitiatedResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun kycStatus() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().kycStatus()
            if (res.isSuccessful)
                _kycStatusResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun treezorStatus(status: Int = 1) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().treezorStatus(hashMapOf("status" to status))
            if (res.isSuccessful)
                _treezorStatusResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }


    fun verifyPin(pin: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().verifyPin(hashMapOf("login_pin" to pin.toInt()))
            if (res.isSuccessful)
                _verifyPinResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

}