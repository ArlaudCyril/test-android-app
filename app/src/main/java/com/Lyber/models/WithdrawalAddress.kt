package com.Lyber.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class WithdrawalAddress {
    @SerializedName("data")
    @Expose
    var data: List<WithdrawAddress>? = null
}