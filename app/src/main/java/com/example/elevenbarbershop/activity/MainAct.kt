package com.example.elevenbarbershop.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.databinding.ActivityMainBinding
import com.example.elevenbarbershop.fragment.AboutFrg
import com.example.elevenbarbershop.fragment.BookActiveFrg
import com.example.elevenbarbershop.fragment.BookFrg
import com.example.elevenbarbershop.fragment.HomeFrg
import com.example.elevenbarbershop.fragment.ProfileFrg

class MainAct : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView = binding.bottomNavigation

        // Check intent data to determine which fragment to show
        val fragmentName = intent.getStringExtra("fragment")
        if (fragmentName == "about") {
            replaceFragment(AboutFrg())
            bottomNavigationView.selectedItemId = R.id.about
        }else if(fragmentName == "active_bookings"){
            replaceFragment(BookActiveFrg())
            bottomNavigationView.selectedItemId = R.id.calender
        } else {
            // Load default fragment
            replaceFragment(HomeFrg())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(HomeFrg())
                    true
                }
                R.id.about -> {
                    replaceFragment(AboutFrg())
                    true
                }
                R.id.calender -> {
                    replaceFragment(BookFrg())
                    true
                }
                R.id.profile -> {
                    replaceFragment(ProfileFrg())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}
