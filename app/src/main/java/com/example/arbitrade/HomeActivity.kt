package com.example.arbitrade

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arbitrade.databinding.ActivityHomeBinding
import com.google.android.material.navigation.NavigationBarView

class HomeActivity : AppCompatActivity(), View.OnClickListener,
    NavigationBarView.OnItemSelectedListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var dataAdapter: DataAdapter
    private lateinit var dataList: MutableList<DataModel> // Change to MutableList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Sample Data
        dataList = mutableListOf( // Initialize as mutable list
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

        // Initialize the adapter with the original data
        dataAdapter = DataAdapter(dataList)

        // Set up the RecyclerView
        binding.rvData.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = dataAdapter
        }

        binding.bottomNavigationView.selectedItemId = R.id.bottom_home
        binding.bottomNavigationView.setOnItemSelectedListener(this)

        // Set the search button listener
        binding.btnSearch.setOnClickListener { performSearch() }

        binding.searchEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch() // Lakukan pencarian ketika tombol "Search" ditekan
                true
            } else {
                false
            }
        }
    }

    private fun performSearch() {
        val query = binding.searchEditText.text.toString().trim()
        val filteredDataList = if (query.isEmpty()) {
            // If the query is empty, reset to original data
            dataAdapter.resetData()
            return // Exit early since we've reset
        } else {
            // Filter the original data list based on the search query
            dataList.filter { dataModel ->
                dataModel.differenceItem.contains(query, ignoreCase = true) ||
                        dataModel.buyFrom.contains(query, ignoreCase = true) ||
                        dataModel.sellAt.contains(query, ignoreCase = true)
            }
        }

        // Update the adapter with the filtered data
        dataAdapter.updateData(filteredDataList)
    }


    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bottom_home -> {
                // No action needed if the home button is selected
            }
            R.id.bottom_settings -> {
                startActivityWithAnimation(SettingsActivity::class.java)
            }
            R.id.bottom_about -> {
                startActivityWithAnimation(AboutActivity::class.java)
            }
        }
    }

    private fun startActivityWithAnimation(targetActivity: Class<*>) {
        val moveIntent = Intent(this@HomeActivity, targetActivity)
        val options = ActivityOptions.makeCustomAnimation(
            this,
            R.anim.slide_in_right,  // Animation when entering
            R.anim.slide_out_left    // Animation when exiting
        )
        startActivity(moveIntent, options.toBundle())
        finish()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bottom_home -> {
                true // No action needed if Home is selected
            }
            R.id.bottom_settings -> {
                startActivityWithAnimation(SettingsActivity::class.java)
                true
            }
            R.id.bottom_about -> {
                startActivityWithAnimation(AboutActivity::class.java)
                true
            }
            else -> false
        }
    }
}
