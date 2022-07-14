package com.example.demoaccessibility.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ContentValues.TAG
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class MyAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        Log.e("Access", "onAccessibilityEvent")
        val packageName = p0?.packageName.toString()
        val packManager = this.packageManager
        try {
            val appInfo: ApplicationInfo = packManager.getApplicationInfo(packageName, 0)
            val appLabel = packManager.getApplicationLabel(appInfo)
            Log.e(TAG, "app name is $appLabel")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onInterrupt() {
        Log.e("Access", "onInterrupt")
    }

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo()
        info.apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or AccessibilityEvent.TYPE_VIEW_FOCUSED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
            // flags = AccessibilityServiceInfo.DEFAULT;
            notificationTimeout = 100
        }
        this.serviceInfo = info
        Log.e("Access", "onServiceConnected")

    }
}