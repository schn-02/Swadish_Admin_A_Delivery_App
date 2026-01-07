package com.example.adminblinkit.Authentication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.adminblinkit.MainActivity
import com.example.adminblinkit.R
import com.example.adminblinkit.databinding.ActivitySigninBinding
import com.example.adminblinkit.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Signin : AppCompatActivity()
{

    lateinit var binding:ActivitySigninBinding


    // Creating firebaseAuth object
    lateinit var auth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
         binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.signin.setOnClickListener {
            login()
        }
        binding.dontaccount.setOnClickListener{
            startActivity(Intent(this , Signup::class.java))
            finish()

        }



    }
    private fun login() {
        binding.admin2.visibility = View.VISIBLE
        val email = binding.enemail2.text.toString().trim()
        val pass = binding.enpass2.text.toString().trim()

        if (email.isBlank() || pass.isBlank()) {
            Toast.makeText(this, "Email and Password can't be blank", Toast.LENGTH_SHORT).show()
            binding.admin2.visibility = View.GONE
            return

        }

        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid
                // Check user type in Firebase Database
                if (uid != null) {
                    database.reference.child("Admin").child("Admin_Info").child(uid)
                        .get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                val userType = snapshot.child("userType").getValue(String::class.java)

                                if (userType == "Admin") {
                                    // Admin login successful
                                    Toast.makeText(this, "Welcome, Admin!", Toast.LENGTH_SHORT).show()
                                    binding.admin2.visibility = View.GONE

                                    startActivity(Intent(this@Signin, MainActivity::class.java))
                                    finish()
                                } else {
                                    // Not an admin, log out and show error
                                    Toast.makeText(this, "Login failed: Only admins can log in here.", Toast.LENGTH_SHORT).show()
                                    binding.admin2.visibility = View.GONE

                                    auth.signOut()
                                }
                            }
                        }.addOnFailureListener {
                            Toast.makeText(this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show()
                            binding.admin2.visibility = View.GONE

                        }
                }
            } else
            {
                Toast.makeText(this, "Please create an account first", Toast.LENGTH_SHORT).show()
                binding.admin2.visibility = View.GONE

            }
        }
    }
}