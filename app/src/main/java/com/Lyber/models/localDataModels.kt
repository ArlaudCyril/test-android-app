package com.Lyber.models


data class PersonalDataLocal(
    var firstName: String,
    var lastName: String,
    var birthDate: String,
    val birthPlace: String,
    val birthCountry: String,
    val nationality: String,
    val specifiedUsPerson: String, val nationalityCode: String, val birthCountryCode: String,
    val birthDateLocal: String
)

data class AddressDataLocal(
    var streetNumber: String,
    var streetAddress: String,
    var city: String,
    val zipCode: String,
    val country: String,
    val specifiedUsPerson: String
)

data class InvestmentExperienceLocal(
    val investmentExperience: String, val sourceOfIncome: String, val workIndustry: String,
    val annualIncome: String, val activity: String
)

data class InvestmentExperienceLocalIds(
    val investmentExperience: Int, val sourceOfIncome: Int, val workIndustry: Int,
    val annualIncome: Int, val activity: Int
)

data class MonthsList(
    val monthEnglish:String,val monthFrench:String)
