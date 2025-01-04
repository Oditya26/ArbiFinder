package com.example.arbitrade

import TradeViewModel
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arbitrade.databinding.FragmentAiBinding

class AiFragment : Fragment() {

    private lateinit var binding: FragmentAiBinding
    private lateinit var tradeViewModel: TradeViewModel
    private lateinit var tradeAdapter: TradeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAiBinding.inflate(inflater, container, false)
        tradeViewModel = ViewModelProvider(requireActivity()).get(TradeViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView
        tradeAdapter = TradeAdapter(
            tradeList = mutableListOf(), // Empty list initially
            onDelete = { trade ->
                tradeViewModel.deleteTrade(trade)
            },
            onEdit = { trade ->
                // Get position from the view model's tradeList
                val tradeList = tradeViewModel.tradeList.value ?: emptyList()

                // Find the position of the trade item
                val position = tradeList.indexOf(trade)

                val bundle = Bundle().apply {
                    putString("date", trade.date)
                    putString("symbol", trade.symbol)
                    putFloat("buyPrice", trade.buyPrice ?: 0f)
                    putFloat("sellPrice", trade.sellPrice ?: 0f)
                    putFloat("amount", trade.amount ?: 0f)
                    putFloat("pnl", trade.pnl ?: 0f)
                    putString("comment", trade.comment)
                    putInt("editPosition", position) // Passing the index as edit position
                }

                val inputDataFragment = InputDataFragment().apply {
                    arguments = bundle
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, inputDataFragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

        binding.rvData.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tradeAdapter
        }

        // Observe the tradeList LiveData for changes
        tradeViewModel.tradeList.observe(viewLifecycleOwner) { updatedList ->
            tradeAdapter.tradeList = updatedList
            tradeAdapter.notifyDataSetChanged()
        }

        // Add new trade on button click
        binding.btnAdd.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, InputDataFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}