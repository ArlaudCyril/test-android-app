package com.au.lyber.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class WithdrawAddress {
    @SerializedName("network")
    @Expose
    var network: String? = null

    @SerializedName("address")
    @Expose
    var address: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("origin")
    @Expose
    var origin: String? = null

    @SerializedName("creationDate")
    @Expose
    var creationDate: String? = null

    @SerializedName("exchange")
    @Expose
    var exchange: String? = null
}