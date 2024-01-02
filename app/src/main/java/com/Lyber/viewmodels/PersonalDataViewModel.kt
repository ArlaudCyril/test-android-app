package com.Lyber.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.Lyber.models.MessageResponse
import com.Lyber.models.User
import com.Lyber.network.RestClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PersonalDataViewModel : NetworkViewModel() {


    private var _isReview: Boolean = false
    var isReview
        get() = _isReview
        set(value) {
            _isReview = value
        }
    private var _personalData: User? = null
    var personalData
        get() = _personalData
        set(value) {
            _personalData = value
        }


    private var _hashMap: HashMap<String, Any> = hashMapOf()
    var hashMap
        get() = _hashMap
        private set(value) {
            _hashMap = value

        }


    private var _firstname: String = ""
    var firstName
        get() = _firstname
        set(value) {
            _firstname = value
            if (personalData != null) {
//                if (value != personalData!!.first_name)
                hashMap["first_name"] = value
            } else
                hashMap["first_name"] = value
        }

    private var _lastname: String = ""
    var lastName
        get() = _lastname
        set(value) {
            _lastname = value
            if (personalData != null) {
//                if (value != personalData!!.last_name)
                hashMap["last_name"] = value
            } else
                hashMap["last_name"] = value
        }

    private var _birthdate: String = ""
    var birthDate
        get() = _birthdate
        set(value) {
            _birthdate = value
            if (personalData != null) {
//                if (value != personalData!!.last_name)
                hashMap["dob"] = value
            } else
                hashMap["dob"] = value
        }

    private var _birthPlace: String = ""
    var birthPlace
        get() = _birthPlace
        set(value) {
            _birthPlace = value
            if (personalData != null) {
//                if (value != personalData!!.birth_place)
                hashMap["birth_place"] = value
            } else
                hashMap["birth_place"] = value
        }

    private var _birthCountry: String = ""
    var birthCountry
        get() = _birthCountry
        set(value) {
            _birthCountry = value
            if (personalData != null) {
//                if (value != personalData!!.birth_place)
                hashMap["birth_country"] = value
            } else
                hashMap["birth_country"] = value
        }

    private var _specifiedUsPerson: Int = 0
    var specifiedUsPerson
        get() = _specifiedUsPerson
        set(value) {
            _specifiedUsPerson = value
            hashMap["specifiedUSPerson"] = value
        }

    private var _nationality: String = ""
    var nationality
        get() = _nationality
        set(value) {
            _nationality = value
            if (personalData != null) {
//                if (value != personalData!!.birth_place)
                hashMap["nationality"] = value
            } else
                hashMap["nationality"] = value
        }

    private var _email: String = ""
    var email
        get() = _email
        set(value) {
            _email = value
            if (personalData != null) {
//                if (value != personalData!!.birth_place)
                hashMap["email"] = value
            } else
                hashMap["email"] = value
        }

    private var _password: String = ""
    var password: String
        get() = _password
        set(value) {
            _password = value
        }

    private var _streetNumber: String = ""
    var streetNumber
        get() = _streetNumber
        set(value) {
            _streetNumber = value

        }

    private var _buildingFloorName: String = ""
    var buildingFloorName
        get() = _buildingFloorName
        set(value) {
            _buildingFloorName = value
        }

    private var _city: String = ""
    var city
        get() = _city
        set(value) {
            _city = value
            if (personalData != null) {
//                if (value != personalData!!.city)
                hashMap["city"] = value
            } else
                hashMap["city"] = value
        }

    private var _state: String = ""
    var state
        get() = _state
        set(value) {
            _state = value
            if (personalData != null) {
//                if (value != personalData!!.state)
                hashMap["state"] = value
            } else
                hashMap["state"] = value
        }

    private var _zipCode: String = ""
    var zipCode
        get() = _zipCode
        set(value) {
            _zipCode = value
            if (personalData != null) {
//                if (value != personalData!!.zip_code.toString())
                hashMap["zip_code"] = value.toLong()
            } else
                hashMap["zip_code"] = value.toLong()
        }

    private var _country: String = ""
    var country
        get() = _country
        set(value) {
            _country = value
            if (personalData != null) {
//                if (value != personalData!!.country)
                hashMap["country"] = value
            } else
                hashMap["country"] = value
        }

    private var _completeAddress: String = ""
    var completeAddress
        get() = _completeAddress
        set(value) {
            _completeAddress = value
            if (personalData != null) {
//                if (value != personalData!!.address1)
                hashMap["address1"] = value
            } else
                hashMap["address1"] = value
        }


    private var _cryptoExp: String = ""
    var cryptoExp
        get() = _cryptoExp
        set(value) {
            _cryptoExp = value
        }

    private var _sourceOfIncome: String = ""
    var sourceOfIncome
        get() = _sourceOfIncome
        set(value) {
            _sourceOfIncome = value
        }

    private var _workIndustry: String = ""
    var workIndustry
        get() = _workIndustry
        set(value) {
            _workIndustry = value
            if (personalData != null) {
//                if (value != personalData!!.occupation)
                hashMap["occupation"] = value
            } else
                hashMap["occupation"] = value
        }

    private var _annualIncome: String = ""
    var annualIncome
        get() = _annualIncome
        set(value) {
            _annualIncome = value
            if (personalData != null) {
//                if (value != personalData!!.incomeRange)
                hashMap["incomeRange"] = value
            } else
                hashMap["incomeRange"] = value
        }

    private var _personalAssets: String = ""
    var personalAssets
        get() = _personalAssets
        set(value) {
            _personalAssets = value
            if (personalData != null) {
//                if (value != personalData!!.personalAssets)
                hashMap["mainUse"] = value
            } else
                hashMap["mainUse"] = value
        }

    /* api */

    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        /* handle exceptions here */
    }

    private var _sendEmailResponse = MutableLiveData<MessageResponse>()
    val sendEmailResponse get() = _sendEmailResponse

    private var _resendEmailResponse = MutableLiveData<MessageResponse>()
    val resendEmailResponse get() = _resendEmailResponse

    private var _emailVerificationResponse = MutableLiveData<MessageResponse>()
    val emailVerificationResponse get() = _emailVerificationResponse

    private var _fillPersonalDataResponse = MutableLiveData<MessageResponse>()
    val fillPersonalDataResponse get() = _fillPersonalDataResponse

    private var _personalDataResponse = MutableLiveData<User>()
    val personDataResponse get() = _personalDataResponse

    private var _updatePersonalData = MutableLiveData<MessageResponse>()
    val updatePersonalData get() = _updatePersonalData

    private var _addPersonalInfoResponse = MutableLiveData<MessageResponse>()
    val addPersonalInfoResponse get() = _addPersonalInfoResponse

    private var _treezorCreateUser = MutableLiveData<User>()
    val treezorCreateUser get() = _treezorCreateUser


    fun addPersonalInfo(step: Int) {
        viewModelScope.launch(exceptionHandler) {
            hashMap["personal_info_step"] = step
            val res = RestClient.get().personalInfo(hashMap)
            if (res.isSuccessful)
                _addPersonalInfoResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun sendEmail(isResend: Boolean = false) {
        try {
            CoroutineScope(Dispatchers.Main).launch(exceptionHandler) {
                val res = RestClient.get().sendEmail(hashMapOf("email" to email))
                if (res.isSuccessful) {
                    if (isResend)
                        _resendEmailResponse.postValue(res.body())
                    else
                        _sendEmailResponse.postValue(res.body())
                } else listener?.onRetrofitError(res.errorBody())
            }
        } catch (e: Exception) {
            listener?.onError()
        }
    }

    fun emailVerification() {
        try {
            CoroutineScope(Dispatchers.Main).launch(exceptionHandler) {
                val res = RestClient.get().checkEmailVerification()
                if (res.isSuccessful) _emailVerificationResponse.postValue(res.body())
                else listener?.onRetrofitError(res.errorBody())
            }
        } catch (e: Exception) {
            listener?.onError()
        }
    }

    fun fillPersonalData() {
        try {
            CoroutineScope(Dispatchers.Main).launch(exceptionHandler) {
                val res = RestClient.get().fillPersonalData(hashMap)
                if (res.isSuccessful) _fillPersonalDataResponse.postValue(res.body())
                else listener?.onRetrofitError(res.errorBody())
            }
        } catch (e: Exception) {
            listener?.onError()
        }
    }

    fun getPersonalData() {
        try {
            viewModelScope.launch(exceptionHandler) {
                val res = RestClient.get().getPersonalInfo()
                if (res.isSuccessful) _personalDataResponse.postValue(res.body())
                else listener?.onRetrofitError(res.errorBody())
            }
        } catch (e: Exception) {
            listener?.onError()
        }
    }

    fun updatePersonalInfo() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().updatePersonalInfo(hashMap)
            if (res.isSuccessful)
                _updatePersonalData.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun treezorCreateUser() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().createUser()
            if (res.isSuccessful)
                _treezorCreateUser.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }
}