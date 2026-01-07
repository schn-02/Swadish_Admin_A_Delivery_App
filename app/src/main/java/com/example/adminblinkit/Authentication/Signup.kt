package com.example.adminblinkit.Authentication

import android.content.Intent

import android.media.audiofx.BassBoost
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.adminblinkit.R
import com.example.adminblinkit.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.provider.Settings
import android.view.View
import com.example.adminblinkit.MainActivity
import com.example.blinklit.Models.users
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class Signup : AppCompatActivity() {

    lateinit var name: EditText
    lateinit var auth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null)
        {
                // User is signed in with a registered account
                startActivity(Intent(this@Signup, MainActivity::class.java))
                finish()
            }
                binding.alreadyaccount.setOnClickListener{
                    startActivity(Intent(this , Signin::class.java))
                    finish()
                }

        binding.Signup.setOnClickListener {
            signUpUser()
        }

    }


    private fun signUpUser() {
        binding.admin.visibility = View.VISIBLE
        val email = binding.enemail.text.toString().trim()
        val pass = binding.enpass.text.toString().trim()
        val confirmPassword = binding.enconfirmpass.text.toString().trim()
        val name = binding.enname.text.toString().trim()
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        // Input validation
        if (email.isEmpty() || pass.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            binding.admin.visibility = View.GONE
            return

        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            binding.admin.visibility = View.GONE
            return
        }
        if (pass != confirmPassword) {
            Toast.makeText(this, "Password and Confirm Password do not match", Toast.LENGTH_SHORT).show()
            binding.admin.visibility = View.GONE
            return

        }

        val usersRef = database.reference.child("Admin").child("Admin_Info")

        // Check if device ID already registered
        usersRef.orderByChild("admindeviceId").equalTo(deviceId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Device ID already registered
                        Toast.makeText(this@Signup, "Device is Already Registers , Please Login...", Toast.LENGTH_SHORT).show()
                        binding.admin.visibility = View.GONE

                        auth.signOut()  // Logout any previously logged-in user
                        startActivity(Intent(this@Signup, Signin::class.java))
                        finish()
                    } else {
                        // Proceed with registration if not registered on this device
                        setupSignUpProcess(usersRef)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Signup, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    binding.admin.visibility = View.GONE

                }
            })
    }
    private fun setupSignUpProcess(usersRef: DatabaseReference) {

            val email = binding.enemail.text.toString()
            val pass = binding.enpass.text.toString()
            val name = binding.enname.text.toString()
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)


            // Check if email is already registered
            usersRef.orderByChild("userEmail").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(emailSnapshot: DataSnapshot) {
                        if (emailSnapshot.exists()) {
                            Toast.makeText(this@Signup, "Ye email pehle se registered hai. Login karein.", Toast.LENGTH_SHORT).show()
                            binding.admin.visibility = View.GONE


                            auth.signOut()  // Logout any previously logged-in user
                            startActivity(Intent(this@Signup, Signin::class.java))
                            finish()
                        } else {
                            auth.createUserWithEmailAndPassword(email, pass)
                                .addOnCompleteListener(this@Signup) { signUpTask ->
                                    if (signUpTask.isSuccessful) {
                                        val userId = signUpTask.result.user!!.uid
                                        val newUser = users(
                                            uid = userId,
                                            AdminName = name,
                                            AdminEmail = email,
                                            AdmindeviceId = deviceId.toString(),
                                            userType = "Admin" ,
                                            AdminPassword = pass
                                        )
                                        usersRef.child(userId).setValue(newUser)
                                            .addOnCompleteListener { dbTask ->
                                                if (dbTask.isSuccessful) {
                                                    Toast.makeText(this@Signup, "Signup Successful", Toast.LENGTH_SHORT).show()
                                                    binding.admin.visibility = View.VISIBLE

                                                    startActivity(Intent(this@Signup, MainActivity::class.java))
                                                    finish()
                                                } else {
                                                    Toast.makeText(this@Signup, "User data save nahi ho paya", Toast.LENGTH_SHORT).show()
                                                    binding.admin.visibility = View.GONE

                                                }
                                            }
                                    } else {
                                        val errorMsg = signUpTask.exception?.message ?: "Error"
                                        Toast.makeText(this@Signup, "Signup Failed: $errorMsg", Toast.LENGTH_SHORT).show()
                                        binding.admin.visibility = View.GONE

                                    }
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@Signup, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        binding.admin.visibility = View.GONE

                    }
                })
        }
    }

