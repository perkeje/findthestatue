package com.example.findthestatue


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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
    }

    class StatueViewHolder constructor(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        private val statueImg: ImageView =
            itemView.findViewById(R.id.statuePhoto)
        private val statueName: TextView =
            itemView.findViewById(R.id.statueName)
        private val statueDescription: TextView =
            itemView.findViewById(R.id.statue_description)
        private val descriptionLayout: ConstraintLayout =
            itemView.findViewById(R.id.description_layout)
        private lateinit var adapter :HistoryRecyclerAdapter;

        fun bind(item: Int,myRef:DatabaseReference) {

            myRef.child(item.toString()).child("name").get().addOnSuccessListener {
                statueName.text = it.value.toString()
            }.addOnFailureListener{
                Log.e("firebase", "Error getting data", it)
            }
            myRef.child(item.toString()).child("description").get().addOnSuccessListener {
                statueDescription.text = it.value.toString()
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
            this.itemView.setOnClickListener{
                if (descriptionLayout.visibility == View.GONE) {
                    expand(descriptionLayout)
                }
                else{
                    collapse(descriptionLayout)
                }
            }
        }

        fun linkAdapter(adapter: HistoryRecyclerAdapter):StatueViewHolder{
            this.adapter=adapter
            return this
        }
    }
}


fun expand(view: View) {
    val animation = expandAction(view)
    view.startAnimation(animation)
}

private fun expandAction(view: View): Animation {
    view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    val actualheight = view.measuredHeight
    view.layoutParams.height = 0
    view.visibility = View.VISIBLE
    val animation: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            view.layoutParams.height =
                if (interpolatedTime == 1f) ViewGroup.LayoutParams.WRAP_CONTENT
                else (actualheight * interpolatedTime).toInt()
            view.requestLayout()
        }
    }
    animation.duration = ((actualheight / view.context.resources.displayMetrics.density)).toLong()
    view.startAnimation(animation)
    return animation
}

fun collapse(view: View) {
    val actualHeight = view.measuredHeight
    val animation: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            if (interpolatedTime == 1f) {
                view.visibility = View.GONE
            } else {
                view.layoutParams.height = actualHeight - (actualHeight * interpolatedTime).toInt()
                view.requestLayout()
            }
        }
    }
    animation.duration = ((actualHeight / view.context.resources.displayMetrics.density)/2.5 ).toLong()
    view.startAnimation(animation)
}