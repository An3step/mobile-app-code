package com.example.project_skebob

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FactsAdapter(
    private val facts: MutableList<FactRecord>,
    private val onFavClick: (FactRecord) -> Unit
) : RecyclerView.Adapter<FactsAdapter.FactViewHolder>() {

    override fun onBindViewHolder(holder: FactViewHolder, position: Int) {
        val fact = facts[position]
        holder.textView.text = (if (fact.isFavorite) "⭐ " else "") + fact.factText
        holder.itemView.setOnClickListener { onFavClick(fact) }
    }
    class FactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return FactViewHolder(view)
    }

    override fun getItemCount() = facts.size

    fun getFactAt(position: Int) = facts[position]

    fun updateData(newFacts: List<FactRecord>) {
        facts.clear()
        facts.addAll(newFacts)
        notifyDataSetChanged()
    }
}