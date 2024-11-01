package com.example.arbitrade

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arbitrade.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var dataAdapter: DataAdapter
    private var dataList: MutableList<DataModel> = mutableListOf() // Initialize here
    private var isAscendingDifference: Boolean = false // Status sorting untuk difference
    private var isAscendingBuyFrom: Boolean = false // Status sorting untuk buy from
    private var isAscendingSellAt: Boolean = false // Status sorting untuk sell at
    private val handler = Handler(Looper.getMainLooper())
    private val animationRunnable = object : Runnable {
        override fun run() {
            binding.emptyDataAnim.playAnimation()
            handler.postDelayed(this, 10000) // Ulangi setiap 10 detik
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi dan setup RecyclerView, Adapter, animasi, serta listeners lainnya
        setupRecyclerViewAndListeners()

        // Mulai loop animasi
        handler.post(animationRunnable)

        // Apply window insets for full-screen mode
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
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

        // Sort data by difference descending at the start
        val sortedList = dataList.sortedByDescending { it.difference }

        // Initialize the adapter with sorted data
        dataAdapter = DataAdapter(sortedList)

        // Set up the RecyclerView
        binding.rvData.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dataAdapter
        }

        updateEmptyDataView()

        // Set the search button listener
        binding.btnSearch.setOnClickListener { performSearch() }

        // Set listener untuk tombol sorting
        binding.btnDifference.setOnClickListener { toggleSortDifference() }
        binding.btnBuyFrom.setOnClickListener { toggleSortBuyFrom() }
        binding.btnSellAt.setOnClickListener { toggleSortSellAt() }

        // Initialize the drawable for the difference button to indicate sorting
        isAscendingDifference = false // Set this to false since we are starting with descending
        binding.btnDifference.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_keyboard_arrow_down_17, 0)

        binding.searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch()
                true
            } else {
                false
            }
        }
    }
    private fun updateEmptyDataView() {
        if (dataAdapter.itemCount == 0) {
            binding.emptyDataAnim.visibility = View.VISIBLE
            binding.rvData.visibility = View.GONE
        } else {
            binding.emptyDataAnim.visibility = View.GONE
            binding.rvData.visibility = View.VISIBLE
        }
    }

    private fun resetDrawables(except: View) {
        // Reset drawable pada tombol lainnya
        if (except != binding.btnDifference) {
            binding.btnDifference.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
        if (except != binding.btnBuyFrom) {
            binding.btnBuyFrom.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
        if (except != binding.btnSellAt) {
            binding.btnSellAt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    private fun toggleSortDifference() {
        resetDrawables(binding.btnDifference) // Reset drawable dari tombol lain
        isAscendingDifference = !isAscendingDifference // Toggle status sorting

        val sortedList = if (isAscendingDifference) {
            dataList.sortedBy { it.difference } // Sort ascending
        } else {
            dataList.sortedByDescending { it.difference } // Sort descending
        }

        dataAdapter.updateData(sortedList)
        binding.btnDifference.setCompoundDrawablesWithIntrinsicBounds(0, 0, if (isAscendingDifference) R.drawable.baseline_keyboard_arrow_up_17 else R.drawable.baseline_keyboard_arrow_down_17, 0)
    }

    private fun toggleSortBuyFrom() {
        resetDrawables(binding.btnBuyFrom) // Reset drawable dari tombol lain
        isAscendingBuyFrom = !isAscendingBuyFrom // Toggle status sorting

        val sortedList = if (isAscendingBuyFrom) {
            dataList.sortedBy { it.buyValue } // Sort ascending
        } else {
            dataList.sortedByDescending { it.buyValue } // Sort descending
        }

        dataAdapter.updateData(sortedList)
        binding.btnBuyFrom.setCompoundDrawablesWithIntrinsicBounds(0, 0, if (isAscendingBuyFrom) R.drawable.baseline_keyboard_arrow_up_17 else R.drawable.baseline_keyboard_arrow_down_17, 0)
    }

    private fun toggleSortSellAt() {
        resetDrawables(binding.btnSellAt) // Reset drawable dari tombol lain
        isAscendingSellAt = !isAscendingSellAt // Toggle status sorting

        val sortedList = if (isAscendingSellAt) {
            dataList.sortedBy { it.sellValue } // Sort ascending
        } else {
            dataList.sortedByDescending { it.sellValue } // Sort descending
        }

        dataAdapter.updateData(sortedList)
        binding.btnSellAt.setCompoundDrawablesWithIntrinsicBounds(0, 0, if (isAscendingSellAt) R.drawable.baseline_keyboard_arrow_up_17 else R.drawable.baseline_keyboard_arrow_down_17, 0)
    }



    private fun performSearch() {
        val query = binding.searchEditText.text.toString().trim()
        val filteredDataList = if (query.isEmpty()) {
            dataAdapter.resetData()
            updateEmptyDataView() // Periksa tampilan kosong setelah reset
            return
        } else {
            dataList.filter { dataModel ->
                dataModel.differenceItem.contains(query, ignoreCase = true) ||
                        dataModel.buyFrom.contains(query, ignoreCase = true) ||
                        dataModel.sellAt.contains(query, ignoreCase = true)
            }
        }

        // Update data di adapter dan periksa jika hasil pencarian kosong
        dataAdapter.updateData(filteredDataList)
        if (filteredDataList.isEmpty()) {
            // Jika hasil pencarian kosong, tampilkan animasi kosong
            binding.emptyDataAnim.visibility = View.VISIBLE
            binding.rvData.visibility = View.GONE
        } else {
            // Jika ada hasil, tampilkan RecyclerView dan sembunyikan animasi
            binding.emptyDataAnim.visibility = View.GONE
            binding.rvData.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(animationRunnable) // Hentikan loop ketika view dihancurkan
    }

    private fun setupRecyclerViewAndListeners() {
        dataAdapter = DataAdapter(dataList)

        binding.rvData.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dataAdapter
        }

        // Mulai loop animasi
        handler.post(animationRunnable)

        updateEmptyDataView()

        // Inisialisasi listeners untuk sorting dan searching
        binding.btnSearch.setOnClickListener { performSearch() }
        binding.btnDifference.setOnClickListener { toggleSortDifference() }
        binding.btnBuyFrom.setOnClickListener { toggleSortBuyFrom() }
        binding.btnSellAt.setOnClickListener { toggleSortSellAt() }

        binding.searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch()
                true
            } else {
                false
            }
        }
    }


}
