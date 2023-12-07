package com.au.lyber.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class WithdrawalAddress {
    @SerializedName("data")
    @Expose
    var data: List<WithdrawAddress>? = null
}