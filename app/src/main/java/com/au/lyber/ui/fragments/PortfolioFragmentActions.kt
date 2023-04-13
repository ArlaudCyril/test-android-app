package com.au.lyber.ui.fragments

import com.au.lyber.models.*

interface PortfolioFragmentActions {

    fun recurringInvestmentClicked(investment: Investment)

    fun investMoneyClicked(toStrategy: Boolean)

    fun assetClicked(asset: Assets)

    fun availableAssetClicked(asset: priceServiceResume)

    fun menuOptionSelected(tag:String,option:String)

    fun clickedAnalytics(analyticsData: AnalyticsData)

}