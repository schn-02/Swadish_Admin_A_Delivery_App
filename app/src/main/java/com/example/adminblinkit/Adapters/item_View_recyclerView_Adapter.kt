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
import kotlin.reflect.KFunction1


class item_View_recyclerView_Adapter(val onEditClicked: (product) -> Unit) :RecyclerView.Adapter<item_View_recyclerView_Adapter.viewHolder>() , Filterable
{
       val auth = FirebaseAuth.getInstance()
    var original  = ArrayList<product>()
 val difutill =   object :DiffUtil.ItemCallback<product>()
 {

     override fun areItemsTheSame(oldItem: product, newItem: product): Boolean {
         return oldItem.productRandomId ==newItem.productRandomId
     }

     override fun areContentsTheSame(oldItem: product, newItem: product): Boolean {
return oldItem ==newItem
     }


 }
    val differ = AsyncListDiffer(this,difutill)

    class  viewHolder(val binding :SampleLayoutItemViewProductBinding):RecyclerView.ViewHolder(binding.root)
    {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder
    {
        return viewHolder(SampleLayoutItemViewProductBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int
    {
        return differ.currentList.size
    }



    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val product = differ.currentList[position]


        holder.binding.apply {
            val imageList = ArrayList<SlideModel>()
            

            // Loop through the product images and set scaleType
            product.productImageURI?.forEach { imageUrl ->
                // Set scaleType to FIT_CENTER or any other type
                val slideModel = SlideModel(imageUrl, ScaleTypes.FIT)
                imageList.add(slideModel)
            }

            // Set image list to ImageSlider if available, else show toast
            if (imageList.isNotEmpty()) {
                imageslider.setImageList(imageList)
            } else {
                Toast.makeText(holder.itemView.context, "No images available", Toast.LENGTH_SHORT).show()
            }
              val currentuser = auth.currentUser?.uid

            if (currentuser == product.adminUID) {
                holder.binding.veg.visibility = View.GONE
                holder.binding.showProductEdit.visibility = View.VISIBLE

                holder.binding.showProductEdit.setOnClickListener {
                    onEditClicked(product)
                }
            } else if (currentuser != product.adminUID) {
                holder.binding.veg.visibility = View.VISIBLE
                holder.binding.showProductEdit.visibility = View.GONE
            }

             holder.itemView.setOnClickListener{
                 Toast.makeText(holder.itemView.context , "LALAL" , Toast.LENGTH_SHORT).show()

             }

            // Set product details to views
            showProductName.text = product.productTitle
            showProductUnit.text = product.ProductQuantity +product.ProductUnit
            showProductPrice.text = "₹${product.productPrice}"
        }
    }
    
    private  val filter :FilterHome? =null

    override fun getFilter(): Filter {

        if (filter==null)
        {
            return FilterHome(this, original)
        }
        else{
            return filter
        }
    }


}