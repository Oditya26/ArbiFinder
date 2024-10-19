package com.example.arbitrade

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.arbitrade.databinding.ActivitySettingsBinding
import com.google.android.material.navigation.NavigationBarView

class SettingsActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener, View.OnClickListener {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.bottomNavigationView.selectedItemId = R.id.bottom_settings

        // Set listener for navigation item selection
        binding.bottomNavigationView.setOnItemSelectedListener(this)

        // Set listener for clicks
        binding.bottomNavigationView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bottom_home -> {
                val moveIntent = Intent(this@SettingsActivity, HomeActivity::class.java)
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,  // Animasi saat masuk
                    R.anim.slide_out_right    // Animasi saat keluar
                )
                startActivity(moveIntent, options.toBundle())
                finish()
            }
            R.id.bottom_settings -> {
                // Tidak perlu aksi karena sudah di Settings
            }
            R.id.bottom_about -> {
                val moveIntent = Intent(this@SettingsActivity, AboutActivity::class.java)
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,  // Animasi saat masuk
                    R.anim.slide_out_left    // Animasi saat keluar
                )
                startActivity(moveIntent, options.toBundle())
                finish()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.bottom_home -> {
                val moveIntent = Intent(this@SettingsActivity, HomeActivity::class.java)
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
                // Tidak perlu aksi, sudah di halaman settings
                return true
            }
            R.id.bottom_about -> {
                val moveIntent = Intent(this@SettingsActivity, AboutActivity::class.java)
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,  // Animasi saat masuk
                    R.anim.slide_out_left    // Animasi saat keluar
                )
                startActivity(moveIntent, options.toBundle())
                finish()
                return true
            }
        }
        return false
    }
}
