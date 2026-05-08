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
import com.example.adminblinkit.databinding.FragmentOrderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Order : Fragment() {

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: Orders_Adapter
    private lateinit var dataList: ArrayList<product>

    private var ordersListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        dataList = ArrayList()
        adapter = Orders_Adapter(requireContext(), dataList)

        binding.recyclerOders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerOders.adapter = adapter

        showLoading()

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            showEmpty()
            return binding.root
        }

        database = FirebaseDatabase.getInstance()
            .reference
            .child("All_users")
            .child("users")

        fetchDataFromFirebase()

        return binding.root
    }

    private fun fetchDataFromFirebase() {

        ordersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val userOrderList = ArrayList<product>()

                if (!snapshot.exists()) {
                    dataList.clear()
                    adapter.notifyDataSetChanged()
                    showEmpty()
                    return
                }

                val currentAdminUid = auth.currentUser?.uid

                for (userSnapshot in snapshot.children) {

                    val userOrdersSnapshot = userSnapshot.child("userODERS")

                    for (orderData in userOrdersSnapshot.children) {

                        val adminUID = orderData.child("AdminUID").getValue(String::class.java)

                        if (adminUID == currentAdminUid) {
                            val orderProduct = createProductFromSnapshot(orderData)

                            if (orderProduct != null) {
                                userOrderList.add(orderProduct)
                            }
                        }
                    }
                }

                dataList.clear()
                dataList.addAll(userOrderList)
                adapter.notifyDataSetChanged()

                if (dataList.isEmpty()) {
                    showEmpty()
                } else {
                    showOrders()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Failed to load data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                showEmpty()
            }
        }

        database.addValueEventListener(ordersListener as ValueEventListener)
    }

    private fun createProductFromSnapshot(orderData: DataSnapshot): product? {

        val productName = orderData.child("productName").getValue(String::class.java)
        val productPrice = orderData.child("productPrice").getValue(String::class.java)
        val productItemCount = orderData.child("productCount").getValue(Int::class.java)?.toString() ?: "0"
        val status = orderData.child("Status").getValue(String::class.java) ?: "Waiting"

        var productDate = orderData.child("productData").getValue(String::class.java)

        // Agar order me date missing hai, to current date Firebase me set kar do
        if (productDate.isNullOrEmpty()) {
            productDate = getCurrentDateTime()

            orderData.ref.child("productData")
                .setValue(productDate)
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Date set failed: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        val price = productPrice?.toDoubleOrNull() ?: 0.0
        val count = productItemCount.toDoubleOrNull() ?: 0.0
        val totalPrice = price * count

        return product(
            productTitle = productName ?: "No title",
            productPrice = totalPrice.toString(),
            itemCount = productItemCount,
            dateTime = productDate,
            Status = status
        )
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        )

        return dateFormat.format(Date())
    }

    private fun showLoading() {
        binding.recyclerOders.visibility = View.GONE
        binding.emptyOrderLayout.visibility = View.VISIBLE

        binding.admin25.visibility = View.VISIBLE
        binding.oderNo.visibility = View.VISIBLE
        binding.oderNo.text = "Loading orders..."
    }

    private fun showEmpty() {
        binding.recyclerOders.visibility = View.GONE
        binding.emptyOrderLayout.visibility = View.VISIBLE

        binding.admin25.visibility = View.VISIBLE
        binding.oderNo.visibility = View.VISIBLE
        binding.oderNo.text = "No orders found"
    }

    private fun showOrders() {
        binding.emptyOrderLayout.visibility = View.GONE
        binding.recyclerOders.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()

        ordersListener?.let {
            database.removeEventListener(it)
        }

        _binding = null
    }
}