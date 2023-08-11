package com.au.lyber.ui.portfolio.action

import com.au.lyber.models.AssetBaseData
import com.au.lyber.models.Balance

interface AssestFragmentAction {
    fun assetClicked(balance: AssetBaseData)
}