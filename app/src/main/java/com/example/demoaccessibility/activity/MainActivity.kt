package com.example.demoaccessibility.activity

import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.provider.Settings
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.demoaccessibility.R
import com.example.demoaccessibility.util.ContentListPlaceHolder
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import com.orhanobut.hawk.LogInterceptor
import com.orhanobut.hawk.NoEncryption
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {
    private val button: Button by lazy { findViewById<Button>(R.id.button) }
    private val button2: Button by lazy { findViewById<Button>(R.id.button2) }
    private val button3: Button by lazy { findViewById<Button>(R.id.button3) }
    private var mList: MutableList<PackageInfo> = ArrayList()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Hawk.init(this).build()

        button.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        }
        button2.setOnClickListener {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        }
        button3.setText(getString(R.string.clean_cache))

        button3.setOnClickListener {
            buttonIntentClean()
        }


    }

    private fun getListInstalledApps(systemOnly: Boolean, userOnly: Boolean): ArrayList<PackageInfo> {
        val list = packageManager.getInstalledPackages(0)
        val pkgInfoList = ArrayList<PackageInfo>()
        for (i in list.indices) {
            val packageInfo = list[i]
            val flags = packageInfo!!.applicationInfo.flags
            val isSystemApp = (flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystemApp = (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            val addPkg = (systemOnly && (isSystemApp and !isUpdatedSystemApp)) or
                    (userOnly && (!isSystemApp or isUpdatedSystemApp))
            if (addPkg)
                pkgInfoList.add(packageInfo)
        }
        return pkgInfoList
    }

    private fun getListInstalledUserApps(): ArrayList<PackageInfo> {
        return getListInstalledApps(systemOnly = true, userOnly = true)
    }

    private fun getStorageStats(packageName: String): StorageStats? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null

        try {
            val storageStatsManager =
                getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
            return storageStatsManager.queryStatsForPackage(
                StorageManager.UUID_DEFAULT, packageName,
                android.os.Process.myUserHandle()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun buttonIntentClean() {
        mList.clear()
        ContentListPlaceHolder.clearMyList()
        mList = getListInstalledUserApps()
        mList.forEach {
            getStorageStats(it.applicationInfo.packageName)?.let { it1 ->
                ContentListPlaceHolder.addItem(
                    pkgInfo = it,
                    label = it.applicationInfo.packageName,
                    icon = it.applicationInfo.loadIcon(packageManager),
                    checked = false,
                    stats = it1
                )
            }

        }
        if (mList.size > 0) {
            ContentListPlaceHolder.sortBySize()
            startActivity(Intent(this, ListActivity::class.java))
        }
    }
}