package com.Lyber.dev.ui.portfolio.action

import com.Lyber.dev.models.Balance
import com.Lyber.dev.models.Investment
import com.Lyber.dev.models.PriceServiceResume
import com.Lyber.dev.models.*

interface PortfolioFragmentActions {

    fun recurringInvestmentClicked(investment: ActiveStrategyData)

    fun investMoneyClicked(toStrategy: Boolean)

    fun assetClicked(balance: Balance)

    fun availableAssetClicked(priceResume: PriceServiceResume)

    fun menuOptionSelected(tag:String,option:String)


}