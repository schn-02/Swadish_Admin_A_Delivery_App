package com.example.adminblinkit.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.adminblinkit.Filter.FilterHome
import com.example.adminblinkit.Models.product
import com.example.adminblinkit.databinding.SampleLayoutItemViewProductBinding
import com.google.firebase.auth.FirebaseAuth

class item_View_recyclerView_Adapter(
    private val onEditClicked: (product) -> Unit
) : RecyclerView.Adapter<item_View_recyclerView_Adapter.ProductViewHolder>(), Filterable {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var original = ArrayList<product>()

    private val diffUtil = object : DiffUtil.ItemCallback<product>() {

        override fun areItemsTheSame(oldItem: product, newItem: product): Boolean {
            return oldItem.productRandomId == newItem.productRandomId
        }

        override fun areContentsTheSame(oldItem: product, newItem: product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    inner class ProductViewHolder(
        val binding: SampleLayoutItemViewProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: product) {
            binding.apply {

                showProductName.text = item.productTitle ?: "No Title"

                showProductUnit.text = buildString {
                    append(item.ProductQuantity ?: "")
                    append(" ")
                    append(item.ProductUnit ?: "")
                }.trim()

                showProductPrice.text = "₹${item.productPrice ?: "0"}"

                setProductImages(item)

                val currentUserId = auth.currentUser?.uid
                val isMyProduct = currentUserId == item.adminUID

                showProductEdit.visibility = if (isMyProduct) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                showProductEdit.setOnClickListener {
                    onEditClicked(item)
                }

                root.setOnClickListener {
                    Toast.makeText(
                        root.context,
                        item.productTitle ?: "Product clicked",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        private fun setProductImages(item: product) {
            val imageList = ArrayList<SlideModel>()

            item.productImageURI?.forEach { imageUrl ->
                if (!imageUrl.isNullOrBlank()) {
                    imageList.add(
                        SlideModel(
                            imageUrl,
                            ScaleTypes.CENTER_CROP
                        )
                    )
                }
            }

            if (imageList.isNotEmpty()) {
                binding.imageslider.setImageList(imageList)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = SampleLayoutItemViewProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getFilter(): Filter {
        return FilterHome(this, original)
    }
}