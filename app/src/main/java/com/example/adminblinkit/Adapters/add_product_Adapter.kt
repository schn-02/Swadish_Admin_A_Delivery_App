package com.example.adminblinkit.Adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.adminblinkit.Models.add_product_model
import com.example.adminblinkit.R

class add_product_Adapter(var list:ArrayList<add_product_model>, val context: Context, var selectedImages: MutableList<Uri>):
    RecyclerView.Adapter<add_product_Adapter.viewHolder>()
{
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): viewHolder
    {
        val view:View = LayoutInflater.from(context).inflate(R.layout.sample_add_product_layout, p0, false)
        return viewHolder(view)
    }

    override fun getItemCount(): Int
    {
        return list.size
    }


    override fun onBindViewHolder(p0: viewHolder, p1: Int)
    {
        val productModel = list[p1]
        p0.image.setImageURI(productModel.image)
        p0.imageRemove.setImageResource(productModel.imageRemove)

        p0.imageRemove.setOnClickListener{
            selectedImages.removeAt(p1)
            list.removeAt(p1)
            notifyItemRemoved(p1)
            notifyItemRangeChanged(p1, list.size)
            notifyItemRemoved(p1)
        }
    }

    class viewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val image:ImageView = itemView.findViewById(R.id.product)
        val imageRemove:ImageView = itemView.findViewById(R.id.Remove)


    }

}