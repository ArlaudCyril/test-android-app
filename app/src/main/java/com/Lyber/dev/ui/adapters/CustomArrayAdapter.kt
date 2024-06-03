//package com.Lyber.ui.adapters
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.graphics.Typeface
//import android.os.Build
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.animation.AnimationUtils
//import android.widget.ArrayAdapter
//import android.widget.TextView
//import androidx.annotation.RequiresApi
//import androidx.core.content.res.ResourcesCompat
//import com.Lyber.R
//import com.Lyber.databinding.AppItemLayoutBinding
//import com.Lyber.databinding.LoaderViewBinding
//import com.Lyber.models.MonthsList
//import com.Lyber.models.Network
//import com.Lyber.utils.CommonMethods.Companion.loadCircleCrop
//
//
//class CustomArrayAdapter(context: Context, resource: Int, objects: List<MonthsList>) :
//    ArrayAdapter<String>(context, resource, objects) {
//
//    private val customTypeface: Typeface? = ResourcesCompat.getFont(context, R.font.mabry_pro)
//
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    @SuppressLint("SetTextI18n")override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val view = inflater.inflate(android.R.layout.simple_list_item_1, null) // or use your custom layout
//
//        val textView = view.findViewById<TextView>(android.R.id.text1)
//        textView.text = getItem(position)
//  textView.typeface = customTypeface
//        return view
//    }
//}
//class NetworkPopupAdapter : android.widget.BaseAdapter() {
//
//    private val list = mutableListOf<MonthsList>()
//
//    fun getItemAt(position: Int): MonthsList {
//        return list[position]
//    }
//
//    fun hasNoData(): Boolean {
//        return list.isEmpty()
//    }
//
//    fun addProgress() {
//        list.add(null)
//        notifyDataSetChanged()
//    }
//
//    fun removeProgress() {
//        list.last()?.let {
//            list.remove(it)
//            notifyDataSetChanged()
//        }
//    }
//
//    fun setData(items: List<MonthsList>) {
//        list.clear()
//        list.addAll(items)
//        notifyDataSetChanged()
//    }
//
//    override fun getCount(): Int {
//        return list.count()
//    }
//
//    override fun getItem(position: Int): Any? {
//        return list[position]
//    }
//
//    override fun getItemId(position: Int): Long {
//        return 0
//    }
//
//
//    @SuppressLint("ViewHolder")
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val view = inflater.inflate(android.R.layout.simple_list_item_1, null) // or use your custom layout
//
//        val textView = view.findViewById<TextView>(android.R.id.text1)
//        textView.text = getItem(position)
//        textView.typeface = customTypeface
//        return view
//    }
//
//
//}
