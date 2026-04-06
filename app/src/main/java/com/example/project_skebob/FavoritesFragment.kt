package com.example.project_skebob

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritesFragment : Fragment(R.layout.fragment_list) {
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: FactsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        view.findViewById<Button>(R.id.btnFetchApi).visibility = View.GONE
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val btnFilter = view.findViewById<Button>(R.id.btnFilter)
        val tvStats = view.findViewById<TextView>(R.id.tvStats)

        adapter = FactsAdapter(mutableListOf()) { fact -> viewModel.toggleFavorite(fact) }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.favoriteFacts.observe(viewLifecycleOwner) { adapter.updateData(it) }
        viewModel.favStatsLiveData.observe(viewLifecycleOwner) { stats ->
            tvStats.text = "Избранное: $stats"
        }
        btnFilter.setOnClickListener {
            viewModel.loadFavorites(!viewModel.filterFavs)
        }

        viewModel.loadFavorites()
    }
}