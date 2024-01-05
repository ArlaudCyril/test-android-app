package com.Lyber.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.Lyber.R


class CustomArrayAdapter(context: Context, resource: Int, objects: List<String>) :
    ArrayAdapter<String>(context, resource, objects) {

    private val customTypeface: Typeface? = ResourcesCompat.getFont(context, R.font.mabry_pro)


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(android.R.layout.simple_list_item_1, null) // or use your custom layout

        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = getItem(position)
  textView.typeface = customTypeface
        return view
    }
}