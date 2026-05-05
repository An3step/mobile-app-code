package com.example.project_skebob

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListFragment : Fragment(R.layout.fragment_list) {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: FactsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val btnFetch = view.findViewById<Button>(R.id.btnFetchApi)
        val btnFilter = view.findViewById<Button>(R.id.btnFilter)
        val tvStats = view.findViewById<TextView>(R.id.tvStats)

        adapter = FactsAdapter(mutableListOf()) { fact ->
            viewModel.toggleFavorite(fact)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.allFacts.observe(viewLifecycleOwner) { facts ->
            adapter.updateData(facts)
        }

        viewModel.errorLiveData.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        viewModel.allStatsLiveData.observe(viewLifecycleOwner) { stats ->
            tvStats.text = stats
        }

        btnFetch.setOnClickListener {
            viewModel.fetchAndSaveNewFact()
        }

        btnFilter.setOnClickListener {
            val newFilter = !viewModel.filterAll
            viewModel.loadAll(newFilter)
            btnFilter.text = if (newFilter) "Показать все" else "Только за 24ч"
        }


        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val fact = adapter.getFactAt(position)

                if (!fact.isFavorite) {
                    viewModel.deleteFact(fact.id)
                    Toast.makeText(requireContext(), "Факт удален", Toast.LENGTH_SHORT).show()
                } else {

                    adapter.notifyItemChanged(position)
                    Toast.makeText(requireContext(), "Нельзя удалить избранное свайпом", Toast.LENGTH_SHORT).show()
                }
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
        viewModel.loadAll()
    }
}