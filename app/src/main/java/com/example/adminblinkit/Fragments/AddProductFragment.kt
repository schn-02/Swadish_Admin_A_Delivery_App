package com.example.adminblinkit.Fragments

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminblinkit.Adapters.add_product_Adapter
import com.example.adminblinkit.Models.add_product_model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.adminblinkit.Models.product
import com.example.adminblinkit.R
import com.example.adminblinkit.add_product_element
import com.example.adminblinkit.databinding.FragmentAddProductBinding
import com.google.firebase.FirebaseApp
import java.util.UUID

class AddProductFragment : Fragment() {

    private val selectedImages = mutableListOf<Uri>()
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private  var CHANNEL_ID ="channelID"
    private lateinit var storageRef: FirebaseStorage
    private lateinit var   productRandomUID : String

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!
    private var clipData1: ClipData? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()
        FirebaseApp.initializeApp(requireContext())

        NotificationChannel()

        binding.imageButton.setOnClickListener {
            openGallery()
        }

        showListInText()

        binding.unittt.setOnClickListener {
            binding.unittt.showDropDown()
        }
        binding.productTypee.setOnClickListener {
            binding.productTypee.showDropDown()
        }
        binding.productCategory.setOnClickListener {
            binding.productCategory.showDropDown()
        }

        binding.AddProduct.setOnClickListener {
            binding.admin23.visibility = View.VISIBLE

            val title = binding.productTitle.text.toString()
            val unit = binding.unittt.text.toString()
            val stock = binding.stock.text.toString()
            val rupees = binding.Ruppees.text.toString()
            val productType = binding.productTypee.text.toString()
            val productCategory = binding.productCategory.text.toString()
            val quantity = binding.Quantity.text.toString()

            if (title.isEmpty()||unit.isEmpty()||stock.isEmpty()||rupees.isEmpty()||productCategory.isEmpty()||
                productCategory.isEmpty()||quantity.isEmpty())
            {
                Toast.makeText(context , "Please Fill All Details " , Toast.LENGTH_LONG).show()
                binding.admin23.visibility = View.GONE
                return@setOnClickListener
            }

            productRandomUID = generateRandomString(8)

            val adminUID = FirebaseAuth.getInstance().currentUser?.uid

            if ((clipData1?.itemCount ?: 0) > 0) {
                // Product details initialization
                val product = product(
                    productRandomId = productRandomUID.toString(),
                    productType = productType,
                    productCategory = productCategory,
                    productStock = stock,
                    productTitle = title,
                    productPrice = rupees,
                    ProductQuantity = quantity,
                    ProductUnit = unit,
                    adminUID = adminUID
                )

                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    // Saving product details to Firebase
                    database.reference.child("Admin").child("ProductsDetails")
                        .child(productRandomUID.toString()).setValue(product)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    requireContext(),
                                    "Data Added Successfully..",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Call to upload images after adding product
                                uploadSelectedImages()
                            }
                        }.addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed...", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                // Handle case when no images are selected
                Toast.makeText(requireContext(), "Please Select At least 1 image", Toast.LENGTH_SHORT).show()
                // Optionally, make the view visible again if needed
                binding.admin23.visibility = View.GONE
                return@setOnClickListener
            }




        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun generateRandomString(length: Int): String {
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')  // Uppercase, lowercase aur numbers
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"
        imagePickerlauncher.launch(intent)
    }

    private fun showListInText() {
        val unit = ArrayAdapter(requireContext(), R.layout.show_list, add_product_element.allUnitsProduct)
        val type = ArrayAdapter(requireContext(), R.layout.show_list, add_product_element.allProductTypes)
        val category = ArrayAdapter(requireContext(), R.layout.show_list, add_product_element.allProductCategory)

        binding.unittt.setAdapter(unit)
        binding.productTypee.setAdapter(type)
        binding.productCategory.setAdapter(category)
    }

    private val imagePickerlauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val list = ArrayList<add_product_model>()
            selectedImages.clear()
            it.data?.clipData?.let { clipData ->

                this.clipData1 = clipData

                if (clipData.itemCount == 0) {
                    Toast.makeText(context, "Please select at least one image", Toast.LENGTH_LONG).show()
                    return@let
                }
                if (clipData.itemCount > 4) {
                    add_product_element.showToastWithDelay(requireContext(), "You can select up to 4 images", 1000)
                    return@let
                }
                for (i in 0 until clipData.itemCount) {
                    val imageUri = clipData.getItemAt(i).uri
                    selectedImages.add(imageUri)
                    list.add(add_product_model(imageUri, R.drawable.remove))
                }
                val adapter = add_product_Adapter(list, requireContext(), selectedImages)
                binding.productRecycler.adapter = adapter
                binding.productRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            }
        }
    }
 private  var uploadImages =0
    private fun uploadSelectedImages() {
        if (selectedImages.isEmpty()) {
            add_product_element.showToastWithDelay(requireContext(), "No Image is selected", 1000)
            binding.admin23.visibility = View.GONE

        }
        uploadImages =0
        for (imageUri in selectedImages) {
            uploadImagesToFirebase(imageUri)
        }
    }

    private fun  uploadImagesToFirebase(imageUri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val imageRef = storageRef.reference.child("Admin").child(
            productRandomUID.toString()
        ).child("$uid/Admin_Product_Images/${System.currentTimeMillis()}.jpg")
        imageRef.putFile(imageUri).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener {
                saveImageIntoFirebase(it.toString())
            }
        }.addOnFailureListener {
            add_product_element.showToastWithDelay(requireContext(), "Please Try Again.....", 1000)
            binding.admin23.visibility = View.GONE

        }
    }

    private fun saveImageIntoFirebase(imageUrl: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val databaseRef = database.reference.child("Admin")
            .child("ProductsDetails")
            .child(productRandomUID.toString())
            .child("Admin_Product_Images")

        val imageID = databaseRef.push().key

        imageID?.let {
            databaseRef.child(it).setValue(imageUrl).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    uploadImages++

                    if (uploadImages == selectedImages.size) {
                        add_product_element.showToastWithDelay(
                            requireActivity(),
                            "All Images Uploaded Successfully",
                            1000
                        )
                        binding.admin23.visibility = View.GONE
                            NotificationForPost()

                        // Navigate back to HomeFragment
                        val homeFragment = HomeFragment()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, homeFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                } else {
                    add_product_element.showToastWithDelay(
                        requireContext(),
                        "Please Try Again",
                        1000
                    )
                    binding.admin23.visibility = View.GONE

                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT)
                    .show()
                binding.admin23.visibility = View.GONE

            }
        }
    }
    private  fun NotificationForPost()
    {
        val builder = NotificationCompat.Builder(requireContext() , CHANNEL_ID)
            .setSmallIcon(R.drawable.slack)
            .setContentTitle("Admin Swadish")
            .setContentText(" Now, Food is live ")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
             with(NotificationManagerCompat.from(requireContext()))
             {
                 if (ActivityCompat.checkSelfPermission(
                         requireContext(),
                         Manifest.permission.POST_NOTIFICATIONS
                     ) != PackageManager.PERMISSION_GRANTED
                 ) {

                     return
                 }
                 notify(1  , builder.build())
             }
    }

    private  fun NotificationChannel()
    {
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O)
        {
            val channel = NotificationChannel(CHANNEL_ID , "Admin Swadish" , NotificationManager.IMPORTANCE_DEFAULT)
            channel.description ="Food"
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)        }
    }

}
