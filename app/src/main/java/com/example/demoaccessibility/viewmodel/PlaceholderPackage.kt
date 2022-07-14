package com.example.demoaccessibility.viewmodel

import android.app.usage.StorageStats
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable

data class PlaceholderPackage(
    val pkgInfo: PackageInfo,
    val name: String,
    val label: String,
    val icon: Drawable,
    var checked: Boolean,
    val stats: StorageStats
)
