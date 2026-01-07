package com.example.adminblinkit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.adminblinkit.Fragments.AddProductFragment
import com.example.adminblinkit.Fragments.HomeFragment
import com.example.adminblinkit.Fragments.Order
import com.example.adminblinkit.Fragments.Setting
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Reference to BottomNavigationView
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Default fragment to display when activity is created
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        // Listener for BottomNavigationView item selection
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            val selectedFragment = when (item.itemId) {
                R.id.homee -> HomeFragment()
                R.id.addProduct -> AddProductFragment()
                R.id.order -> Order()
                R.id.Setting ->Setting()
                else -> null
            }
            selectedFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, it)
                    .commit()
            }
            true
        }
    }
}
