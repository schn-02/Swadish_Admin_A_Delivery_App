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
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminblinkit.Adapters.ProductCategoryHomeFragmentAdapter
import com.example.adminblinkit.Adapters.item_View_recyclerView_Adapter
import com.example.adminblinkit.Models.ProductModelHomeFragment
import com.example.adminblinkit.Models.product
import com.example.adminblinkit.R
import com.example.adminblinkit.add_product_element
import com.example.adminblinkit.viewModels.AdminViewModel
import com.example.adminblinkit.databinding.FragmentHomeBinding
import com.example.adminblinkit.databinding.SampleCustomAlertEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch


class HomeFragment : Fragment()
{


    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private val viewModel = AdminViewModel()
    val products = ArrayList<product>()
    lateinit var database: FirebaseDatabase
    lateinit var auth: FirebaseAuth
    lateinit var adapter2: item_View_recyclerView_Adapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        val recylerView = binding.recyclerHomeFragment
        val list = ArrayList<ProductModelHomeFragment>()
        list.add(ProductModelHomeFragment(R.drawable.all, "All"))
        list.add(ProductModelHomeFragment(R.drawable.masala, "Masala"))
        list.add(ProductModelHomeFragment(R.drawable.atta_rice, "Atta Rice & Dal"))
        list.add(ProductModelHomeFragment(R.drawable.cold_and_juices, "Cold Drink & Juices"))
        list.add(ProductModelHomeFragment(R.drawable.dairy_breakfast, "Dairy and Breakfast"))
        list.add(ProductModelHomeFragment(R.drawable.dry_masala, "Dry Masala"))
        list.add(ProductModelHomeFragment(R.drawable.chicken_meat, "Chicken Meat & Fish"))
        list.add(ProductModelHomeFragment(R.drawable.instant_frozen, "Instant & Frozen Foods"))
        list.add(ProductModelHomeFragment(R.drawable.sangam_milk, "sangam Milk"))
        list.add(ProductModelHomeFragment(R.drawable.pharma_wellness, "Pharma & Wellness"))
        list.add(ProductModelHomeFragment(R.drawable.sauce_spreads, "Sauces & Spreads"))
        list.add(ProductModelHomeFragment(R.drawable.sweet_tooth, "Sweet Tooth"))
        list.add(ProductModelHomeFragment(R.drawable.vegetable, "vegetables & Fruits"))
        list.add(ProductModelHomeFragment(R.drawable.tea, "Tea"))
        list.add(ProductModelHomeFragment(R.drawable.tea_coffee, "Tea Coffee & Health Drinks"))
        list.add(ProductModelHomeFragment(R.drawable.munchies, "Munchies"))
        list.add(ProductModelHomeFragment(R.drawable.organic_premium, "Organic & Premimum "))
        list.add(ProductModelHomeFragment(R.drawable.pet_care, "Pet Care"))
        list.add(ProductModelHomeFragment(R.drawable.baby, "Baby Care"))
        list.add(ProductModelHomeFragment(R.drawable.bakery_biscuits, "Bakery & Biscuits"))
        list.add(ProductModelHomeFragment(R.drawable.toned_milk, "Toned Milk"))

        val adapter = ProductCategoryHomeFragmentAdapter(list, requireContext() ,::onClickedCategories)

        recylerView.adapter = adapter
        val lm = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recylerView.layoutManager = lm

        binding.search.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
         }

            override fun onTextChanged(Text: CharSequence?, p1: Int, p2: Int, p3: Int)
            {
                 val searchText = Text.toString().trim()
                adapter2.filter.filter(searchText)

            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        getAllTheProducts("All")

        return binding.root

    }



    private fun onEditClicked(product: product) {

        val editProduct = SampleCustomAlertEditBinding.inflate(LayoutInflater.from(requireContext()))
        editProduct.apply {
            // Initial values set karna
            productTitleedit.setText(product.productTitle)
            Quantityedit.setText(product.ProductQuantity)
            unitttedit.setText(product.ProductUnit)
            Ruppeesedit.setText(product.productPrice)
            stockedit.setText(product.productStock)
            productCategoryedit.setText(product.productCategory)
            productTypeeedit.setText(product.productType)

            // Edit button pe click karne se fields enable ho jayengi
            editProduct.Edit.setOnClickListener {
                Toast.makeText(requireContext(), "Edit Button Clicked", Toast.LENGTH_SHORT).show()
                productCategoryedit.isEnabled = true
                Quantityedit.isEnabled = true
                unitttedit.isEnabled = true
                Ruppeesedit.isEnabled = true
                stockedit.isEnabled = true
                productTypeeedit.isEnabled = true
                productTitleedit.isEnabled = true
            }

            val unit = ArrayAdapter(requireContext(), R.layout.show_list, add_product_element.allUnitsProduct)
            val type = ArrayAdapter(requireContext(), R.layout.show_list, add_product_element.allProductTypes)
            val category = ArrayAdapter(requireContext(), R.layout.show_list, add_product_element.allProductCategory)

            editProduct.unitttedit.setAdapter(unit)
            editProduct.productTypeeedit.setAdapter(type)
            editProduct.productCategoryedit.setAdapter(category)

            val alertDialog = AlertDialog.Builder(requireContext())
                .setView(editProduct.root)
                .create()
            alertDialog.show()
            // Firebase reference define karna
            val reference = database.getReference("Admin").child("ProductsDetails").child(product.productRandomId)

            // Save button pe click karne ke baad updates map prepare karna aur Firebase me update karna
            save.setOnClickListener {
                val updates = hashMapOf<String, Any>(
                    "productCategory" to productCategoryedit.text.toString(),
                    "productPrice" to Ruppeesedit.text.toString(),
                    "productQuantity" to Quantityedit.text.toString(),
                    "productStock" to stockedit.text.toString(),
                    "productTitle" to productTitleedit.text.toString(),
                    "productType" to productTypeeedit.text.toString(),
                    "productUnit" to unitttedit.text.toString()
                )

                // Firebase me data update karna
                reference.updateChildren(updates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Data Successfully Edited", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Data Failed to Edit", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // AlertDialog create aur show karna

        }
    }


    private  fun onClickedCategories(categories : ProductModelHomeFragment)
     {
        getAllTheProducts(categories.text)
     }
    private fun getAllTheProducts(text: String)
    {
        binding.shimmer.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.fetchallTheProducts(text).collect{

                if (it.isEmpty())
                {
                    binding.recyclerHomeFragment2.visibility = View.GONE
//                    binding.textNoProduct.visibility = View.VISIBLE
                    binding.homelottie.visibility = View.VISIBLE
                }
                else
                {
                    binding.recyclerHomeFragment2.visibility = View.VISIBLE
//                    binding.textNoProduct.visibility = View.GONE
                    binding.homelottie.visibility = View.GONE

                }




                 adapter2 = item_View_recyclerView_Adapter(::onEditClicked)

                binding.recyclerHomeFragment2.adapter= adapter2
                binding.recyclerHomeFragment2.layoutManager = GridLayoutManager(context, 2)
                adapter2.differ.submitList(it)
                binding.shimmer.visibility = View.GONE
                adapter2.original = it as ArrayList<product>




            }
        }

    }

}