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
import androidx.fragment.app.Fragment
import com.example.arbitrade.databinding.ActivityNavigationBinding
import com.google.android.material.navigation.NavigationBarView

class NavigationActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener,
    View.OnClickListener {

    private lateinit var binding: ActivityNavigationBinding

    // Track the current selected item to apply animations based on previous and next screens
    private var currentItemId: Int = R.id.bottom_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ambil fragment terakhir yang dikunjungi
        currentItemId = getLastVisitedFragment()
        binding.bottomNavigationView.selectedItemId = currentItemId

        // Muat fragment terakhir
        val selectedFragment: Fragment = when (currentItemId) {
            R.id.bottom_home -> HomeFragment()
            R.id.bottom_settings -> SettingsFragment()
            R.id.bottom_about -> AboutFragment()
            R.id.bottom_ai -> AiFragment()
            else -> HomeFragment()
        }
        loadFragment(selectedFragment, true)

        // Set listener untuk navigasi item
        binding.bottomNavigationView.setOnItemSelectedListener(this)
    }

    private fun saveLastVisitedFragment(itemId: Int) {
        val preferences = getSharedPreferences("NavigationPreferences", MODE_PRIVATE)
        preferences.edit().putInt("lastVisitedFragment", itemId).apply()
    }

    private fun getLastVisitedFragment(): Int {
        val preferences = getSharedPreferences("NavigationPreferences", MODE_PRIVATE)
        return preferences.getInt("lastVisitedFragment", R.id.bottom_home) // Default ke Home
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bottom_home -> {
                if (currentItemId != R.id.bottom_home) {
                    startActivityWithAnimation(HomeFragment::class.java, currentItemId)
                }
            }
            R.id.bottom_settings -> {
                if (currentItemId != R.id.bottom_settings) {
                    startActivityWithAnimation(SettingsFragment::class.java, currentItemId)
                }
            }
            R.id.bottom_about -> {
                if (currentItemId != R.id.bottom_about) {
                    startActivityWithAnimation(AboutFragment::class.java, currentItemId)
                }
            }
            R.id.bottom_ai -> {
                if (currentItemId != R.id.bottom_ai) {
                    startActivityWithAnimation(AiFragment::class.java, currentItemId)
                }
            }
        }
    }

    private fun startActivityWithAnimation(targetActivity: Class<*>, fromItemId: Int) {
        val moveIntent = Intent(this@NavigationActivity, targetActivity)

        // Tentukan animasi berdasarkan halaman asal dan tujuan
        val (enterAnim, exitAnim) = when (fromItemId) {
            R.id.bottom_home -> {
                // Home ke halaman lain: Animasi dari kanan ke kiri
                R.anim.slide_in_right to R.anim.slide_out_left
            }
            R.id.bottom_settings -> {
                if (targetActivity == HomeFragment::class.java) {
                    // Settings ke Home: Animasi dari kiri ke kanan
                    R.anim.slide_in_left to R.anim.slide_out_right
                } else {
                    // Settings ke About: Animasi dari kanan ke kiri
                    R.anim.slide_in_right to R.anim.slide_out_left
                }
            }
            R.id.bottom_about -> {
                if (targetActivity == AiFragment::class.java) {
                    R.anim.slide_in_right to R.anim.slide_out_left
                } else {
                    R.anim.slide_in_left to R.anim.slide_out_right
                }
            }
            R.id.bottom_ai -> {
                // About ke halaman lain: Animasi dari kiri ke kanan
                R.anim.slide_in_left to R.anim.slide_out_right
            }
            else -> {
                // Default animasi dari kanan ke kiri
                R.anim.slide_in_right to R.anim.slide_out_left
            }
        }

        val options = ActivityOptions.makeCustomAnimation(
            this,
            enterAnim,  // Animasi saat masuk
            exitAnim    // Animasi saat keluar
        )
        startActivity(moveIntent, options.toBundle())
        finish()
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == currentItemId) return true

        val selectedFragment: Fragment = when (item.itemId) {
            R.id.bottom_home -> HomeFragment()
            R.id.bottom_settings -> SettingsFragment()
            R.id.bottom_about -> AboutFragment()
            R.id.bottom_ai -> AiFragment()
            else -> return false
        }

        loadFragment(selectedFragment, false)
        currentItemId = item.itemId

        // Simpan fragment terakhir yang dikunjungi
        saveLastVisitedFragment(currentItemId)

        return true
    }


    private fun loadFragment(fragment: Fragment, isInitial: Boolean) {
        val enterAnim: Int
        val exitAnim: Int

        // Apply animation based on the current and target fragment
        when (currentItemId) {
            R.id.bottom_home -> {
                enterAnim = R.anim.slide_in_right
                exitAnim = R.anim.slide_out_left
            }
            R.id.bottom_settings -> {
                if (fragment is HomeFragment) {
                    enterAnim = R.anim.slide_in_left
                    exitAnim = R.anim.slide_out_right
                } else {
                    enterAnim = R.anim.slide_in_right
                    exitAnim = R.anim.slide_out_left
                }
            }
            R.id.bottom_about -> {
                if (fragment is AiFragment) {
                    enterAnim = R.anim.slide_in_right
                    exitAnim = R.anim.slide_out_left
                } else {
                    enterAnim = R.anim.slide_in_left
                    exitAnim = R.anim.slide_out_right
                }
            }
            R.id.bottom_ai -> {
                enterAnim = R.anim.slide_in_left
                exitAnim = R.anim.slide_out_right
            }
            else -> {
                enterAnim = R.anim.slide_in_right
                exitAnim = R.anim.slide_out_left
            }
        }

        // Load the fragment with the selected animations
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                if (isInitial) 0 else enterAnim,  // No animation if it's the initial fragment
                exitAnim
            )
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}
