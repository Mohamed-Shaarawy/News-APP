package com.example.newsapi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView


class CountriesAdapter (
    var countryTitle: ArrayList<String>
) : RecyclerView.Adapter<CountriesAdapter.CountriesViewHolder>(){
  class CountriesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
      var textViewCountryTitle: TextView = itemView.findViewById(R.id.textViewCountryName)
      var cardView: CardView = itemView.findViewById(R.id.countryCardView)

  }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountriesViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.country_card_view, parent, false)
        return CountriesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return countryTitle.size
    }

    override fun onBindViewHolder(holder: CountriesViewHolder, position: Int) {
        holder.textViewCountryTitle.text = countryTitle.get(position)
        }
}