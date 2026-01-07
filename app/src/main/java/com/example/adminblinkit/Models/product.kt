package com.example.adminblinkit.Models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
data class product(
    var productRandomId: String = UUID.randomUUID().toString(),
    var productTitle: String? = null,
    var ProductQuantity: String? = null,
    var ProductUnit: String? = null,
    var productPrice: String? = null,
    var Pincode: String? = null,
    var State: String? = null,
    var Address: String? = null,
    var City: String? = null,
    var MobileNo: String? = null,
    var Status: String? ="Waiting",
    var productStock: String? = null,
    var productCategory: String? = null,
    var productType: String? = null,
    var dateTime: String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()),  // Set current date here
    var itemCount: String? = null,
    var adminUID: String? = null,
    var productImageURI: MutableList<String>? = mutableListOf() // List to store multiple image URLs
)
