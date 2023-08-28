package com.au.lyber.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WithdrawAddress {

@SerializedName("network")
@Expose
public String network;
@SerializedName("address")
@Expose
public String address;
@SerializedName("name")
@Expose
public String name;
@SerializedName("origin")
@Expose
public String origin;
@SerializedName("creationDate")
@Expose
public String creationDate;

}