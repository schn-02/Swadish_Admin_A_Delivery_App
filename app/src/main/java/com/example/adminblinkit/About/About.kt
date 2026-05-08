package com.example.adminblinkit.About

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.adminblinkit.R

class About : AppCompatActivity() {

    private lateinit var instagram: ImageView
    private lateinit var gmail: ImageView
    private lateinit var linkedin: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        instagram = findViewById(R.id.instagram)
        gmail = findViewById(R.id.gmail)
        linkedin = findViewById(R.id.linkedin)

        instagram.setOnClickListener {
            openUrl("https://www.instagram.com/schn_rwt02/")
        }

        linkedin.setOnClickListener {
            openUrl("https://www.linkedin.com/in/schn-rwt/")
        }

        gmail.setOnClickListener {
            openGmail("ssrxtech02@gmail.com")
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGmail(email: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, "Contact from Swadish Admin Panel")
            }
            startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }
}