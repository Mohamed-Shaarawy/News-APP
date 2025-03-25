package com.example.newsapi

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class NewsAdapter(
    var NewsImage: ArrayList<String?>,
    var NewsTitle: ArrayList<String>,
    var NewsDesc: ArrayList<String>,
    var NewsDate: ArrayList<String>,
    var NewsSource: ArrayList<String>,
    var context:Context
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>(){
    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var imageViewNewsImg: ImageView = itemView.findViewById<ImageView>(R.id.imageViewNewsImg)
        var textViewNewsTitle: TextView = itemView.findViewById<TextView>(R.id.textViewNewsTitle)
        var textViewNewsDesc: TextView = itemView.findViewById<TextView>(R.id.textViewDesc)
        var textViewNewsDate: TextView = itemView.findViewById<TextView>(R.id.textViewDate)
        var textViewNewsSource: TextView = itemView.findViewById<TextView>(R.id.textViewSource)
        var cardView: View = itemView.findViewById<View>(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_design, parent, false)
        return NewsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return NewsImage.size
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(NewsImage.get(position))
            .placeholder(R.drawable.imageloadfailed)
            .error(R.drawable.imageloadfailed)
            .into(holder.imageViewNewsImg)
        holder.textViewNewsTitle.text = NewsTitle.get(position)
        holder.textViewNewsDesc.text = NewsDesc.get(position)
        holder.textViewNewsDate.text = NewsDate.get(position)
        holder.textViewNewsSource.text = NewsSource.get(position)

        holder.cardView.setOnClickListener{
            Toast.makeText(context, "Item Clicked", Toast.LENGTH_SHORT).show()
        }

    }
}