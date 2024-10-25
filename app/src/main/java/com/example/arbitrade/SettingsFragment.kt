package com.example.arbitrade

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.arbitrade.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Remove padding for full-screen
            v.setPadding(0, 0, 0, 0)
            insets
        }

        // Volume SeekBar listener
        binding.volumeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volumeValue = progress * 5.0f // Kelipatan 5
                binding.volumeValue.text = String.format("%.1f", volumeValue).replace('.', ',') // Format ke 1 desimal dan ganti . dengan ,
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Auto Refresh SeekBar listener
        binding.autoRefreshSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val autoRefreshValue = 10 + (progress * 5) // Kelipatan 5 mulai dari 10
                binding.autoRefreshValue.text = "$autoRefreshValue s" // Menampilkan nilai dengan 's' di belakang
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Listener untuk switch "Auto Refresh"
        binding.autoRefreshSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.autoRefreshSeekbar.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.autoRefreshValue.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Listener untuk switch "Low Volume"
        binding.lowVolumeSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateLowVolumeDescription(isChecked)
        }

        // Set initial description for low volume and visibility for auto refresh elements
        updateLowVolumeDescription(binding.lowVolumeSwitch.isChecked)

        binding.autoRefreshSeekbar.visibility = if (binding.autoRefreshSwitch.isChecked) View.VISIBLE else View.GONE
        binding.autoRefreshValue.visibility = if (binding.autoRefreshSwitch.isChecked) View.VISIBLE else View.GONE

        // Tambahkan listener untuk btn_reset_setting
        binding.btnResetSetting.setOnClickListener { showResetSettingsDialog() }
    }

    private fun updateLowVolumeDescription(isChecked: Boolean) {
        val description = if (isChecked) {
            "<u><b>Warn</b></u> Low Volume Trades if volume is less than :"
        } else {
            "<u><b>Hide</b></u> Low Volume Trades if volume is less than :"
        }

        // Use Html.fromHtml for formatting if needed
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

        builder.setPositiveButton("Yes") { dialog: DialogInterface, which: Int ->
            resetSettings() // Panggil fungsi untuk reset pengaturan
            dialog.dismiss() // Tutup dialog
        }

        builder.setNegativeButton("No") { dialog: DialogInterface, which: Int ->
            dialog.dismiss() // Tutup dialog tanpa melakukan apa-apa
        }

        val dialog: AlertDialog = builder.create()
        dialog.show() // Tampilkan dialog
    }

    private fun resetSettings() {
        // Mengatur semua switch menjadi ON
        binding.disableTradesSwitch.isChecked = true
        binding.unknownStatusSwitch.isChecked = true
        binding.lowVolumeSwitch.isChecked = true
        binding.autoRefreshSwitch.isChecked = true

        binding.btcSwitch.isChecked = true
        binding.ethSwitch.isChecked = true
        binding.usdtSwitch.isChecked = true

        binding.binanceSwitch.isChecked = true
        binding.bitfinexSwitch.isChecked = true
        binding.bitflyerSwitch.isChecked = true
        binding.hitbtcSwitch.isChecked = true
        binding.indodaxSwitch.isChecked = true

        // Opsional: Tampilkan pesan toast untuk mengonfirmasi reset
        Toast.makeText(requireContext(), "Settings have been reset", Toast.LENGTH_SHORT).show()
    }
}
