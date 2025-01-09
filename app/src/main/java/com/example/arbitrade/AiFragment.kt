package com.example.arbitrade

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arbitrade.databinding.FragmentAiBinding
import com.example.arbitrade.gemini.Content
import com.example.arbitrade.gemini.GeminiRequest
import com.example.arbitrade.gemini.GeminiResponse
import com.example.arbitrade.gemini.RetrofitClient
import retrofit2.Call
import com.example.arbitrade.gemini.Part
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.random.Random

class AiFragment : Fragment() {

    private var currentSortColumn: SortColumn = SortColumn.DATE
    private var isAscendingSort: Boolean = true
    enum class SortColumn {
        DATE, SYMBOL, BUY_PRICE, SELL_PRICE, AMOUNT, PNL
    }

    private val tradeList: List<TradeData>
        get() = tradeViewModel.tradeList.value ?: emptyList()

    private lateinit var binding: FragmentAiBinding
    private lateinit var tradeViewModel: TradeViewModel
    private lateinit var tradeAdapter: TradeAdapter

    // Daftar API key
    private val apiKeys = listOf(
        "AIzaSyCjAa5aOei9NvNQ6BPRmqrAdRJqnXYe3MQ",
        "AIzaSyAbApsGWOvyQnHoWdyamSXfImL4rDf9Q_E",
        "AIzaSyC6LtgelKxa4KgSBd3IcmIte3S6VmJaKjo",
        "AIzaSyAJ3oqG0j-VpKzihKAhpJ8SH7eqzFcx0Zk",
        "AIzaSyBxZT72tkwpfZLEQFWAtCkQIfrjgSWPsWc",
        "AIzaSyBrDeBaQ8i2z4FhiR5lt6fozFrr9uW2jbY",
        "AIzaSyBFzi9TJulowuHFTeZDpZBZrio5vKyj_gw",
        "AIzaSyAh_PPJrQHeOUzlQauO817xJG2gMu0sQd4",
        "AIzaSyDQqJIzhEoSAO7qf50x4h-JF4oBEK6w9q4",
        "AIzaSyAa6-c6BGNZrsaqO42CemXTrawqCBTIzaI",
        "AIzaSyDexz3GucxalLUddiS5duHEPiQ6ixgpOsc"
    )

    // Fungsi untuk memilih API key secara acak
    private fun getRandomApiKey(): String {
        return apiKeys[Random.nextInt(apiKeys.size)]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAiBinding.inflate(inflater, container, false)
        tradeViewModel = ViewModelProvider(requireActivity())[TradeViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the loading animation visible and TextView hidden initially
        binding.loadingDataAnim.visibility = View.VISIBLE
        binding.noSignalAnim.visibility = View.GONE
        binding.tvAiAdvice.visibility = View.GONE

        currentSortColumn = SortColumn.DATE // Default column
        isAscendingSort = false // Default descending order

        // Initialize ViewModel with SharedPreferences context
        tradeViewModel.initSharedPreferences(requireContext())

        // Set up RecyclerView
        tradeAdapter = TradeAdapter(
            tradeList = mutableListOf(), // Empty list initially
            onDelete = { trade ->
                tradeViewModel.deleteTrade(trade)

                // Jika tradeList kosong setelah penghapusan, tampilkan animasi loading
                if (tradeViewModel.tradeList.value.isNullOrEmpty()) {
                    binding.loadingDataAnim.visibility = View.VISIBLE
                    binding.tvAiAdvice.visibility = View.GONE
                } else {
                    // Cari data dengan tanggal paling baru
                    val latestTrade = tradeViewModel.tradeList.value
                        ?.mapNotNull { parseDate(it.date)?.let { date -> it to date } }
                        ?.maxByOrNull { it.second }

                    latestTrade?.let { (latestTradeData, _) ->
                        generatePromptForLatestTrade(latestTradeData)
                    }
                }
            },
            onEdit = { trade ->
                val tradeList = tradeViewModel.tradeList.value ?: emptyList()
                val position = tradeList.indexOf(trade)

                val bundle = Bundle().apply {
                    putString("date", trade.date)
                    putString("symbol", trade.symbol)
                    putFloat("buyPrice", trade.buyPrice ?: 0f)
                    putFloat("sellPrice", trade.sellPrice ?: 0f)
                    putFloat("amount", trade.amount ?: 0f)
                    putFloat("pnl", trade.pnl ?: 0f)
                    putString("comment", trade.comment)
                    putInt("editPosition", position)
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
            sortAndUpdateAdapter() // Urutkan ulang setiap kali data diperbarui
        }

        // Add new trade on button click
        binding.btnAdd.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, InputDataFragment())
                .addToBackStack(null)
                .commit()
        }

        sortAndUpdateAdapter()

        // Set listener for sort buttons
        binding.btnDate.setOnClickListener { toggleSortDate() }
        binding.btnSymbol.setOnClickListener { toggleSortSymbol() }
        binding.btnBuyPrice.setOnClickListener { toggleSortBuyPrice() }
        binding.btnSellPrice.setOnClickListener { toggleSortSellPrice() }
        binding.btnAmount.setOnClickListener { toggleSortAmount() }
        binding.btnPnl.setOnClickListener { toggleSortPnl() }

        // Initialize the drawable for the difference button
        binding.btnDate.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.baseline_keyboard_arrow_down_17,
            0
        )

        // Example: Get advice for the trade with the latest date
        val latestTrade = tradeList
            .mapNotNull { parseDate(it.date)?.let { date -> it to date } } // Only consider trades with valid dates
            .maxByOrNull { it.second } // Get the trade with the latest date

        latestTrade?.let { (trade, _) ->
            val date = trade.date
            val pnl = trade.pnl
            val comment = trade.comment

            val promptText = """
            Date: $date
            PnL (Profit and Loss): $pnl
            Comment: $comment
            Give advice 8-15 words
        """.trimIndent()

            // Log the prompt text to Logcat
            Log.d("AiFragment", "Prompt Text: $promptText")

            val apiKey = getRandomApiKey()
            generateContent(apiKey, promptText)
        }

    }

    private fun generatePromptForLatestTrade(trade: TradeData) {
        val date = trade.date
        val pnl = trade.pnl
        val comment = trade.comment

        val promptText = """
        Date: $date
        PnL (Profit and Loss): $pnl
        Comment: $comment
        Give advice 8-15 words
    """.trimIndent()

        Log.d("AiFragment", "Generated Prompt: $promptText")

        val apiKey = getRandomApiKey()
        generateContent(apiKey, promptText)
    }

    private fun retryAfterDelay(delayMillis: Long, action: () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(delayMillis) // Tunggu sesuai delay
            action() // Eksekusi ulang aksi
        }
    }

    private fun generateContent(apiKey: String, text: String) {
        val maxRetryCount = 3 // Jumlah maksimum percobaan
        var currentRetry = 0
        // Show the loading animation
        fun attemptRequest() {
            binding.loadingDataAnim.visibility = View.VISIBLE
            binding.noSignalAnim.visibility = View.GONE
            binding.tvAiAdvice.visibility = View.GONE // Hide the TextView initially

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = text))))
            )

            RetrofitClient.geminiApiService.generateContent(apiKey, request)
                .enqueue(object : Callback<GeminiResponse> {
                    @SuppressLint("SetTextI18n")
                    override fun onResponse(
                        call: Call<GeminiResponse>,
                        response: Response<GeminiResponse>
                    ) {
                        binding.loadingDataAnim.visibility = View.GONE
                        binding.noSignalAnim.visibility = View.GONE
                        if (response.isSuccessful) {
                            val geminiResponse = response.body()
                            geminiResponse?.let {
                                // Set the generated content into the TextView
                                val aiAdviceText =
                                    it.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                                binding.tvAiAdvice.text = aiAdviceText ?: "No advice generated"
                            }
                        } else {
                            // Handle API error
                            binding.tvAiAdvice.text = "API error: ${response.errorBody()?.string()}"
                        }

                        // Make the TextView visible after loading
                        binding.tvAiAdvice.visibility = View.VISIBLE
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                        // Hide the loading animation on failure
                        binding.loadingDataAnim.visibility = View.GONE

                        if (t.message?.contains("Unable to resolve host") == true) {
                            // Tampilkan animasi no_signal_anim jika tidak ada koneksi
                            binding.noSignalAnim.visibility = View.VISIBLE
                            binding.tvAiAdvice.visibility = View.GONE
                            // Retry jika masih ada kesempatan
                            if (currentRetry < maxRetryCount) {
                                currentRetry++
                                Log.w(
                                    "AiFragment",
                                    "Request failed, retrying... ($currentRetry/$maxRetryCount)"
                                )

                                // Jalankan ulang dengan jeda 10 detik
                                retryAfterDelay(10_000L) {
                                    attemptRequest()
                                }
                            } else {
                                binding.tvAiAdvice.text =
                                    "No Signal!"
                                binding.tvAiAdvice.visibility = View.VISIBLE
                                binding.noSignalAnim.visibility = View.VISIBLE
                            }
                        } else {
                            // Tampilkan pesan kesalahan lainnya
                            binding.tvAiAdvice.text = "Request failed: ${t.message}"
                            binding.tvAiAdvice.visibility = View.VISIBLE
                        }
                    }

                })
        }
        attemptRequest() // Mulai request pertama
    }


    private fun sortAndUpdateAdapter() {
        val sortedList = when (currentSortColumn) {
            SortColumn.DATE -> {
                if (isAscendingSort) {
                    tradeList.sortedBy { parseDate(it.date) }
                } else {
                    tradeList.sortedByDescending { parseDate(it.date) }
                }
            }
            SortColumn.SYMBOL -> if (isAscendingSort) tradeList.sortedBy { it.symbol } else tradeList.sortedByDescending { it.symbol }
            SortColumn.BUY_PRICE -> if (isAscendingSort) tradeList.sortedBy { it.buyPrice } else tradeList.sortedByDescending { it.buyPrice }
            SortColumn.SELL_PRICE -> if (isAscendingSort) tradeList.sortedBy { it.sellPrice } else tradeList.sortedByDescending { it.sellPrice }
            SortColumn.AMOUNT -> if (isAscendingSort) tradeList.sortedBy { it.amount } else tradeList.sortedByDescending { it.amount }
            SortColumn.PNL -> if (isAscendingSort) tradeList.sortedBy { it.pnl } else tradeList.sortedByDescending { it.pnl }
        }
        tradeAdapter.updateData(sortedList)
    }

    private fun parseDate(dateString: String?): LocalDate? {
        return try {
            if (dateString.isNullOrBlank()) null
            else LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (e: DateTimeParseException) {
            null // Return null if the date format is invalid
        }
    }


    private fun resetDrawables(except: View) {
        val buttons = listOf(
            binding.btnDate,
            binding.btnSymbol,
            binding.btnBuyPrice,
            binding.btnSellPrice,
            binding.btnAmount,
            binding.btnPnl
        )
        buttons.filter { it != except }
            .forEach { it.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0) }
    }

    private fun toggleSortDate() {
        resetDrawables(binding.btnDate)
        isAscendingSort = currentSortColumn != SortColumn.DATE || !isAscendingSort
        currentSortColumn = SortColumn.DATE

        sortAndUpdateAdapter()

        binding.btnDate.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            if (isAscendingSort) R.drawable.baseline_keyboard_arrow_up_17 else R.drawable.baseline_keyboard_arrow_down_17,
            0
        )
    }

    private fun toggleSortSymbol() {
        resetDrawables(binding.btnSymbol)
        isAscendingSort = currentSortColumn != SortColumn.SYMBOL || !isAscendingSort
        currentSortColumn = SortColumn.SYMBOL

        sortAndUpdateAdapter()

        binding.btnSymbol.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            if (isAscendingSort) R.drawable.baseline_keyboard_arrow_up_17 else R.drawable.baseline_keyboard_arrow_down_17,
            0
        )
    }

    private fun toggleSortBuyPrice() {
        resetDrawables(binding.btnBuyPrice)
        isAscendingSort = currentSortColumn != SortColumn.BUY_PRICE || !isAscendingSort
        currentSortColumn = SortColumn.BUY_PRICE

        sortAndUpdateAdapter()

        binding.btnBuyPrice.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            if (isAscendingSort) R.drawable.baseline_keyboard_arrow_up_17 else R.drawable.baseline_keyboard_arrow_down_17,
            0
        )
    }

    private fun toggleSortSellPrice() {
        resetDrawables(binding.btnSellPrice)
        isAscendingSort = currentSortColumn != SortColumn.SELL_PRICE || !isAscendingSort
        currentSortColumn = SortColumn.SELL_PRICE

        sortAndUpdateAdapter()

        binding.btnSellPrice.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            if (isAscendingSort) R.drawable.baseline_keyboard_arrow_up_17 else R.drawable.baseline_keyboard_arrow_down_17,
            0
        )
    }

    private fun toggleSortAmount() {
        resetDrawables(binding.btnAmount)
        isAscendingSort = currentSortColumn != SortColumn.AMOUNT || !isAscendingSort
        currentSortColumn = SortColumn.AMOUNT

        sortAndUpdateAdapter()

        binding.btnAmount.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            if (isAscendingSort) R.drawable.baseline_keyboard_arrow_up_17 else R.drawable.baseline_keyboard_arrow_down_17,
            0
        )
    }

    private fun toggleSortPnl() {
        resetDrawables(binding.btnPnl)
        isAscendingSort = currentSortColumn != SortColumn.PNL || !isAscendingSort
        currentSortColumn = SortColumn.PNL

        sortAndUpdateAdapter()

        binding.btnPnl.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            if (isAscendingSort) R.drawable.baseline_keyboard_arrow_up_17 else R.drawable.baseline_keyboard_arrow_down_17,
            0
        )
    }
}