package com.example.arbitrade

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.arbitrade.databinding.FragmentInputDataBinding
import java.util.Calendar

class InputDataFragment : Fragment() {
    private lateinit var binding: FragmentInputDataBinding
    private lateinit var tradeViewModel: TradeViewModel
    private var isEditMode = false
    private var editPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInputDataBinding.inflate(inflater, container, false)
        tradeViewModel = ViewModelProvider(requireActivity())[TradeViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the DatePicker for the EditText
        binding.datePicker.setOnClickListener {
            showDatePickerDialog()
        }

        // Check if data was passed from AiFragment (for editing)
        val bundle = arguments
        if (bundle != null) {
            isEditMode = true
            val date = bundle.getString("date", "")
            val symbol = bundle.getString("symbol", "")
            val buyPrice = bundle.getFloat("buyPrice", 0f)
            val sellPrice = bundle.getFloat("sellPrice", 0f)
            val amount = bundle.getFloat("amount", 0f)
            val pnl = bundle.getFloat("pnl", 0f)
            val comment = bundle.getString("comment", "")

            editPosition = bundle.getInt("editPosition", -1)

            // Pre-fill the fields with existing data
            binding.datePicker.setText(date)
            binding.symbol.setText(symbol)
            binding.buyPrice.setText(buyPrice.toString())
            binding.sellPrice.setText(sellPrice.toString())
            binding.amount.setText(amount.toString())
            binding.comment.setText(comment)
        }

        // Listener for Buy Price and Sell Price changes to calculate PnL automatically
        binding.buyPrice.addTextChangedListener {
            calculatePnL()
        }

        binding.sellPrice.addTextChangedListener {
            calculatePnL()
        }

        binding.btnSubmit.setOnClickListener {
            val newDate = binding.datePicker.text.toString()
            val newSymbol = binding.symbol.text.toString()
            val newBuyPrice = binding.buyPrice.text.toString().toFloatOrNull() ?: 0f
            val newSellPrice = binding.sellPrice.text.toString().toFloatOrNull() ?: 0f
            val newAmount = binding.amount.text.toString().toFloatOrNull() ?: 0f

            // Gunakan nilai PnL yang dihitung secara otomatis
            val newPnl = calculatePnL()
            val newComment = binding.comment.text.toString()

            // Validasi input
            if (newDate.isNotEmpty() && newSymbol.isNotEmpty() && newBuyPrice != 0f &&
                newSellPrice != 0f && newAmount != 0f && newComment.isNotEmpty()) {

                // Membuat objek TradeData dengan nilai yang valid
                val newTrade = TradeData(newDate, newSymbol, newBuyPrice, newSellPrice, newAmount, newPnl, newComment)

                // Menambahkan atau memperbarui trade berdasarkan apakah dalam mode edit
                if (isEditMode && editPosition != -1) {
                    tradeViewModel.updateTrade(editPosition, newTrade)  // Memperbarui trade di list
                } else {
                    tradeViewModel.addTrade(newTrade)  // Menambahkan trade baru ke list
                }

                // Kembali ke fragment sebelumnya setelah menyimpan data
                parentFragmentManager.popBackStack()
            } else {
                // Menampilkan toast jika ada field yang belum diisi
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }


    }

    // Fungsi untuk menghitung PnL
    private fun calculatePnL(): Float {
        val buyPrice = binding.buyPrice.text.toString().toFloatOrNull() ?: 0f
        val sellPrice = binding.sellPrice.text.toString().toFloatOrNull() ?: 0f

        // Jika kedua nilai buyPrice dan sellPrice bukan nol, hitung PnL
        return if (buyPrice != 0f && sellPrice != 0f) {
            ((sellPrice - buyPrice) / buyPrice) * 100
        } else {
            0f  // Kembalikan 0 jika harga beli atau jual adalah nol
        }
    }



    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Set the date in DD/MM/YYYY format
                val formattedDate = formatDate(dayOfMonth, month + 1, year)
                binding.datePicker.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun formatDate(day: Int, month: Int, year: Int): String {
        // Ensure the day and month are always 2 digits
        val dayFormatted = String.format("%02d", day)
        val monthFormatted = String.format("%02d", month)
        return "$dayFormatted/$monthFormatted/$year"
    }
}