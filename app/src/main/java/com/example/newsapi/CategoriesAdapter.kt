package com.example.newsapi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView


class CategoriesAdapter(
    var CategoryName:ArrayList<String>
): RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {
    class CategoryViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var textViewCategoryTitle: TextView = itemView.findViewById(R.id.textViewCountryName)
        var cardView: CardView = itemView.findViewById(R.id.countryCardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.country_card_view, parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return CategoryName.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.textViewCategoryTitle.text = CategoryName.get(position)
    }
}