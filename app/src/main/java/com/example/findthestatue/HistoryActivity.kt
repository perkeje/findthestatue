package com.example.findthestatue

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HistoryActivity : AppCompatActivity() {
    private lateinit var historyAdapter: HistoryRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        historyAdapter = HistoryRecyclerAdapter()
        val backBtn = findViewById<ImageButton>(R.id.back_btn)
        val list = getArrayList()
        if (list==null){
            historyAdapter.postItemsList(ArrayList())
        }
        else historyAdapter.postItemsList(list as ArrayList<Int>)

        initView()

        backBtn.setOnClickListener {
            onBackPressed()
        }
    }
    private fun initView() {
        findViewById<RecyclerView>(R.id.items_list).apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }
    }

    private fun getArrayList():ArrayList<Int?>?{
        val prefs = this.getSharedPreferences("saved", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString("saved",null)
        val type= object : TypeToken<ArrayList<Int?>?>() {}.type
        return gson.fromJson(json,type)
    }
}