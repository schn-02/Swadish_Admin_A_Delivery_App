package com.example.adminblinkit.Filter

import android.widget.Filter
import android.widget.Toast
import com.example.adminblinkit.Adapters.item_View_recyclerView_Adapter
import com.example.adminblinkit.Models.product
import java.util.Locale

class FilterHome(private val adapter:item_View_recyclerView_Adapter ,
    private  val filterProducts :ArrayList<product>):Filter()
{



    override fun performFiltering(searchingText: CharSequence?): FilterResults {

        val filterResults = FilterResults()
        if (!searchingText.isNullOrEmpty()) {
            val query = searchingText.toString().trim().uppercase(Locale.getDefault()).split(" ")
            val filterProductList = ArrayList<product>()
            for (products in filterProducts) {
                if (query.any { search ->
                        products.productTitle?.uppercase(Locale.getDefault())?.contains(search) == true ||
                                products.productCategory?.uppercase(Locale.getDefault())?.contains(search) == true ||
                                products.productType?.uppercase(Locale.getDefault())?.contains(search) == true||
                                products.productPrice?.uppercase(Locale.getDefault())?.contains(search) == true||
                                products.ProductUnit?.uppercase(Locale.getDefault())?.contains(search) == true

                    }) {
                    filterProductList.add(products)
                }
            }
            filterResults.apply {
                count = filterProductList.size
                values = filterProductList
            }
        } else {
            filterResults.apply {
                count = filterProducts.size
                values = filterProducts
            }
        }
        return filterResults
    }

    override fun publishResults(p0: CharSequence?, results: FilterResults?) {
        adapter.differ.submitList(results?.values as? ArrayList<product>)
    }


}