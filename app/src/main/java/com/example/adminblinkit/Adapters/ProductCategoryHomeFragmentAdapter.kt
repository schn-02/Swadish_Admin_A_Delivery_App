package com.example.adminblinkit.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.adminblinkit.Models.ProductModelHomeFragment
import com.example.adminblinkit.R
import kotlin.reflect.KFunction

class ProductCategoryHomeFragmentAdapter(
    var list: ArrayList<ProductModelHomeFragment>,
    val context: Context,
    val onClickedCategories: (ProductModelHomeFragment) -> Unit
):
    RecyclerView.Adapter<ProductCategoryHomeFragmentAdapter.viewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder
    {
        val view:View = LayoutInflater.from(context).inflate(R.layout.sample_layout_item_product_home,parent,false)
        return viewHolder(view)
    }

    override fun getItemCount(): Int
    {
        return list.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int)
    {
        val product =list[position]
        holder.image.setImageResource(product.image)
        holder.text.setText(product.text)

        holder.image.setOnClickListener {
            Toast.makeText(context, "${product.text}" , Toast.LENGTH_SHORT).show()
            onClickedCategories(product)
        }

    }
    class viewHolder(itemView:View): RecyclerView.ViewHolder(itemView)
    {
        var image:ImageView= itemView.findViewById(R.id.imageView3)

        var text:TextView = itemView.findViewById(R.id.textView11)
    }
}