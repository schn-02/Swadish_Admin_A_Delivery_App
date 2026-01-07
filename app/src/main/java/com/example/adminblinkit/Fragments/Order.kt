package com.example.adminblinkit.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminblinkit.Adapters.Orders_Adapter
import com.example.adminblinkit.Models.product
import com.example.adminblinkit.R
import com.example.adminblinkit.databinding.FragmentAddProductBinding
import com.example.adminblinkit.databinding.FragmentOrderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.time.times


class Order : Fragment() {
    private var _binding: FragmentOrderBinding? = null
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: Orders_Adapter
    private lateinit var dataList: ArrayList<product>
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        dataList = ArrayList()
        adapter = Orders_Adapter(requireContext(), dataList)
        binding.recyclerOders.adapter = adapter
        binding.recyclerOders.layoutManager = LinearLayoutManager(requireContext())
        auth = FirebaseAuth.getInstance()

        binding.admin25.visibility = View.VISIBLE

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return binding.root
        }

        database = FirebaseDatabase.getInstance().reference
            .child("All_users").child("users")

        fetchDataFromFirebase()
        return binding.root
    }

    private fun fetchDataFromFirebase() {
        val userOrderList = mutableListOf<product>() // Temporary list to store fetched data

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    showToast("No orders found!")
                    binding.admin25.visibility = View.VISIBLE // Show no products view if no data found
                    return
                }

                var orderCount = 0 // Counter to track when all orders have been fetched

                for (userSnapshot in snapshot.children) {
                    val userUID = userSnapshot.key
                    val userOrderRef = database.child(userUID!!).child("userODERS")

                    userOrderRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(orderSnapshot: DataSnapshot) {
                            for (orderData in orderSnapshot.children) {
                                val adminUID = orderData.child("AdminUID").getValue(String::class.java)
                                if (adminUID == auth.currentUser?.uid) {
                                    // Extract product details
                                    val product = createProductFromSnapshot(orderData)
                                    product?.let { userOrderList.add(it) }
                                }
                            }

                            orderCount++
                            if (orderCount.toLong() == snapshot.childrenCount) {
                                if (userOrderList.isEmpty()) {
                                    binding.admin25.visibility = View.VISIBLE // Show no products view if list is empty
                                } else {
                                    binding.admin25.visibility = View.GONE // Hide no products view if there are orders
//                                    updateRecyclerView(userOrderList)
                                    dataList.clear()
                                            dataList.addAll(userOrderList)
        adapter.notifyDataSetChanged()

                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            showToast("Failed to load user orders: ${error.message}")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to load data: ${error.message}")
                binding.admin25.visibility = View.VISIBLE // Show no products view on error as well
            }
        })
    }

    private fun createProductFromSnapshot(orderData: DataSnapshot): product? {
        val productName = orderData.child("productName").getValue(String::class.java)
        val productPrice = orderData.child("productPrice").getValue(String::class.java)
        val productDate = orderData.child("productData").getValue(String::class.java)
        val productItemCount = orderData.child("productCount").getValue(Int::class.java)?.toString() ?: "0"
        val status = orderData.child("Status").getValue(String::class.java)

        val price = productPrice?.toDoubleOrNull() ?: 0.0
        val totalPrice = price * productItemCount.toDouble()

        return product(
            productTitle = productName,
            productPrice = totalPrice.toString(),
            itemCount = productItemCount,
            dateTime = productDate ?: "",
            Status = status
        )
    }

//    private fun updateRecyclerView(userOrderList: List<product>) {
//        dataList.clear()
//        dataList.addAll(userOrderList)
//        adapter.notifyDataSetChanged()
//    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
