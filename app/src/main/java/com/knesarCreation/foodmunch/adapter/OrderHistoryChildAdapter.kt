package com.knesarCreation.foodmunch.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.model.FoodItems

class OrderHistoryChildAdapter(
    val context: Context,
    private val foodItemList: ArrayList<FoodItems>
) :
    RecyclerView.Adapter<OrderHistoryChildAdapter.OrderHistoryChildViewHolder>() {

    class OrderHistoryChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /*initializing view*/
        val txtFoodName: TextView = itemView.findViewById(R.id.txtOrderHistFoodName)
        val txtFoodPrice: TextView = itemView.findViewById(R.id.txtOrderHistFoodPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryChildViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.order_history_child_custom_view, parent, false)
        return OrderHistoryChildViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OrderHistoryChildViewHolder, position: Int) {
        val orderHistFoodItems = foodItemList[position]
        holder.txtFoodName.text = orderHistFoodItems.name
        holder.txtFoodPrice.text = "Rs.${orderHistFoodItems.cost}"
    }

    override fun getItemCount() = foodItemList.size
}