package com.example.adminblinkit.Adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.adminblinkit.Models.product
import com.example.adminblinkit.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Orders_Adapter(
    private val context: Context,
    private val list: ArrayList<product>
) : RecyclerView.Adapter<Orders_Adapter.ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    private val database = FirebaseDatabase.getInstance()
        .reference
        .child("All_users")
        .child("users")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.oders_sample_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        holder.date.text = if (data.dateTime.isNullOrEmpty()) {
            "No date available"
        } else {
            data.dateTime
        }

        holder.title.text = if (data.productTitle.isNullOrEmpty()) {
            "No title"
        } else {
            data.productTitle
        }

        holder.price.text = formatPrice(data.productPrice)

        holder.count.text = if (data.itemCount.isNullOrEmpty()) {
            "0"
        } else {
            data.itemCount
        }

        setStatusUi(holder.status, data.Status)

        holder.itemView.setOnClickListener {
            val currentPosition = holder.adapterPosition

            if (currentPosition != RecyclerView.NO_POSITION) {
                showAddressDialog(list[currentPosition], currentPosition)
            }
        }
    }

    private fun formatPrice(price: String?): String {
        if (price.isNullOrEmpty()) return "₹0"

        return if (price.startsWith("₹")) {
            price
        } else {
            "₹$price"
        }
    }

    private fun setStatusUi(statusView: TextView, status: String?) {
        when (status) {
            "Accept" -> {
                statusView.text = "Accepted"
                statusView.setTextColor(Color.WHITE)
                statusView.setBackgroundResource(R.drawable.bg_status_accept)
            }

            "Reject" -> {
                statusView.text = "Rejected"
                statusView.setTextColor(Color.WHITE)
                statusView.setBackgroundResource(R.drawable.bg_status_reject)
            }

            "Delieverd" -> {
                statusView.text = "Delivered"
                statusView.setTextColor(Color.WHITE)
                statusView.setBackgroundResource(R.drawable.bg_status_delivered)
            }

            else -> {
                statusView.text = "Waiting"
                statusView.setTextColor(Color.WHITE)
                statusView.setBackgroundResource(R.drawable.bg_status_chip)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.Date)
        val title: TextView = itemView.findViewById(R.id.ProductTitle)
        val price: TextView = itemView.findViewById(R.id.price)
        val count: TextView = itemView.findViewById(R.id.count1)
        val status: TextView = itemView.findViewById(R.id.status2)
    }

    private fun showAddressDialog(data: product, position: Int) {
        if (position == RecyclerView.NO_POSITION) return

        val adminUid = auth.currentUser?.uid

        if (adminUid == null) {
            Toast.makeText(context, "Admin not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(context, "No users found!", Toast.LENGTH_SHORT).show()
                    return
                }

                val view = LayoutInflater.from(context)
                    .inflate(R.layout.show_address_layout, null)

                val addressView: TextView = view.findViewById(R.id.showAddress)
                val phoneView: TextView = view.findViewById(R.id.showNumber)

                val acceptBtn: Button = view.findViewById(R.id.Accept)
                val rejectBtn: Button = view.findViewById(R.id.Reject)
                val deliveredBtn: Button = view.findViewById(R.id.Deliverd)

                val dialog = AlertDialog.Builder(context)
                    .setView(view)
                    .setCancelable(true)
                    .create()

                setupButtons(
                    status = data.Status ?: "Waiting",
                    acceptBtn = acceptBtn,
                    rejectBtn = rejectBtn,
                    deliveredBtn = deliveredBtn,
                    position = position,
                    dialog = dialog
                )

                var addressFound = false

                for (userSnapshot in snapshot.children) {

                    val userOrdersSnapshot = userSnapshot.child("userODERS")

                    for (orderSnapshot in userOrdersSnapshot.children) {

                        val adminUidFromDb = orderSnapshot.child("AdminUID")
                            .getValue(String::class.java)

                        if (adminUidFromDb == adminUid) {
                            val userAddress = userSnapshot.child("UserAddress")

                            if (userAddress.exists()) {
                                val address = userAddress.child("address1")
                                    .getValue(String::class.java) ?: "No address"

                                val district = userAddress.child("district1")
                                    .getValue(String::class.java) ?: "No district"

                                val phone = userAddress.child("phoneno1")
                                    .getValue(String::class.java) ?: "No phone"

                                val pincode = userAddress.child("pincode1")
                                    .getValue(String::class.java) ?: "No pincode"

                                val state = userAddress.child("state1")
                                    .getValue(String::class.java) ?: "No state"

                                addressView.text = "$address, $district, $pincode, $state"
                                phoneView.text = phone

                                addressFound = true
                                break
                            }
                        }
                    }

                    if (addressFound) break
                }

                if (addressFound) {
                    dialog.show()
                } else {
                    Toast.makeText(context, "Address not found!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Failed to fetch address: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupButtons(
        status: String,
        acceptBtn: Button,
        rejectBtn: Button,
        deliveredBtn: Button,
        position: Int,
        dialog: AlertDialog
    ) {
        when (status) {
            "Waiting" -> {
                acceptBtn.visibility = View.VISIBLE
                rejectBtn.visibility = View.VISIBLE
                deliveredBtn.visibility = View.VISIBLE

                acceptBtn.setOnClickListener {
                    changeStatus(position, "Accept")
                    dialog.dismiss()
                }

                rejectBtn.setOnClickListener {
                    changeStatus(position, "Reject")
                    dialog.dismiss()
                }

                deliveredBtn.setOnClickListener {
                    Toast.makeText(
                        context,
                        "First accept the product before delivery",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            "Accept" -> {
                acceptBtn.visibility = View.GONE
                rejectBtn.visibility = View.GONE
                deliveredBtn.visibility = View.VISIBLE

                deliveredBtn.setOnClickListener {
                    changeStatus(position, "Delieverd")
                    dialog.dismiss()
                }
            }

            "Reject", "Delieverd" -> {
                acceptBtn.visibility = View.GONE
                rejectBtn.visibility = View.GONE
                deliveredBtn.visibility = View.GONE
            }

            else -> {
                acceptBtn.visibility = View.GONE
                rejectBtn.visibility = View.GONE
                deliveredBtn.visibility = View.GONE
            }
        }
    }

    private fun changeStatus(position: Int, newStatus: String) {
        if (position == RecyclerView.NO_POSITION) return

        val adminUid = auth.currentUser?.uid

        if (adminUid == null) {
            Toast.makeText(context, "Admin not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        var matchedPosition = 0

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(context, "No orders found!", Toast.LENGTH_SHORT).show()
                    return
                }

                for (userSnapshot in snapshot.children) {
                    val userUid = userSnapshot.key ?: continue
                    val userOrdersSnapshot = userSnapshot.child("userODERS")

                    for (orderSnapshot in userOrdersSnapshot.children) {
                        val adminUidFromDb = orderSnapshot.child("AdminUID")
                            .getValue(String::class.java)

                        if (adminUidFromDb == adminUid) {
                            if (matchedPosition == position) {
                                val orderKey = orderSnapshot.key ?: return

                                val specificOrderRef = database
                                    .child(userUid)
                                    .child("userODERS")
                                    .child(orderKey)

                                val updates = mapOf(
                                    "Status" to newStatus
                                )

                                specificOrderRef.updateChildren(updates)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Status updated successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { error ->
                                        Toast.makeText(
                                            context,
                                            "Failed: ${error.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                return
                            }

                            matchedPosition++
                        }
                    }
                }

                Toast.makeText(context, "Order not found!", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Database Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}