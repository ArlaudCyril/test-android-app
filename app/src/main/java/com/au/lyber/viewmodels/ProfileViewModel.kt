package com.au.lyber.viewmodels

import com.au.lyber.models.Whitelistings
import com.au.lyber.models.WithdrawAddress


class ProfileViewModel : NetworkViewModel() {

    private var _createPin: String = ""
    var createPin: String
        get() = _createPin
        set(value) {
            _createPin = value
        }


    private var _selectedAddress: WithdrawAddress? = null
    var whitelistAddress
        get() = _selectedAddress
        set(value) {
            _selectedAddress = value
        }


}