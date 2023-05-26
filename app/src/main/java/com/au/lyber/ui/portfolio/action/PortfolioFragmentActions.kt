package com.au.lyber.ui.portfolio.action

import com.au.lyber.models.*

interface PortfolioFragmentActions {

    fun recurringInvestmentClicked(investment: Investment)

    fun investMoneyClicked(toStrategy: Boolean)

    fun assetClicked(balance: Balance)

    fun availableAssetClicked(asset: PriceServiceResume)

    fun menuOptionSelected(tag:String,option:String)


}