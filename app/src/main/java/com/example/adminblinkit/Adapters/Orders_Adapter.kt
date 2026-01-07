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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Orders_Adapter(val context: Context, val list: ArrayList<product>) : RecyclerView.Adapter<Orders_Adapter.ViewHolder>() {
    private lateinit var database: DatabaseReference
    var status = "Waiting"


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.oders_sample_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]

        holder.Date.text = data.dateTime ?: "No date available"
        holder.Title.text = data.productTitle ?: "No title"
        holder.price.text = data.productPrice ?: "No price"

        holder.count.text = data.itemCount
        status = data.Status.toString()

        holder.itemView.setOnClickListener {
            showAddressDialog(data , position)
        }


        if (status == "Accept")
        {
              holder.statuAccept.text ="Accept"
            holder.statuAccept.setTextColor(Color.BLUE)

        }
        if (status == "Reject")
        {
              holder.statuAccept.text ="Reject"
            holder.statuAccept.setTextColor(Color.RED)

        }
        if (status == "Delieverd")
        {
              holder.statuAccept.text ="Delieverd"
            holder.statuAccept.setTextColor(Color.GREEN)

        }

    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val Date: TextView = itemView.findViewById(R.id.Date)
        val Title: TextView = itemView.findViewById(R.id.ProductTitle)
        val price: TextView = itemView.findViewById(R.id.price)
        val count:TextView =itemView.findViewById(R.id.count1)
        val statuAccept:TextView =itemView.findViewById(R.id.status2)


    }

    private fun showAddressDialog(data: product  , position: Int) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        database = FirebaseDatabase.getInstance().reference.child("All_users").child("users")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(context, "No orders found!", Toast.LENGTH_SHORT).show()
                    return
                }

                // Layout ko inflate karte hain
                val view = LayoutInflater.from(context).inflate(R.layout.show_address_layout, null)
                val addressView: TextView = view.findViewById(R.id.showAddress)
                val phoneView: TextView = view.findViewById(R.id.showNumber)
                var accept:Button = view.findViewById(R.id.Accept)
                var reject:Button = view.findViewById(R.id.Reject)
                var deleverd:Button = view.findViewById(R.id.Deliverd)




                if (data.Status=="Delieverd" || data.Status =="Reject")
                {
                    accept.visibility = View.GONE
                    reject.visibility = View.GONE
                    deleverd.visibility = View.GONE
                }



                val dialog = AlertDialog.Builder(context)
                    .setView(view)
                    .setCancelable(true) // Allow dismiss on outside touch
                    .create()

                if (data.Status =="Waiting")
                {
                    deleverd.setOnClickListener{
                      dialog.dismiss()
                        Toast.makeText(context, "First you should accept the projuct before Delieverd" , Toast.LENGTH_SHORT).show()
                    }
                }

                if (data.Status =="Delieverd" || data.Status =="Reject")
                {
                    accept.visibility = View.GONE
                    reject.visibility = View.GONE
                    deleverd.visibility = View.GONE
                }
                if (data.Status =="Waiting")
                {
                    accept.setOnClickListener {
                        changeStatus(position , 0)
                        accept.visibility = View.GONE
                        dialog.dismiss()
                    }
                    reject.setOnClickListener {
                        changeStatus(position , 1)
                        reject.visibility = View.GONE
                        dialog.dismiss()
                    }
                }
                else if(data.Status!="Waiting" &&data.Status=="Accept")
                {
                    deleverd.setOnClickListener {
                        changeStatus(position, 0)

                        dialog.dismiss()

                    }
                    accept.visibility = View.GONE
                    reject.visibility = View.GONE

                }



                // Snapshot se saare users ka data iterate karte hain
                for (userSnapshot in snapshot.children) {
                    val userOrderAddress = userSnapshot.child("UserAddress")

                    if (userOrderAddress.exists()) {
                        val address = userOrderAddress.child("address1").getValue(String::class.java) ?: "No address"
                        val district = userOrderAddress.child("district1").getValue(String::class.java) ?: "No district"
                        val phone = userOrderAddress.child("phoneno1").getValue(String::class.java) ?: "No phone"
                        val pincode = userOrderAddress.child("pincode1").getValue(String::class.java) ?: "No pincode"
                        val state = userOrderAddress.child("state1").getValue(String::class.java) ?: "No state"

                        // Values ko set karte hain
                        addressView.text = "$address, $district, $pincode, $state"
                        phoneView.text = phone

                        dialog.show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to fetch address: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun changeStatus(position: Int , a:Int) {
        val db = FirebaseDatabase.getInstance().getReference("All_users").child("users")
           var intialPositon  = 0
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(context, "No orders found!", Toast.LENGTH_SHORT).show()
                    return
                }

                for (userSnapshot in snapshot.children) {
                    val userUID = userSnapshot.key
                    val userOrdersRef = userSnapshot.child("userODERS")

                    for (orderSnapshot in userOrdersRef.children) {
                        val statusRef = orderSnapshot.child("Status")
                        val currentStatus = statusRef.getValue(String::class.java) ?: "Waiting"

                        val adminUID = FirebaseAuth.getInstance().currentUser?.uid
                        val adminUIDFromDatabase = orderSnapshot.child("AdminUID").getValue(String::class.java)

                        if (adminUIDFromDatabase == adminUID) {
                            Toast.makeText(context , "CLick $intialPositon" , Toast.LENGTH_SHORT).show()

                            if (intialPositon == position) {

                                if (currentStatus == "Waiting") {

                                    val orderKey = orderSnapshot.key
                                    val specificOrderRef =
                                        db.child(userUID!!).child("userODERS").child(orderKey!!)

                                    val updates = mapOf("Status" to "Accept")

                                    specificOrderRef.updateChildren(updates)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(
                                                    context,
                                                    "Status updated successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                notifyDataSetChanged()
                                                notifyItemChanged(position)


                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to update status: ${task.exception?.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                }


                                if (currentStatus == "Accept") {
                                    val orderKey = orderSnapshot.key
                                    val specificOrderRef =
                                        db.child(userUID!!).child("userODERS").child(orderKey!!)

                                    val updates = mapOf("Status" to "Delieverd")

                                    specificOrderRef.updateChildren(updates)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(
                                                    context,
                                                    "Status updated successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                notifyDataSetChanged()
                                                notifyItemChanged(position)



                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to update status: ${task.exception?.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                }
                                if (currentStatus == "Waiting" &&a==1) {
                                    val orderKey = orderSnapshot.key
                                    val specificOrderRef =
                                        db.child(userUID!!).child("userODERS").child(orderKey!!)

                                    val updates = mapOf("Status" to "Reject")

                                    specificOrderRef.updateChildren(updates)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(
                                                    context,
                                                    "Status updated successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                notifyDataSetChanged()
                                                notifyItemChanged(position)


                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to update status: ${task.exception?.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                }
                            }
                        }
                        intialPositon++
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
