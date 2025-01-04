package com.example.arbitrade

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
            binding.pnl.setText(pnl.toString())
            binding.comment.setText(comment)
        }

        binding.btnSubmit.setOnClickListener {
            val newDate = binding.datePicker.text.toString()
            val newSymbol = binding.symbol.text.toString()
            val newBuyPrice = binding.buyPrice.text.toString().toFloatOrNull() ?: 0f
            val newSellPrice = binding.sellPrice.text.toString().toFloatOrNull() ?: 0f
            val newAmount = binding.amount.text.toString().toFloatOrNull() ?: 0f
            val newPnl = binding.pnl.text.toString().toFloatOrNull() ?: 0f
            val newComment = binding.comment.text.toString()

            if (newDate.isNotEmpty() && newSymbol.isNotEmpty() && newBuyPrice != 0f &&
                newSellPrice != 0f && newAmount != 0f && newPnl != 0f && newComment.isNotEmpty()) {

                val newTrade = TradeData(newDate, newSymbol, newBuyPrice, newSellPrice, newAmount, newPnl, newComment)

                // Add or update trade based on whether we are in edit mode
                if (isEditMode && editPosition != -1) {
                    tradeViewModel.updateTrade(editPosition, newTrade)  // Update the trade in the list
                } else {
                    tradeViewModel.addTrade(newTrade)  // Add new trade to the list
                }

                // Return to previous fragment after saving
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
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