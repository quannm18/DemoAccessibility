package com.example.demoaccessibility.util

import android.app.usage.StorageStats
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.demoaccessibility.viewmodel.PlaceholderPackage

object ContentListPlaceHolder {
    val itemList: MutableList<PlaceholderPackage> = ArrayList()

    fun addItem(
        pkgInfo: PackageInfo,
        label: String,
        icon: Drawable,
        checked: Boolean,
        stats: StorageStats
    ) {
        itemList.add(
            PlaceholderPackage(
                pkgInfo = pkgInfo,
                name = pkgInfo.versionName,
                label = label,
                icon = icon,
                checked = checked,
                stats = stats
            )
        )
    }
    fun clearMyList(){
        this.itemList.clear()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun sortBySize() {
        itemList.sortWith(compareBy<PlaceholderPackage>{
            !it.checked
        }.thenByDescending {
            it.stats?.cacheBytes ?: 0
        }.thenBy {
            it.label
        })
    }
}