package com.Lyber.ui.portfolio.action

import com.Lyber.models.Balance
import com.Lyber.models.Investment
import com.Lyber.models.PriceServiceResume
import com.Lyber.models.*

interface PortfolioFragmentActions {

    fun recurringInvestmentClicked(investment: ActiveStrategyData)

    fun investMoneyClicked(toStrategy: Boolean)

    fun assetClicked(balance: Balance)

    fun availableAssetClicked(priceResume: PriceServiceResume)

    fun menuOptionSelected(tag:String,option:String)


}