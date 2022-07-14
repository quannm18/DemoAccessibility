package com.example.demoaccessibility.adapter

import android.os.Build
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demoaccessibility.R
import com.example.demoaccessibility.viewmodel.PlaceholderPackage

class MyAdapter(val mList: MutableList<PlaceholderPackage>) : RecyclerView.Adapter<MyAdapter.MyViewModel>() {

    inner class MyViewModel(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val packageIcon: ImageView by lazy { itemView.findViewById<ImageView>(R.id.packageIcon) }
        private val packageLabel: CheckBox by lazy { itemView.findViewById<CheckBox>(R.id.packageLabel) }
        private val packageName: TextView by lazy { itemView.findViewById<TextView>(R.id.packageName) }
        private val cacheSize: TextView by lazy { itemView.findViewById<TextView>(R.id.cacheSize) }

        fun bind(pkgInfo: PlaceholderPackage) {
            packageIcon.setImageDrawable(pkgInfo.icon)
            packageLabel.setText(pkgInfo.label)
            packageLabel.isChecked = pkgInfo.checked
            packageLabel.setOnCheckedChangeListener(null)
            packageName.setText(pkgInfo.name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pkgInfo.stats != null) {
                cacheSize.setText(Formatter.formatShortFileSize(itemView.context, pkgInfo.stats.cacheBytes))
            }
            packageLabel.setOnCheckedChangeListener { _, checked ->
                pkgInfo.checked = checked
            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewModel {
        return MyViewModel(LayoutInflater.from(parent.context).inflate(R.layout.fragment_package, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewModel, position: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.bind(mList[position])
        }
    }
}