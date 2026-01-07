package com.example.adminblinkit.Models

import android.net.Uri

data class Item_Model
(
    val productName: String,
    val productUnit: String,
    val productPrice: String,
    val prroductImages: ArrayList<Uri>
)