package com.example.findthestatue


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HistoryRecyclerAdapter:
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: ArrayList<Int> = ArrayList()
    private val database = Firebase.database
    private val myRef = database.getReference("/")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder {
         val view = StatueViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recycle_view_item, parent,false))
        return view.linkAdapter(this)

    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder,
                                  position: Int) {
        when(holder) {
            is StatueViewHolder -> {
                holder.bind(items[position],myRef)
            }
        }
    }
    override fun getItemCount(): Int {
        return items.size
    }
    fun postItemsList(data: ArrayList<Int>) {
        items = data
    }

    fun removeAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position,items.size)
    }

    class StatueViewHolder constructor(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        private val statueImg: ImageView =
            itemView.findViewById(R.id.statuePhoto)
        private val statueName: TextView =
            itemView.findViewById(R.id.statueName)
        private lateinit var adapter :HistoryRecyclerAdapter;
        fun bind(item: Int,myRef:DatabaseReference) {

            myRef.child(item.toString()).child("name").get().addOnSuccessListener {
                statueName.text = it.value.toString()
            }.addOnFailureListener{
                Log.e("firebase", "Error getting data", it)
            }
            myRef.child(item.toString()).child("img").get().addOnSuccessListener {
                Glide
                    .with(itemView.context)
                    .load(it.value.toString())
                    .centerCrop()
                    .into(statueImg)
            }.addOnFailureListener{
                Log.e("firebase", "Error getting data", it)
            }

            itemView.findViewById<ImageButton>(R.id.rm_btn).setOnClickListener {
                val prefs = Prefs()
                adapter.removeAt(adapterPosition)
                var list = prefs.getArrayList(itemView.context)
                list?.remove(item)
                prefs.saveArrayList(list,itemView.context)


            }
        }
        fun linkAdapter(adapter: HistoryRecyclerAdapter):StatueViewHolder{
            this.adapter=adapter
            return this
        }
    }
}