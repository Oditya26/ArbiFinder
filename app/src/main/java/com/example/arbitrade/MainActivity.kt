package com.example.arbitrade

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.arbitrade.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Disable the decor fitting system windows to allow full-screen content
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply system window insets (for status bar and navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // No padding here if you want a full-screen view
            v.setPadding(0, 0, 0, 0)
            insets
        }

        val dataImage = resources.getString(R.string.image_welcome_page)

        Glide.with(this)
            .load(dataImage)
            .into(binding.imageWelcomePage)

        binding.btnNext.setOnClickListener {
            val moveIntent = Intent(this@MainActivity, NavigationActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_right,  // Slide-in animation
                R.anim.slide_out_left   // Slide-out animation
            )
            startActivity(moveIntent, options.toBundle())
            finish()
        }
    }
}
