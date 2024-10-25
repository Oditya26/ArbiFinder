package com.example.arbitrade

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.arbitrade.databinding.ActivityAboutBinding
import com.google.android.material.navigation.NavigationBarView

class AboutActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener, View.OnClickListener {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.bottomNavigationView.selectedItemId = R.id.bottom_about

        // Set listener for navigation item selection
        binding.bottomNavigationView.setOnItemSelectedListener(this)

        // Set listener for clicks
        binding.bottomNavigationView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bottom_home -> {
                val moveIntent = Intent(this@AboutActivity, HomeActivity::class.java)
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,  // Animasi saat masuk
                    R.anim.slide_out_right    // Animasi saat keluar
                )
                startActivity(moveIntent, options.toBundle())
                finish()
            }
            R.id.bottom_settings -> {
                val moveIntent = Intent(this@AboutActivity, SettingsActivity::class.java)
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,  // Animasi saat masuk
                    R.anim.slide_out_right    // Animasi saat keluar
                )
                startActivity(moveIntent, options.toBundle())
                finish()
            }
            R.id.bottom_about -> {
                // Tidak perlu aksi karena sudah di halaman About
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.bottom_home -> {
                val moveIntent = Intent(this@AboutActivity, HomeActivity::class.java)
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,  // Animasi saat masuk
                    R.anim.slide_out_right    // Animasi saat keluar
                )
                startActivity(moveIntent, options.toBundle())
                finish()
                return true
            }
            R.id.bottom_settings -> {
                val moveIntent = Intent(this@AboutActivity, SettingsActivity::class.java)
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                startActivity(moveIntent, options.toBundle())
                finish()
                return true
            }
            R.id.bottom_about -> {
                // Tidak perlu aksi karena sudah di halaman About
                return true
            }
        }
        return false
    }
}
