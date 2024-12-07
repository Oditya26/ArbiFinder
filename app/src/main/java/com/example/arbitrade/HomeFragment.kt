package com.example.arbitrade

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arbitrade.binance.BinanceApi
import com.example.arbitrade.binance.TickerResponseBinance
import com.example.arbitrade.databinding.FragmentHomeBinding
import com.example.arbitrade.indodax.IndodaxApi
import com.example.arbitrade.indodax.TickerResponseIndodax
import okhttp3.OkHttpClient
//import com.example.arbitrade.kucoin.KucoinApi
//import com.example.arbitrade.kucoin.TickerResponseKucoin
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    companion object {
        const val BASE_URL_INDODAX = "https://indodax.com/api/ticker/"
        const val BASE_URL_BINANCE = "https://testnet.binance.vision/api/v3/ticker/"
        const val TAG: String = "CHECK_RESPONSE"
    }

    private val autoRefreshHandler = Handler(Looper.getMainLooper())
    private val autoRefreshRunnable = object : Runnable {
        override fun run() {
            if (sharedPreferences.getBoolean(
                    "autoRefreshSwitch",
                    true
                )
            ) { // Check if auto-refresh is enabled
                processGroupedData() // Refresh data
                val interval = sharedPreferences.getInt(
                    "autoRefresh",
                    10
                ) * 1000L // Convert seconds to milliseconds
                autoRefreshHandler.postDelayed(this, interval) // Schedule the next refresh
            }
        }
    }

    private lateinit var sharedPreferences: SharedPreferences
    private var conversionRate: Float? = null // Menyimpan nilai 1 USDT dalam IDR
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
        sharedPreferences =
            requireContext().getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        val volumeValue = sharedPreferences.getFloat("volume", 0f)
        val disableTrades = sharedPreferences.getBoolean("disableTradesSwitch", true)
//        loadSettings()

        // Initialize and setup RecyclerView, Adapter, animations, and other listeners
        setupRecyclerViewAndListeners()

        // Start animation loop
        handler.post(animationRunnable)

        // Start auto-refresh
        startAutoRefresh()

        // Apply window insets for full-screen mode
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            v.setPadding(0, 0, 0, 0)
            insets
        }

        // Initialize empty dataList
        dataList = mutableListOf()

        // Set up the RecyclerView
        binding.rvData.apply {
            layoutManager = LinearLayoutManager(requireContext())
            dataAdapter = DataAdapter(dataList, volumeValue) // Adapter initialized with empty data
            adapter = dataAdapter
        }

        // Listener jika ada perubahan di SharedPreferences (opsional)
        sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "volume") {
                val updatedVolumeValue = sharedPreferences.getFloat("volume", 0f)
                dataAdapter.updateVolumeValue(updatedVolumeValue) // Update Adapter
            }
        }

        getConvertPrice {
            processGroupedData()
        }

        updateEmptyDataView()

        // Set the search button listener
        binding.btnSearch.setOnClickListener { performSearch() }

        // Set listener for sort buttons
        binding.btnDifference.setOnClickListener { toggleSortDifference() }
        binding.btnBuyFrom.setOnClickListener { toggleSortBuyFrom() }
        binding.btnSellAt.setOnClickListener { toggleSortSellAt() }

        // Initialize the drawable for the difference button to indicate sorting
        isAscendingDifference = false // Start with descending
        binding.btnDifference.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.baseline_keyboard_arrow_down_17,
            0
        )

        binding.searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun startAutoRefresh() {
        val interval =
            sharedPreferences.getInt("autoRefresh", 10) * 1000L // Interval in milliseconds
        autoRefreshHandler.postDelayed(autoRefreshRunnable, interval) // Start auto-refresh
    }

    private fun stopAutoRefresh() {
        autoRefreshHandler.removeCallbacks(autoRefreshRunnable) // Stop auto-refresh
    }

//    private fun loadSettings() {
//        with(sharedPreferences) {
//            val disableTrades = getBoolean("disableTradesSwitch", true)
//            val unknownStatus = getBoolean("unknownStatusSwitch", true)
//            val volume = getFloat("volume", 0f)
//            val autoRefresh = getInt("autoRefresh", 10)
//            val autoRefreshSwitch = getBoolean("autoRefreshSwitch", true)
//            val lowVolumeSwitch = getBoolean("lowVolumeSwitch", true)
//
//            // Contoh: Update UI berdasarkan data SharedPreferences
//            binding.disableTradesSwitch.text =
//                if (disableTrades) "Disable Trades Enabled" else "Disable Trades Disabled"
//            binding.unknownStatusSwitch.text =
//                if (unknownStatus) "Unknown Status Enabled" else "Unknown Status Disabled"
//            binding.volumeText.text = "Volume: %.1f".format(volume)
//            binding.refreshText.text = "Auto Refresh: $autoRefresh s"
//            binding.volumeSwitch.text =
//                if (lowVolumeSwitch) "Low Volume Enabled" else "Low Volume Disabled"
//            binding.refreshSwitch.text =
//                if (autoRefreshSwitch) "Auto Refresh ON" else "Auto Refresh OFF"
//        }
//    }


    private fun processGroupedData() {
        val symbols = listOf(
            "BTCUSDT",
            "ETHUSDT",
            "BONKUSDT",
            "FLOKIUSDT",
            "LUNCUSDT",
            "PEPEUSDT",
            "PUNDIXUSDT",
            "SHIBUSDT",
            "XECUSDT"
        )
        val symbols2 = listOf("ADAUSDT", "ZRXUSDT", "AEVOUSDT", "BNBUSDT", "PYTHUSDT", "TURBOUSDT")
        val groupedData = mutableMapOf<String, MutableList<DataModel>>()
        var remainingCalls = symbols.size * 2 + symbols2.size * 2 // Total API calls

        symbols.forEach { symbol ->
            getAllTickersIndodax(symbol.lowercase()) { data ->
                groupedData.getOrPut(symbol) { mutableListOf() }.add(data)
                checkAndUpdateData(groupedData, --remainingCalls)
            }
            getAllTickersBinance(symbol) { data ->
                groupedData.getOrPut(symbol) { mutableListOf() }.add(data)
                checkAndUpdateData(groupedData, --remainingCalls)
            }
        }

        symbols2.forEach { symbol ->
            val indodaxSymbol = symbol.replace("USDT", "IDR").lowercase()
            getAllTickersIndodax(indodaxSymbol) { data ->
                groupedData.getOrPut(symbol) { mutableListOf() }.add(data)
                checkAndUpdateData(groupedData, --remainingCalls)
            }
            getAllTickersBinance(symbol) { data ->
                groupedData.getOrPut(symbol) { mutableListOf() }.add(data)
                checkAndUpdateData(groupedData, --remainingCalls)
            }
        }
    }

    private fun checkAndUpdateData(groupedData: Map<String, List<DataModel>>, remainingCalls: Int) {
        if (remainingCalls == 0) {
            calculateAndDisplayGroupedData(groupedData)
            sortAndUpdateAdapter()
        }
    }

    private fun calculateAndDisplayGroupedData(groupedData: Map<String, List<DataModel>>) {
        dataList.clear() // Hapus data lama

        // Ambil nilai disableTradesSwitch, unknownStatusSwitch, dan lowVolumeSwitch dari SharedPreferences
        val disableTradesSwitch = sharedPreferences.getBoolean("disableTradesSwitch", true)
        val unknownStatusSwitch = sharedPreferences.getBoolean("unknownStatusSwitch", true)
        val lowVolumeSwitch =
            sharedPreferences.getBoolean("lowVolumeSwitch", true) // Ambil nilai lowVolumeSwitch
        val volume = sharedPreferences.getFloat("volume", 0f)

        groupedData.forEach { (symbol, dataListGrouped) ->
            if (dataListGrouped.size >= 2) { // Pastikan ada minimal 2 data
                val bestBuy = dataListGrouped.minByOrNull { it.buyValue }
                val bestSell = dataListGrouped.maxByOrNull { it.sellValue }

                if (bestBuy != null && bestSell != null) {
                    // Periksa apakah BuyFrom dan SellAt berbeda
                    if (bestBuy.buyFrom != bestSell.sellAt) {
                        val differenceValue =
                            calculateDifference(bestSell.sellValue, bestBuy.buyValue)

                        // Filter berdasarkan nilai difference jika disableTradesSwitch false
                        if (disableTradesSwitch || differenceValue >= 0.5) {
                            // Jika unknownStatusSwitch false, pastikan buyValue dan sellValue bukan 0
                            if (unknownStatusSwitch || (bestBuy.buyValue > 0 && bestSell.sellValue > 0)) {
                                // Filter berdasarkan lowVolumeSwitch jika false
                                if (lowVolumeSwitch || (bestBuy.buyVolume >= volume && bestSell.sellVolume >= volume)) {
                                    val groupedModel = DataModel(
                                        difference = differenceValue,
                                        differenceItem = symbol,
                                        buyFrom = bestBuy.buyFrom,
                                        sellAt = bestSell.sellAt,
                                        buyValue = bestBuy.buyValue,
                                        sellValue = bestSell.sellValue,
                                        buyVolume = bestBuy.buyVolume,
                                        sellVolume = bestSell.sellVolume
                                    )

                                    // Tambahkan ke dataList utama
                                    dataList.add(groupedModel)

                                    // Log hasil grouping
                                    Log.i(TAG, "Grouped Data: $groupedModel")
                                }
                            }
                        }
                    } else {
                        Log.i(
                            TAG,
                            "Filtered out data with same BuyFrom and SellAt: BuyFrom=${bestBuy.buyFrom}, SellAt=${bestSell.sellAt}"
                        )
                    }
                }
            }
        }

        // Perbarui RecyclerView setelah data ditambahkan
        dataAdapter.notifyDataSetChanged()
        updateEmptyDataView()
    }


    private fun sortAndUpdateAdapter() {
        val sortedList = dataList.sortedByDescending { it.difference }
        dataAdapter.updateData(sortedList)
        updateEmptyDataView() // Update tampilan setelah data di-sortir
    }

    private fun getConvertPrice(onComplete: () -> Unit) {
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL_INDODAX)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IndodaxApi::class.java)

        api.getTickers(symbol = "usdtidr").enqueue(object : Callback<TickerResponseIndodax> {
            override fun onResponse(
                call: Call<TickerResponseIndodax>,
                response: Response<TickerResponseIndodax>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { tickerResponse ->
                        val ticker = tickerResponse.ticker
                        val usdtPrice = ticker.last
                        Log.d("USDT_PRICE", "Response: $ticker")

                        run {
                            conversionRate = usdtPrice
                            Log.d("USDT_PRICE", "1 USDT = $usdtPrice IDRT")
                        }
                    }
                } else {
                    Log.e("USDT_PRICE", "Failed to fetch price: ${response.errorBody()?.string()}")
                }
                onComplete() // Continue after response
            }

            override fun onFailure(call: Call<TickerResponseIndodax>, t: Throwable) {
                Log.e("USDT_PRICE", "Error: ${t.message}")
                onComplete() // Continue even if there is an error
            }
        })
    }

    // Modify API calls to accept a callback
    private fun getAllTickersIndodax(symbol: String, callback: (DataModel) -> Unit) {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
            .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
            .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
            .build()
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL_INDODAX)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IndodaxApi::class.java)

        api.getTickers(symbol).enqueue(object : Callback<TickerResponseIndodax> {
            override fun onResponse(
                call: Call<TickerResponseIndodax>,
                response: Response<TickerResponseIndodax>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { tickerResponse ->
                        val ticker = tickerResponse.ticker

                        if (symbol.endsWith("idr", ignoreCase = true)) {
                            // Jika simbol IDR, lakukan konversi
                            if (conversionRate != null) {
                                val rate = conversionRate!!

                                // Konversi harga beli dan jual IDR ke USDT
                                val buyInUSDT = ticker.buy / rate
                                val sellInUSDT = ticker.sell / rate

                                // Konversi volume IDR ke volume dalam USDT
                                // Menggunakan buy price sebagai acuan
                                val volumeInUSDT = ticker.vol_idr / rate

                                val data = DataModel(
                                    difference = 0f, // Placeholder, hitung sesuai kebutuhan
                                    differenceItem = symbol,
                                    buyFrom = "Indodax",
                                    sellAt = "Indodax",
                                    buyValue = buyInUSDT,
                                    sellValue = sellInUSDT,
                                    buyVolume = volumeInUSDT,
                                    sellVolume = volumeInUSDT
                                )
                                callback(data)
                            } else {
                                Log.e(TAG, "Conversion rate not available")
                            }
                        } else {
                            // Jika simbol bukan IDR, gunakan nilai asli
                            val data = DataModel(
                                difference = 0f, // Placeholder, hitung sesuai kebutuhan
                                differenceItem = symbol,
                                buyFrom = "Indodax",
                                sellAt = "Indodax",
                                buyValue = ticker.buy,
                                sellValue = ticker.sell,
                                buyVolume = ticker.vol_usdt,
                                sellVolume = ticker.vol_usdt
                            )
                            callback(data)
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to fetch tickers: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<TickerResponseIndodax>, t: Throwable) {
                Log.e(TAG, "Indodax API Failure: ${t.message}")
            }
        })
    }

    // Similar modification for Binance
    private fun getAllTickersBinance(symbol: String, callback: (DataModel) -> Unit) {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
            .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
            .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
            .build()

        val api = Retrofit.Builder()
            .baseUrl(BASE_URL_BINANCE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BinanceApi::class.java)

        api.getTickers(symbol).enqueue(object : Callback<TickerResponseBinance> {
            override fun onResponse(
                call: Call<TickerResponseBinance>,
                response: Response<TickerResponseBinance>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { tickerResponse ->
                        val data = DataModel(
                            difference = 0f, // Placeholder
                            differenceItem = tickerResponse.symbol,
                            buyFrom = "Binance",
                            sellAt = "Binance",
                            buyValue = tickerResponse.askPrice,
                            sellValue = tickerResponse.bidPrice,
                            buyVolume = tickerResponse.volume,
                            sellVolume = tickerResponse.volume
                        )
                        callback(data)
                    }
                }
            }

            override fun onFailure(call: Call<TickerResponseBinance>, t: Throwable) {
                Log.e(TAG, "Binance API Failure: ${t.message}")
            }
        })
    }

    // Method to calculate the difference
    private fun calculateDifference(sellValue: Float, buyValue: Float): Float {
        val differenceValue = ((sellValue - buyValue) / buyValue) * 100
        // Format to 2 decimal places and replace comma with dot (for locales that use comma)
        val formatted = String.format(Locale.US, "%.2f", differenceValue)
        return formatted.toFloat()
    }

    private fun updateEmptyDataView() {
        if (dataList.isEmpty()) {
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
        binding.btnDifference.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            if (isAscendingDifference) R.drawable.baseline_keyboard_arrow_up_17 else R.drawable.baseline_keyboard_arrow_down_17,
            0
        )
    }

    private fun toggleSortBuyFrom() {
        resetDrawables(binding.btnBuyFrom) // Reset drawable dari tombol lain
        isAscendingBuyFrom = !isAscendingBuyFrom // Toggle status sorting

        val sortedList = if (isAscendingBuyFrom) {
            dataList.sortedBy { it.buyVolume } // Sort ascending
        } else {
            dataList.sortedByDescending { it.buyVolume } // Sort descending
        }

        dataAdapter.updateData(sortedList)
        binding.btnBuyFrom.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            if (isAscendingBuyFrom) R.drawable.baseline_keyboard_arrow_up_17 else R.drawable.baseline_keyboard_arrow_down_17,
            0
        )
    }

    private fun toggleSortSellAt() {
        resetDrawables(binding.btnSellAt) // Reset drawable dari tombol lain
        isAscendingSellAt = !isAscendingSellAt // Toggle status sorting

        val sortedList = if (isAscendingSellAt) {
            dataList.sortedBy { it.sellVolume } // Sort ascending
        } else {
            dataList.sortedByDescending { it.sellVolume } // Sort descending
        }

        dataAdapter.updateData(sortedList)
        binding.btnSellAt.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            if (isAscendingSellAt) R.drawable.baseline_keyboard_arrow_up_17 else R.drawable.baseline_keyboard_arrow_down_17,
            0
        )
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
        stopAutoRefresh() // Hentikan auto-refresh saat fragment dihancurkan
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