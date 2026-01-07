package com.example.adminblinkit.Fragments

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.adminblinkit.About.About
import com.example.adminblinkit.Authentication.Signin
import com.example.adminblinkit.Authentication.Signup
import com.example.adminblinkit.MainActivity
import com.example.adminblinkit.R
import com.example.adminblinkit.databinding.FragmentSettingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Setting : Fragment() {

    // Nullable binding reference
    private var binding: FragmentSettingBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout and assign the binding
        binding = FragmentSettingBinding.inflate(inflater, container, false)

        // Using safe call for all binding references
        binding?.backprofile?.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }

        binding?.deleteAccount?.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setMessage("Are you sure you want to Delete coz You would lose your all data ? ")

            dialog.setPositiveButton("Yes") { _, _ ->
                deleteAccount()
            }

            dialog.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

            dialog.setNeutralButton("Help") { _, _ ->
                startActivity(Intent(requireContext(), About::class.java))
            }

            val alertDialog = dialog.create()
            alertDialog.show()
        }

        binding?.About?.setOnClickListener {
            startActivity(Intent(requireContext(), About::class.java))
        }

        binding?.LogOut?.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setMessage("Are you sure you want to Logout ?")

            dialog.setPositiveButton("Yes") { _, _ ->
                logOut()
            }

            dialog.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

            dialog.setNeutralButton("Help") { _, _ ->
                startActivity(Intent(requireContext(), About::class.java))
            }

            val alertDialog = dialog.create()
            alertDialog.show()
        }

        return binding?.root
    }

    private fun logOut() {
        FirebaseAuth.getInstance().signOut()

        // Start the Signin activity
        val intent = Intent(requireContext(), Signin::class.java)

        // Clear the activity stack and prevent going back to the Profile activity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        requireActivity().finish()
    }

    private fun deleteAccount() {
        val progressdilaog = ProgressDialog(requireContext())
        progressdilaog.setMessage("Permanent Deleting Your Account..")
        progressdilaog.show()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            val db = FirebaseDatabase.getInstance().reference

            // Pehle ProductsDetails me user ke products delete karo
            db.child("Admin").child("ProductsDetails")
                .orderByChild("adminUID")
                .equalTo(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (productSnapshot in snapshot.children) {
                                productSnapshot.ref.removeValue()
                            }
                        }

                        // Admin_Info se user delete karo
                        db.child("Admin").child("Admin_Info").child(uid)
                            .removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Firebase Authentication se user delete karo
                                    FirebaseAuth.getInstance().currentUser?.delete()
                                        ?.addOnCompleteListener { authTask ->
                                            if (authTask.isSuccessful) {
                                                FirebaseAuth.getInstance().signOut()
                                                Toast.makeText(
                                                    requireContext(),
                                                    "User deleted successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                progressdilaog.dismiss()

                                                // Signup screen pe navigate karo
                                                val intent = Intent(requireContext(), Signup::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                                startActivity(intent)
                                                requireActivity().finish()
                                            } else {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Failed to delete user from authentication: ${authTask.exception?.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                progressdilaog.dismiss()
                                            }
                                        }
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to delete user from Admin_Info: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    progressdilaog.dismiss()
                                }
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch product details: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        progressdilaog.dismiss()
                    }
                })
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            progressdilaog.dismiss()
        }
    }
}