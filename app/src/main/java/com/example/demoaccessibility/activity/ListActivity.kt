package com.example.demoaccessibility.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ConditionVariable
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoaccessibility.R
import com.example.demoaccessibility.adapter.MyAdapter
import com.example.demoaccessibility.util.ContentListPlaceHolder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.HashSet

class ListActivity : AppCompatActivity() {
    private val rcvMain: RecyclerView by lazy { findViewById<RecyclerView>(R.id.rcvMain) }
    private val floatingActionButton: FloatingActionButton by lazy { findViewById<FloatingActionButton>(R.id.floatingActionButton) }
    private val fabClear: FloatingActionButton by lazy { findViewById<FloatingActionButton>(R.id.fabClear) }

    private val checkedPkgList: HashSet<String> = HashSet()
    private lateinit var myAdapter: MyAdapter
    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        myAdapter = MyAdapter(ContentListPlaceHolder.itemList)
        rcvMain.apply {
            layoutManager = LinearLayoutManager(this@ListActivity)
            adapter = myAdapter
            addItemDecoration(DividerItemDecoration(this@ListActivity,DividerItemDecoration.VERTICAL))
        }
        floatingActionButton.tag= "uncheck"
        floatingActionButton.setOnClickListener {
            if (ContentListPlaceHolder.itemList.all { it.checked }){
                floatingActionButton.tag = "uncheck"
            }else if (ContentListPlaceHolder.itemList.none{it.checked}){
                floatingActionButton.tag= "check"
            }

            if (floatingActionButton.tag.equals("uncheck")){
                floatingActionButton.tag = "check"
                floatingActionButton.contentDescription = getString(R.string.check_app)
                ContentListPlaceHolder.itemList.forEachIndexed{
                    index, it->
                    it.checked = false
                    myAdapter.notifyItemChanged(index)
                }
            }else{
                floatingActionButton.tag = "check"
                floatingActionButton.contentDescription = getString(R.string.check_app)
                ContentListPlaceHolder.itemList.forEachIndexed{
                        index, it->
                    it.checked = true
                    myAdapter.notifyItemChanged(index)
                }
            }
            ContentListPlaceHolder.itemList.forEach{
                if (it.checked){
                    checkedPkgList.add(it.name)
                }else{
                    checkedPkgList.remove(it.name)
                }
            }
            ContentListPlaceHolder.sortBySize()
        }

        fabClear.setOnClickListener {
            ContentListPlaceHolder.itemList.filter {
                it.checked
            }.forEach {
                checkedPkgList.add(it.name)
            }

            ContentListPlaceHolder.itemList.filter {
                !it.checked
            }.forEach {
                checkedPkgList.remove(it.name)
            }

            Hawk.put("listChecked",checkedPkgList)

            CoroutineScope(IO).launch {
                startCleanCache(ContentListPlaceHolder.itemList.filter { it.checked }.map { it.name })
            }
        }
    }

    //2
    private suspend fun startCleanCache(pkgList: List<String>) {
        cleanCacheInterrupt.set(false)
        cleanCacheFinished.set(false)

        for (i in pkgList.indices) {
            startApplicationDetailsActivity(pkgList[i])
            cleanAppCacheFinished.set(false)
//            runOnUiThread {
//                binding.textView.text = String.format(
//                    Locale.getDefault(),
//                    "%d / %d %s", i, pkgList.size,
//                    getText(R.string.text_clean_cache_left))
//            }
            delay(500L)
            waitAccessibility.block(5000L)
            delay(500L)

            // user interrupt process
            if (cleanCacheInterrupt.get()) break
        }
        cleanCacheFinished.set(true)

        runOnUiThread {
//            val displayText = if (cleanCacheInterrupt.get())
//                getText(R.string.text_clean_cache_interrupt)
//            else getText(R.string.text_clean_cache_finish)
//            binding.textView.text = displayText
//
//            binding.btnCleanUserAppCache.isEnabled = true
//            binding.btnCleanSystemAppCache.isEnabled = true
//            binding.btnCleanAllAppCache.isEnabled = true

            // return back to Main Activity, sometimes not possible press Back from Settings
            if (pkgList.isNotEmpty() && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                val intent = this.intent
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(ARG_DISPLAY_TEXT, "displayText")
                startActivity(intent)
            }
        }
    }

    //1
    private fun startApplicationDetailsActivity(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    companion object {

        const val ARG_DISPLAY_TEXT = "display-text"

        const val FRAGMENT_PACKAGE_LIST_TAG = "package-list"

        const val SETTINGS_CHECKED_PACKAGE_LIST_TAG = "package-list"
        const val SETTINGS_CHECKED_PACKAGE_TAG = "checked"

        val loadingPkgList = AtomicBoolean(false)
        val cleanAppCacheFinished = AtomicBoolean(false)
        val cleanCacheFinished = AtomicBoolean(true)
        val cleanCacheInterrupt = AtomicBoolean(false)
        val waitAccessibility = ConditionVariable()
        private val TAG = MyAdapter::class.java.simpleName
    }
}