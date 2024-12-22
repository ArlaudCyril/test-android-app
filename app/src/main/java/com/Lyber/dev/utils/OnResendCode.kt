package com.Lyber.utils


var resendCodeParam: OnResendCode? = null

interface OnResendCode {
    fun onResend()
}
fun setOnResendCodeClickListener(listener: OnResendCode) {
    resendCodeParam = listener
}