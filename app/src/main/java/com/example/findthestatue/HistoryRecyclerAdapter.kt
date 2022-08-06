package com.example.findthestatue


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


class HistoryRecyclerAdapter:
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: ArrayList<Int> = ArrayList()
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
                holder.bind(items[position])
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

        fun bind(item: Int) {

            Statue.fromIndex(item,object :FirebaseCallback {
                override fun onResponse(statue: Statue?) {
                    statue?.let {
                        statueName.text = statue.name
                        statueDescription.text = statue.description
                        Glide
                            .with(itemView.context)
                            .load(statue.img)
                            .centerCrop()
                            .into(statueImg)
                    }

                }
            })

            itemView.findViewById<ImageButton>(R.id.rm_btn).setOnClickListener {
                adapter.removeAt(adapterPosition)
                var list = Prefs.getArrayList(itemView.context)
                list?.remove(item)
                Prefs.saveArrayList(list,itemView.context)
            }
            this.itemView.setOnClickListener{
                if (descriptionLayout.visibility == View.GONE) {
                    Animations.expand(descriptionLayout)
                }
                else{
                    Animations.collapse(descriptionLayout)
                }
            }
        }

        fun linkAdapter(adapter: HistoryRecyclerAdapter):StatueViewHolder{
            this.adapter=adapter
            return this
        }
    }
}


