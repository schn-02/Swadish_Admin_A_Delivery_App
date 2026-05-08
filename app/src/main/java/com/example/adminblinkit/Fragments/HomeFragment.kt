package com.example.adminblinkit.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminblinkit.Adapters.ProductCategoryHomeFragmentAdapter
import com.example.adminblinkit.Adapters.item_View_recyclerView_Adapter
import com.example.adminblinkit.Models.ProductModelHomeFragment
import com.example.adminblinkit.Models.product
import com.example.adminblinkit.R
import com.example.adminblinkit.add_product_element
import com.example.adminblinkit.databinding.FragmentHomeBinding
import com.example.adminblinkit.databinding.SampleCustomAlertEditBinding
import com.example.adminblinkit.viewModels.AdminViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel = AdminViewModel()

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter2: item_View_recyclerView_Adapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        setupCategoryRecyclerView()
        setupProductRecyclerView()
        setupSearch()

        getAllTheProducts("All")

        return binding.root
    }

    private fun setupCategoryRecyclerView() {
        val categoryList = ArrayList<ProductModelHomeFragment>()

        categoryList.add(ProductModelHomeFragment(R.drawable.all, "All"))
        categoryList.add(ProductModelHomeFragment(R.drawable.masala, "Masala"))
        categoryList.add(ProductModelHomeFragment(R.drawable.atta_rice, "Atta Rice & Dal"))
        categoryList.add(ProductModelHomeFragment(R.drawable.cold_and_juices, "Cold Drink & Juices"))
        categoryList.add(ProductModelHomeFragment(R.drawable.dairy_breakfast, "Dairy and Breakfast"))
        categoryList.add(ProductModelHomeFragment(R.drawable.dry_masala, "Dry Masala"))
        categoryList.add(ProductModelHomeFragment(R.drawable.chicken_meat, "Chicken Meat & Fish"))
        categoryList.add(ProductModelHomeFragment(R.drawable.instant_frozen, "Instant & Frozen Foods"))
        categoryList.add(ProductModelHomeFragment(R.drawable.sangam_milk, "Sangam Milk"))
        categoryList.add(ProductModelHomeFragment(R.drawable.pharma_wellness, "Pharma & Wellness"))
        categoryList.add(ProductModelHomeFragment(R.drawable.sauce_spreads, "Sauces & Spreads"))
        categoryList.add(ProductModelHomeFragment(R.drawable.sweet_tooth, "Sweet Tooth"))
        categoryList.add(ProductModelHomeFragment(R.drawable.vegetable, "Vegetables & Fruits"))
        categoryList.add(ProductModelHomeFragment(R.drawable.tea, "Tea"))
        categoryList.add(ProductModelHomeFragment(R.drawable.tea_coffee, "Tea Coffee & Health Drinks"))
        categoryList.add(ProductModelHomeFragment(R.drawable.munchies, "Munchies"))
        categoryList.add(ProductModelHomeFragment(R.drawable.organic_premium, "Organic & Premium"))
        categoryList.add(ProductModelHomeFragment(R.drawable.pet_care, "Pet Care"))
        categoryList.add(ProductModelHomeFragment(R.drawable.baby, "Baby Care"))
        categoryList.add(ProductModelHomeFragment(R.drawable.bakery_biscuits, "Bakery & Biscuits"))
        categoryList.add(ProductModelHomeFragment(R.drawable.toned_milk, "Toned Milk"))

        val categoryAdapter = ProductCategoryHomeFragmentAdapter(
            categoryList,
            requireContext(),
            ::onClickedCategories
        )

        binding.recyclerHomeFragment.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            isNestedScrollingEnabled = false
        }
    }

    private fun setupProductRecyclerView() {
        adapter2 = item_View_recyclerView_Adapter(::onEditClicked)

        binding.recyclerHomeFragment2.apply {
            adapter = adapter2
            layoutManager = GridLayoutManager(requireContext(), 2)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupSearch() {
        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                text: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                val searchText = text.toString().trim()
                adapter2.filter.filter(searchText)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun onClickedCategories(category: ProductModelHomeFragment) {
        binding.search.setText("")
        getAllTheProducts(category.text)
    }

    private fun getAllTheProducts(text: String) {
        showLoading()

        lifecycleScope.launch {
            viewModel.fetchallTheProducts(text).collect { productList ->

                binding.shimmer.visibility = View.GONE

                adapter2.differ.submitList(productList)
                adapter2.original = ArrayList(productList)

                if (productList.isEmpty()) {
                    showEmpty()
                } else {
                    showProducts()
                }
            }
        }
    }

    private fun onEditClicked(product: product) {
        val editProduct = SampleCustomAlertEditBinding.inflate(
            LayoutInflater.from(requireContext())
        )

        editProduct.apply {
            productTitleedit.setText(product.productTitle)
            Quantityedit.setText(product.ProductQuantity)
            unitttedit.setText(product.ProductUnit)
            Ruppeesedit.setText(product.productPrice)
            stockedit.setText(product.productStock)
            productCategoryedit.setText(product.productCategory)
            productTypeeedit.setText(product.productType)

            productCategoryedit.isEnabled = false
            Quantityedit.isEnabled = false
            unitttedit.isEnabled = false
            Ruppeesedit.isEnabled = false
            stockedit.isEnabled = false
            productTypeeedit.isEnabled = false
            productTitleedit.isEnabled = false

            Edit.setOnClickListener {
                Toast.makeText(requireContext(), "Edit mode enabled", Toast.LENGTH_SHORT).show()

                productCategoryedit.isEnabled = true
                Quantityedit.isEnabled = true
                unitttedit.isEnabled = true
                Ruppeesedit.isEnabled = true
                stockedit.isEnabled = true
                productTypeeedit.isEnabled = true
                productTitleedit.isEnabled = true
            }

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

            unitttedit.setAdapter(unitAdapter)
            productTypeeedit.setAdapter(typeAdapter)
            productCategoryedit.setAdapter(categoryAdapter)

            val alertDialog = AlertDialog.Builder(requireContext())
                .setView(root)
                .create()

            alertDialog.show()

            val reference = database
                .getReference("Admin")
                .child("ProductsDetails")
                .child(product.productRandomId)

            save.setOnClickListener {
                val updates = hashMapOf<String, Any>(
                    "productCategory" to productCategoryedit.text.toString().trim(),
                    "productPrice" to Ruppeesedit.text.toString().trim(),
                    "productQuantity" to Quantityedit.text.toString().trim(),
                    "productStock" to stockedit.text.toString().trim(),
                    "productTitle" to productTitleedit.text.toString().trim(),
                    "productType" to productTypeeedit.text.toString().trim(),
                    "productUnit" to unitttedit.text.toString().trim()
                )

                reference.updateChildren(updates)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Product updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        alertDialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            requireContext(),
                            "Failed: ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun showLoading() {
        binding.shimmer.visibility = View.VISIBLE
        binding.recyclerHomeFragment2.visibility = View.GONE

        // Agar new XML use kiya hai jisme emptyStateCard hai
        binding.emptyStateCard.visibility = View.GONE
    }

    private fun showEmpty() {
        binding.shimmer.visibility = View.GONE
        binding.recyclerHomeFragment2.visibility = View.GONE

        // New premium empty state card
        binding.emptyStateCard.visibility = View.VISIBLE
    }

    private fun showProducts() {
        binding.shimmer.visibility = View.GONE
        binding.emptyStateCard.visibility = View.GONE
        binding.recyclerHomeFragment2.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}