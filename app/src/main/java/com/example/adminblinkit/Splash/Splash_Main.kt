package com.example.adminblinkit.Splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.adminblinkit.Authentication.Signup
import com.example.adminblinkit.R

class Splash_Main : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_main)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, Signup::class.java))
            overridePendingTransition(R.anim.slide_in_right,0)
            finish()
        },3000)
    }
}