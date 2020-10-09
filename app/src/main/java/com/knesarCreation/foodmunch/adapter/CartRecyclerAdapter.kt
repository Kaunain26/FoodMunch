package com.knesarCreation.foodmunch.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.model.CartItems

class CartRecyclerAdapter(val context: Context, private val cartList: ArrayList<CartItems>) :
    RecyclerView.Adapter<CartRecyclerAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /*initializing view*/
        val txtFoodName: TextView = itemView.findViewById(R.id.txtFoodName)
        val txtFoodCost: TextView = itemView.findViewById(R.id.txtFoodPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        return CartViewHolder(
            LayoutInflater.from(context).inflate(R.layout.recycler_cart_single_row, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartList[position]
        holder.txtFoodName.text = cartItem.name
        holder.txtFoodCost.text = "Rs.${cartItem.costForOne}"
    }

    override fun getItemCount() = cartList.size
}