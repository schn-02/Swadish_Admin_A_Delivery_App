package com.example.adminblinkit.Splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.adminblinkit.Authentication.Signin
import com.example.adminblinkit.Authentication.Signup
import com.example.adminblinkit.R

class mainSplash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_splash)
       Handler(Looper.getMainLooper()).postDelayed({
           startActivity(Intent(this , Signup::class.java))
           finish()
       },4000)
    }
}