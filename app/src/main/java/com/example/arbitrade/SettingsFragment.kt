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
import android.widget.AdapterView
import android.widget.SeekBar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.arbitrade.databinding.FragmentSettingsBinding

@Suppress("DEPRECATION")
class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var isManualChange = false

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



        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, 0)
            insets
        }

        // Data untuk spinner
        val riskOptions = resources.getStringArray(R.array.user_profile_risk_options).toList()

        // Custom adapter untuk spinner
        val customAdapter = CustomSpinnerAdapter(
            requireContext(),
            R.layout.spinner_item,
            riskOptions
        )
        binding.userProfileRiskSpinner.adapter = customAdapter

        // Add listener for the risk profile spinner
        binding.userProfileRiskSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isManualChange) {
                    isManualChange = false
                    return
                }
                when (position) {
                    1 -> {
                        applyRiskSettings("low") // Low Risk
                        saveSettings("riskProfile", "low")  // Simpan pilihan
                    }
                    2 -> {
                        applyRiskSettings("high") // High Risk
                        saveSettings("riskProfile", "high")  // Simpan pilihan
                    }
                    else -> {
                        applyRiskSettings("custom") // Custom
                        saveSettings("riskProfile", "custom")  // Simpan pilihan
                    }
                }
            }


            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        loadSettings()

        // Monitor changes in settings
        monitorSettingChanges()

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

        updateLowVolumeDescription(binding.lowVolumeSwitch.isChecked)
        binding.autoRefreshSeekbar.visibility = if (binding.autoRefreshSwitch.isChecked) View.VISIBLE else View.GONE
        binding.autoRefreshValue.visibility = if (binding.autoRefreshSwitch.isChecked) View.VISIBLE else View.GONE

        binding.btnResetSetting.setOnClickListener { showResetSettingsDialog() }
    }

    private fun applyRiskSettings(riskLevel: String) {
        when (riskLevel) {
            "low" -> {
                saveSettings("disableTradesSwitch", false)
                saveSettings("unknownStatusSwitch", false)
                saveSettings("lowVolumeSwitch", false)
                saveSettings("volume", 1000f)
                saveSettings("autoRefreshSwitch", true)
                saveSettings("autoRefresh", 10)
                saveSettings("riskProfile", "low")  // Save the risk profile as low
                loadSettings() // Update UI with these settings
                binding.userProfileRiskSpinner.setSelection(1) // Set to "Low Risk"
            }
            "high" -> {
                saveSettings("disableTradesSwitch", true)
                saveSettings("unknownStatusSwitch", false)
                saveSettings("lowVolumeSwitch", true)
                saveSettings("volume", 500f)
                saveSettings("autoRefreshSwitch", true)
                saveSettings("autoRefresh", 10)
                saveSettings("riskProfile", "high")  // Save the risk profile as high
                loadSettings() // Update UI with these settings
                binding.userProfileRiskSpinner.setSelection(2) // Set to "High Risk"
            }
            "custom" -> {
                saveSettings("riskProfile", "custom")  // For custom, save the risk profile as "custom"
                loadSettings() // Update UI with these settings
                binding.userProfileRiskSpinner.setSelection(0) // Set to "Custom"
            }
            else -> {
                // For custom, save the risk profile as "custom"
                saveSettings("riskProfile", "custom")
                loadSettings() // Update UI with these settings
                binding.userProfileRiskSpinner.setSelection(0) // Set to "Custom"
            }
        }
    }




    private fun monitorSettingChanges() {
        // Listeners for manual changes
        binding.volumeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) setSpinnerToCustom()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.autoRefreshSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) setSpinnerToCustom()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Switch change listeners
        binding.autoRefreshSwitch.setOnCheckedChangeListener { _, _ -> setSpinnerToCustom() }
        binding.lowVolumeSwitch.setOnCheckedChangeListener { _, _ -> setSpinnerToCustom() }
        binding.disableTradesSwitch.setOnCheckedChangeListener { _, _ -> setSpinnerToCustom() }
        binding.unknownStatusSwitch.setOnCheckedChangeListener { _, _ -> setSpinnerToCustom() }
    }

    private fun setSpinnerToCustom() {
        if (!isManualChange) {
            isManualChange = true
            binding.userProfileRiskSpinner.setSelection(0) // Custom
        }
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

            // Load the user profile risk spinner setting
            val riskProfile = getString("riskProfile", "custom")
            when (riskProfile) {
                "low" -> binding.userProfileRiskSpinner.setSelection(1)
                "high" -> binding.userProfileRiskSpinner.setSelection(2)
                else -> binding.userProfileRiskSpinner.setSelection(0) // Custom
            }
        }
    }


    private fun saveSettings(key: String, value: Any) {
        with(sharedPreferences.edit()) {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is Int -> putInt(key, value)
                is String -> putString(key, value)
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
        // Reset to default values
        val defaultSettings = mapOf(
            "disableTradesSwitch" to true,
            "unknownStatusSwitch" to false,
            "lowVolumeSwitch" to true,
            "autoRefreshSwitch" to true,
            "volume" to 0f,
            "autoRefresh" to 10,
            "riskProfile" to "custom"
        )

        // Save and load the settings to update the UI
        with(sharedPreferences.edit()) {
            for ((key, value) in defaultSettings) {
                when (value) {
                    is Boolean -> putBoolean(key, value)
                    is Float -> putFloat(key, value)
                    is Int -> putInt(key, value)
                    is String -> putString(key, value)
                }
            }
            apply()
        }

        // Reload settings to update UI
        loadSettings()
    }
}