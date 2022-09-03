package com.avilin.dallemini.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.avilin.dallemini.R
import com.bumptech.glide.Glide

class ImagesRecyclerAdapter(private val images: List<String>, private val listener: ImagesListener)
    : RecyclerView.Adapter<ImagesRecyclerAdapter.ImagesViewHolder>() {

    public interface ImagesListener{
        fun onClick(position: Int, context: Context)
    }

    class ImagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val image: ImageView = itemView.findViewById(R.id.ivImage)
        public fun bind(imageUrl: String, listener: ImagesListener){
            Glide.with(itemView.context)
                .load(imageUrl)
                .error(R.drawable.ic_baseline_do_not_disturb_24)
                .into(image)
            image.setOnClickListener { listener.onClick(position, itemView.context) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_img, parent, false)
        return ImagesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        holder.bind(images[position], listener)
    }

    override fun getItemCount() = images.size
}