package com.au.lyber.ui.fragments

import com.au.lyber.models.AnalyticsData
import com.au.lyber.models.Assets
import com.au.lyber.models.Data
import com.au.lyber.models.Investment

interface PortfolioFragmentActions {

    fun recurringInvestmentClicked(investment: Investment)

    fun investMoneyClicked(toStrategy: Boolean)

    fun assetClicked(asset: Assets)

    fun availableAssetClicked(asset: Data)

    fun menuOptionSelected(tag:String,option:String)

    fun clickedAnalytics(analyticsData: AnalyticsData)

}