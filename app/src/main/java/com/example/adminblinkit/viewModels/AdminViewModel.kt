package com.example.adminblinkit.viewModels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.adminblinkit.Models.ProductModelHomeFragment
import com.example.adminblinkit.Models.product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class AdminViewModel:ViewModel()
{


    fun fetchallTheProducts(text: String):Flow<List<product>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admin").child("ProductsDetails")
        val eventListener = object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<product>()
                for (productSnapshot in snapshot.children) {
                    val prod = productSnapshot.getValue(product::class.java)
                    if (text == "All" || prod?.productCategory==text)
                    {
                        products.add(prod!!)
                    }
                    // Check if productImageURI exists and is a nested structure
                    val imageList = mutableListOf<String>()
                    val imagesSnapshot = productSnapshot.child("Admin_Product_Images")
                    for (image in imagesSnapshot.children) {
                        image.getValue(String::class.java)?.let { imageUrl ->
                            if(text=="All" ||prod?.productCategory==text)
                            {
                            imageList.add(imageUrl)
                                }
                        }
                    }
                    prod?.productImageURI = imageList // Set images in productImageURI list




                }

                    trySend(products)


            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        db.addValueEventListener(eventListener)
        awaitClose({ db.removeEventListener(eventListener) })
    }
}