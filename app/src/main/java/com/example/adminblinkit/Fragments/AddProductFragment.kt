package com.example.adminblinkit.Fragments

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminblinkit.Adapters.add_product_Adapter
import com.example.adminblinkit.Models.add_product_model
import com.example.adminblinkit.Models.product
import com.example.adminblinkit.R
import com.example.adminblinkit.add_product_element
import com.example.adminblinkit.databinding.FragmentAddProductBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val selectedImages = mutableListOf<Uri>()

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: FirebaseStorage

    private val CHANNEL_ID = "channelID"
    private lateinit var productRandomUID: String

    private var uploadImages = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddProductBinding.inflate(inflater, container, false)

        FirebaseApp.initializeApp(requireContext())

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()

        createNotificationChannel()
        showListInText()
        setupClickListeners()

        return binding.root
    }

    private fun setupClickListeners() {

        binding.imageButton.setOnClickListener {
            openGallery()
        }

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
            addProduct()
        }
    }

    private fun addProduct() {

        val title = binding.productTitle.text.toString().trim()
        val unit = binding.unittt.text.toString().trim()
        val stock = binding.stock.text.toString().trim()
        val rupees = binding.Ruppees.text.toString().trim()
        val productType = binding.productTypee.text.toString().trim()
        val productCategory = binding.productCategory.text.toString().trim()
        val quantity = binding.Quantity.text.toString().trim()

        if (
            title.isEmpty() ||
            unit.isEmpty() ||
            stock.isEmpty() ||
            rupees.isEmpty() ||
            productType.isEmpty() ||
            productCategory.isEmpty() ||
            quantity.isEmpty()
        ) {
            Toast.makeText(requireContext(), "Please fill all details", Toast.LENGTH_LONG).show()
            return
        }

        if (selectedImages.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least 1 image", Toast.LENGTH_SHORT).show()
            return
        }

        val adminUID = auth.currentUser?.uid

        if (adminUID == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        showLoader()

        productRandomUID = database.reference
            .child("Admin")
            .child("ProductsDetails")
            .push()
            .key ?: generateRandomString(8)

        val productData = product(
            productRandomId = productRandomUID,
            productType = productType,
            productCategory = productCategory,
            productStock = stock,
            productTitle = title,
            productPrice = rupees,
            ProductQuantity = quantity,
            ProductUnit = unit,
            adminUID = adminUID
        )

        val productRef = database.reference
            .child("Admin")
            .child("ProductsDetails")
            .child(productRandomUID)

        productRef.setValue(productData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Product data saved", Toast.LENGTH_SHORT).show()
                uploadSelectedImages()
            }
            .addOnFailureListener { error ->
                hideLoader()
                Toast.makeText(
                    requireContext(),
                    "Database failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
    private fun showLoader() {
        binding.loaderOverlay.visibility = View.VISIBLE
        binding.admin23.visibility = View.VISIBLE
        binding.admin23.playAnimation()
        binding.AddProduct.isEnabled = false
    }

    private fun hideLoader() {
        if (_binding == null) return

        binding.admin23.cancelAnimation()
        binding.admin23.visibility = View.GONE
        binding.loaderOverlay.visibility = View.GONE
        binding.AddProduct.isEnabled = true
    }

    private fun generateRandomString(length: Int): String {
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun showListInText() {
        val unitAdapter = ArrayAdapter(
            requireContext(),
            R.layout.show_list,
            add_product_element.allUnitsProduct
        )

        val typeAdapter = ArrayAdapter(
            requireContext(),
            R.layout.show_list,
            add_product_element.allProductTypes
        )

        val categoryAdapter = ArrayAdapter(
            requireContext(),
            R.layout.show_list,
            add_product_element.allProductCategory
        )

        binding.unittt.setAdapter(unitAdapter)
        binding.productTypee.setAdapter(typeAdapter)
        binding.productCategory.setAdapter(categoryAdapter)
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

            val data = result.data ?: return@registerForActivityResult

            val list = ArrayList<add_product_model>()
            selectedImages.clear()

            val clipData = data.clipData

            if (clipData != null) {
                if (clipData.itemCount > 4) {
                    add_product_element.showToastWithDelay(
                        requireContext(),
                        "You can select up to 4 images",
                        1000
                    )
                    return@registerForActivityResult
                }

                for (i in 0 until clipData.itemCount) {
                    val imageUri = clipData.getItemAt(i).uri
                    selectedImages.add(imageUri)
                    list.add(add_product_model(imageUri, R.drawable.remove))
                }
            } else {
                val singleImageUri = data.data

                if (singleImageUri != null) {
                    selectedImages.add(singleImageUri)
                    list.add(add_product_model(singleImageUri, R.drawable.remove))
                }
            }

            if (selectedImages.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one image", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            val adapter = add_product_Adapter(list, requireContext(), selectedImages)

            binding.productRecycler.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            binding.productRecycler.adapter = adapter
        }

    private fun uploadSelectedImages() {
        if (selectedImages.isEmpty()) {
            hideLoader()
            add_product_element.showToastWithDelay(requireContext(), "No image selected", 1000)
            return
        }

        uploadImages = 0

        for (imageUri in selectedImages) {
            uploadImagesToFirebase(imageUri)
        }
    }

    private fun uploadImagesToFirebase(imageUri: Uri) {

        val uid = auth.currentUser?.uid

        if (uid == null) {
            hideLoader()
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val imageRef = storageRef.reference
            .child("Admin")
            .child(productRandomUID)
            .child("$uid/Admin_Product_Images/${System.currentTimeMillis()}.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        saveImageIntoFirebase(downloadUri.toString())
                    }
                    .addOnFailureListener { error ->
                        hideLoader()
                        Toast.makeText(
                            requireContext(),
                            "Failed to get image URL: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { error ->
                hideLoader()
                Toast.makeText(
                    requireContext(),
                    "Image upload failed: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun saveImageIntoFirebase(imageUrl: String) {

        val databaseRef = database.reference
            .child("Admin")
            .child("ProductsDetails")
            .child(productRandomUID)
            .child("Admin_Product_Images")

        val imageID = databaseRef.push().key

        if (imageID == null) {
            hideLoader()
            Toast.makeText(requireContext(), "Image id not created", Toast.LENGTH_SHORT).show()
            return
        }

        databaseRef.child(imageID)
            .setValue(imageUrl)
            .addOnSuccessListener {
                uploadImages++

                if (uploadImages == selectedImages.size) {
                    hideLoader()

                    add_product_element.showToastWithDelay(
                        requireActivity(),
                        "Product added successfully",
                        1000
                    )

                    notificationForPost()

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
            .addOnFailureListener { error ->
                hideLoader()
                Toast.makeText(
                    requireContext(),
                    "Failed to save image URL: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun notificationForPost() {

        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.letters)
            .setContentTitle("Admin Swadish")
            .setContentText("Now, Food is live")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(requireContext())) {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            notify(1, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Admin Swadish",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.description = "Food"

            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}