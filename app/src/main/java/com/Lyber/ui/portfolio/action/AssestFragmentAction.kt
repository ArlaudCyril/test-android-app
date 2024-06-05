package com.Lyber.ui.portfolio.action

import com.Lyber.models.AssetBaseData
import com.Lyber.models.Balance

interface AssestFragmentAction {
    fun assetClicked(balance: AssetBaseData)
}