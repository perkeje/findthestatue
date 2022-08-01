package com.example.findthestatue

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HistoryRecyclerAdapter:
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: List<Int> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder {
        return PersonViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recycle_view_item, parent,
                false)
        )
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder,
                                  position: Int) {
        when(holder) {
            is PersonViewHolder -> {
                holder.bind(items[position])
            }
        }
    }
    override fun getItemCount(): Int {
        return items.size
    }
    fun postItemsList(data: List<Int>) {
        items = data
    }
    class PersonViewHolder constructor(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        private val database = Firebase.database
        private val myRef = database.getReference("/")
        private val statueImg: ImageView =
            itemView.findViewById(R.id.statuePhoto)
        private val statueName: TextView =
            itemView.findViewById(R.id.statueName)
        fun bind(item: Int) {

            myRef.child(item.toString()).child("name").get().addOnSuccessListener {
                statueName.text = it.value.toString()
            }.addOnFailureListener{
                Log.e("firebase", "Error getting data", it)
            }
            myRef.child(item.toString()).child("img").get().addOnSuccessListener {
                Glide
                    .with(itemView.context)
                    .load(it.value.toString())
                    .into(statueImg)
            }.addOnFailureListener{
                Log.e("firebase", "Error getting data", it)
            }
        }
    }
}