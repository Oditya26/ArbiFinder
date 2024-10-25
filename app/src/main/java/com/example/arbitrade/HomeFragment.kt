package com.example.arbitrade

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arbitrade.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var dataAdapter: DataAdapter
    private lateinit var dataList: MutableList<DataModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply window insets for full-screen mode
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Remove padding for full-screen
            v.setPadding(0, 0, 0, 0)
            insets
        }

        // Sample Data
        dataList = mutableListOf(
            DataModel("%0.02", "ETH/BTC", "Bitfinex", "HitBTC", 0.03894f, 0.03896f, 13460.42f, 10856.4f),
            DataModel("%0.03", "BTC/USDT", "Binance", "Indodax", 50340.00f, 50350.00f, 24000.00f, 20000.00f),
            DataModel("%0.04", "ETH/USDT", "Coinbase", "Kraken", 1800.50f, 1810.00f, 15000.00f, 12000.00f),
            DataModel("%0.01", "XRP/BTC", "Bitstamp", "Poloniex", 0.000021f, 0.000022f, 50000.00f, 45000.00f),
            DataModel("%0.05", "LTC/USDT", "Bittrex", "KuCoin", 120.00f, 121.00f, 3000.00f, 2800.00f),
            DataModel("%0.03", "DOGE/USDT", "Binance", "Coinbase", 0.06f, 0.061f, 250000.00f, 200000.00f),
            DataModel("%0.02", "ADA/BTC", "Kraken", "Bittrex", 0.00045f, 0.00046f, 10000.00f, 8000.00f),
            DataModel("%0.04", "DOT/USDT", "Huobi", "Binance", 45.00f, 46.00f, 1500.00f, 1200.00f),
            DataModel("%0.01", "LINK/BTC", "Bitfinex", "Indodax", 0.00075f, 0.00076f, 3000.00f, 2500.00f),
            DataModel("%0.06", "SOL/USDT", "FTX", "Kraken", 25.00f, 25.50f, 4000.00f, 3500.00f),
            DataModel("%0.02", "MATIC/ETH", "Binance", "Poloniex", 0.005f, 0.0051f, 15000.00f, 12000.00f),
            DataModel("%0.03", "UNI/USDT", "Bittrex", "Huobi", 30.00f, 30.50f, 6000.00f, 5500.00f)
        )

        // Initialize the adapter
        dataAdapter = DataAdapter(dataList)

        // Set up the RecyclerView
        binding.rvData.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dataAdapter
        }

        // Set the search button listener
        binding.btnSearch.setOnClickListener { performSearch() }

        binding.searchEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun performSearch() {
        val query = binding.searchEditText.text.toString().trim()
        val filteredDataList = if (query.isEmpty()) {
            dataAdapter.resetData()
            return
        } else {
            dataList.filter { dataModel ->
                dataModel.differenceItem.contains(query, ignoreCase = true) ||
                        dataModel.buyFrom.contains(query, ignoreCase = true) ||
                        dataModel.sellAt.contains(query, ignoreCase = true)
            }
        }

        dataAdapter.updateData(filteredDataList)
    }
}
