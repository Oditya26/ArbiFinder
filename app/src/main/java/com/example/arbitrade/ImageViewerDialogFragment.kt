package com.example.arbitrade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import com.example.arbitrade.databinding.FragmentImageViewerDialogBinding

class ImageViewerDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentImageViewerDialogBinding
    private val imageResources = listOf(
        R.drawable.tutor1,
        R.drawable.tutor2
    )
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.TransparentDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageViewerDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateImage()
        setupIndicators()

        // Close button
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // Image click navigation
        binding.imageView.setOnClickListener {
            val halfWidth = binding.imageView.width / 2
            val clickX = it.x
            if (clickX < halfWidth) {
                navigateToPrevious()
            } else {
                navigateToNext()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawableResource(android.R.color.transparent)
            setGravity(android.view.Gravity.CENTER)
        }
    }

    private fun navigateToNext() {
        currentIndex = (currentIndex + 1) % imageResources.size
        updateImage()
    }

    private fun navigateToPrevious() {
        currentIndex = if (currentIndex - 1 < 0) imageResources.size - 1 else currentIndex - 1
        updateImage()
    }

    private fun updateImage() {
        binding.imageView.setImageResource(imageResources[currentIndex])
        updateIndicators()
    }

    private fun setupIndicators() {
        binding.indicatorContainer.removeAllViews() // Bersihkan indikator lama
        repeat(imageResources.size) { index ->
            val indicator = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(16, 16).apply {
                    setMargins(8, 0, 8, 0)
                }
                setImageResource(if (index == currentIndex) R.drawable.indicator_active else R.drawable.indicator_inactive)
            }
            binding.indicatorContainer.addView(indicator)
        }
    }


    private fun updateIndicators() {
        binding.indicatorContainer.children.forEachIndexed { index, view ->
            val drawable = if (index == currentIndex) R.drawable.indicator_active else R.drawable.indicator_inactive
            (view as ImageView).setImageResource(drawable)
        }
    }

}
