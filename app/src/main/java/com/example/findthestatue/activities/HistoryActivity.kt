package com.example.findthestatue.activities


import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.findthestatue.R
import com.example.findthestatue.adapters.HistoryRecyclerAdapter
import com.example.findthestatue.utils.Prefs

class HistoryActivity : AppCompatActivity() {
    private lateinit var historyAdapter: HistoryRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        Prefs.setView(window)
        historyAdapter = HistoryRecyclerAdapter()
        val backBtn = findViewById<ImageButton>(R.id.back_btn)
        val list = Prefs.getArrayList(this)
        if (list == null) {
            historyAdapter.postItemsList(ArrayList())
        } else {
            @Suppress("UNCHECKED_CAST")
            historyAdapter.postItemsList(list as ArrayList<Int>)
        }

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

}