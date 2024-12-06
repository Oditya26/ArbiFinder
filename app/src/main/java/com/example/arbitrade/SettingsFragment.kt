package com.example.arbitrade

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.arbitrade.databinding.FragmentSettingsBinding

@Suppress("DEPRECATION")
class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSettings()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, 0)
            insets
        }

        binding.volumeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volumeValue = progress * 50.0f
                binding.volumeValue.text = String.format("%.1f", volumeValue).replace(',', '.')
                saveSettings("volume", volumeValue)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.autoRefreshSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val autoRefreshValue = 10 + (progress * 5)
                binding.autoRefreshValue.text = "$autoRefreshValue s"
                saveSettings("autoRefresh", autoRefreshValue)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.autoRefreshSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.autoRefreshSeekbar.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.autoRefreshValue.visibility = if (isChecked) View.VISIBLE else View.GONE
            saveSettings("autoRefreshSwitch", isChecked)
        }

        binding.lowVolumeSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateLowVolumeDescription(isChecked)
            saveSettings("lowVolumeSwitch", isChecked)
        }

        binding.disableTradesSwitch.setOnCheckedChangeListener { _, isChecked -> saveSettings("disableTradesSwitch", isChecked) }
        binding.unknownStatusSwitch.setOnCheckedChangeListener { _, isChecked -> saveSettings("unknownStatusSwitch", isChecked) }
        binding.btcSwitch.setOnCheckedChangeListener { _, isChecked -> saveSettings("btcSwitch", isChecked) }
        binding.ethSwitch.setOnCheckedChangeListener { _, isChecked -> saveSettings("ethSwitch", isChecked) }
        binding.usdtSwitch.setOnCheckedChangeListener { _, isChecked -> saveSettings("usdtSwitch", isChecked) }
        binding.binanceSwitch.setOnCheckedChangeListener { _, isChecked -> saveSettings("binanceSwitch", isChecked) }
        binding.indodaxSwitch.setOnCheckedChangeListener { _, isChecked -> saveSettings("indodaxSwitch", isChecked) }

        updateLowVolumeDescription(binding.lowVolumeSwitch.isChecked)
        binding.autoRefreshSeekbar.visibility = if (binding.autoRefreshSwitch.isChecked) View.VISIBLE else View.GONE
        binding.autoRefreshValue.visibility = if (binding.autoRefreshSwitch.isChecked) View.VISIBLE else View.GONE

        binding.btnResetSetting.setOnClickListener { showResetSettingsDialog() }
    }

    @SuppressLint("SetTextI18n")
    private fun loadSettings() {
        with(sharedPreferences) {
            binding.disableTradesSwitch.isChecked = getBoolean("disableTradesSwitch", true)
            binding.unknownStatusSwitch.isChecked = getBoolean("unknownStatusSwitch", true)
            binding.lowVolumeSwitch.isChecked = getBoolean("lowVolumeSwitch", true)

            val volume = getFloat("volume", 0f)
            binding.volumeValue.text = String.format("%.1f", volume).replace(',', '.')
            binding.volumeSeekbar.progress = volume.toInt().div(50)

            binding.autoRefreshSwitch.isChecked = getBoolean("autoRefreshSwitch", true)

            val autoRefresh = getInt("autoRefresh", 10)
            binding.autoRefreshValue.text = "$autoRefresh s"
            binding.autoRefreshSeekbar.progress = (autoRefresh - 10).div(5)

            binding.btcSwitch.isChecked = getBoolean("btcSwitch", true)
            binding.ethSwitch.isChecked = getBoolean("ethSwitch", true)
            binding.usdtSwitch.isChecked = getBoolean("usdtSwitch", true)
            binding.binanceSwitch.isChecked = getBoolean("binanceSwitch", true)
            binding.indodaxSwitch.isChecked = getBoolean("indodaxSwitch", true)
        }
    }

    private fun saveSettings(key: String, value: Any) {
        with(sharedPreferences.edit()) {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is Int -> putInt(key, value)
                else -> throw IllegalArgumentException("Unsupported data type")
            }
            apply()
        }
    }

    private fun updateLowVolumeDescription(isChecked: Boolean) {
        val description = if (isChecked) {
            "<u><b>Warn</b></u> Low Volume Trades if volume is less than :"
        } else {
            "<u><b>Hide</b></u> Low Volume Trades if volume is less than :"
        }
        binding.lowVolumeDesc.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(description)
        }
    }

    private fun showResetSettingsDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Reset Settings")
        builder.setMessage("Are you sure you want to reset settings?")

        builder.setPositiveButton("Yes") { dialog: DialogInterface, _ ->
            resetSettings()
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog: DialogInterface, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun resetSettings() {
        // Set default values for each setting
        val defaultSettings = mapOf(
            "disableTradesSwitch" to true,
            "unknownStatusSwitch" to true,
            "lowVolumeSwitch" to true,
            "autoRefreshSwitch" to true,
            "volume" to 0f,
            "autoRefresh" to 10,
            "btcSwitch" to true,
            "ethSwitch" to true,
            "usdtSwitch" to true,
            "binanceSwitch" to true,
            "bitfinexSwitch" to true,
            "bitflyerSwitch" to true,
            "hitbtcSwitch" to true,
            "indodaxSwitch" to true
        )

        // Update UI elements to reflect reset values and save to SharedPreferences
        with(sharedPreferences.edit()) {
            for ((key, value) in defaultSettings) {
                when (value) {
                    is Boolean -> putBoolean(key, value)
                    is Float -> putFloat(key, value)
                    is Int -> putInt(key, value)
                }
            }
            apply()
        }

        // Load settings to update UI
        loadSettings()
    }
}