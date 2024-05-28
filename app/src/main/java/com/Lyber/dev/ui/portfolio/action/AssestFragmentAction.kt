package com.Lyber.dev.ui.portfolio.action

import com.Lyber.dev.models.AssetBaseData
import com.Lyber.dev.models.Balance

interface AssestFragmentAction {
    fun assetClicked(balance: AssetBaseData)
}