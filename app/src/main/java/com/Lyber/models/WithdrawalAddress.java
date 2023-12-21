package com.Lyber.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WithdrawalAddress {

@SerializedName("data")
@Expose
public List<WithdrawAddress> data;

}