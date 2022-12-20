package com.au.lyber.utils

import android.text.Editable
import android.text.TextWatcher

interface OnTextChange : TextWatcher {

    fun onTextChange()
    override fun afterTextChanged(s: Editable?) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        onTextChange()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
}