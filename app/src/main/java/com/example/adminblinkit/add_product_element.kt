package com.example.adminblinkit

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.adminblinkit.Fragments.AddProductFragment
import com.example.adminblinkit.Models.ProductModelHomeFragment

object add_product_element
{
    var allProductCategory = arrayOf(
        "All",
        "Vegetables & Fruits",
        "Dairy and Breakfast" ,
        "Munchies",
        "Cold Drink & Juices",
        "Instant & Frozen Foods",
        "Tea Coffee & Health Drinks",
        "Bakery & Biscuits",
        "Sweet Tooth",
        "Atta Rice & Dal",
//        "Dry Fruits Masala & Oil",
        "Sauces & Spreads",
        "Chicken Meat & Fish",
//        "Pan Corner",
        "Organic & Premimum",
        "Tea",
        "Baby Care",
        "Pharma & Wellness",
        "Cleaning Essential",
        "Toned Milk",
//        "Home & Office",
//        "Personal Care",
        "Pet Care"
    )
    var allUnitsProduct = arrayOf(
        "Kg",
        "gm",
        "ml","Ltr","Packets"
        ,"Pieces"
    )

    var allProductTypes = arrayOf(
        "Milk ,Curd & Paneer",
        "Vegetables",
        "Chis & Crips"
        ,"Fruits",
        "Salt & Sugar"
        ,"Noodles",
        "Cold Drink & Juices",
        "Cooking Oil",
        "Biscuits",
        "Eggs",
        "Chocklates",
        "Bread & Butter",
        "Namkeen",
        "Atta & Rice",
        "Ice Cream",
        "Cake",
        "Ghee",
        "Water",
        "Cookies",
        "Maida & Sooji"
    )

    fun showToastWithDelay(context: Context, message: String, delayMillis: Long)
    {
        Handler(Looper.getMainLooper()).postDelayed(
            {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }, delayMillis)
    }
}

