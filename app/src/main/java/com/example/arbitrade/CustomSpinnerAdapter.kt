package com.example.arbitrade

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi

class CustomSpinnerAdapter(
    context: Context,
    @LayoutRes private val resource: Int,
    private val items: List<String>
) : ArrayAdapter<String>(context, resource, items) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = createItemView(position, convertView, parent)
        return view
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = createItemView(position, convertView, parent)
        view.setBackgroundColor(context.resources.getColor(R.color.dark_jungle_green, null)) // Latar belakang gelap
        return view
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(resource, parent, false)
        val textView: TextView = view.findViewById(android.R.id.text1)
        textView.text = items[position]
        textView.setTextColor(context.resources.getColor(R.color.mercury, null)) // Warna teks Mercury
        textView.textSize = 16f // Ukuran teks

        return view
    }
}


