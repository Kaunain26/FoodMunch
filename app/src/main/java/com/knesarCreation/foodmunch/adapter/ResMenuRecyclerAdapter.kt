package com.knesarCreation.foodmunch.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.model.RestaurantMenu

class ResMenuRecyclerAdapter(
    val context: Context,
    private val menuList: ArrayList<RestaurantMenu>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<ResMenuRecyclerAdapter.RestaurantsDetailsViewHolder>() {

    companion object {
        var isCartEmpty = true
    }

    interface OnItemClickListener {
        fun onAddItemClick(orderItems: RestaurantMenu)
        fun onRemoveItemClick(orderItems: RestaurantMenu)
    }

    class RestaurantsDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /*initializing views*/
        val txtSrNo: TextView = itemView.findViewById(R.id.txtSrNo)
        val txtFoodName: TextView = itemView.findViewById(R.id.txtFoodName)
        val txtFoodPrice: TextView = itemView.findViewById(R.id.txtFoodPrice)
        val btnAddFood: Button = itemView.findViewById(R.id.btnAddFood)
        val btnRemoveFood: Button = itemView.findViewById(R.id.btnRemoveFood)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RestaurantsDetailsViewHolder {
        return RestaurantsDetailsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_restaurants_menu_single_row, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RestaurantsDetailsViewHolder, position: Int) {
        val currentItem = menuList[position]
        holder.txtSrNo.text = (position + 1).toString()
        holder.txtFoodName.text = currentItem.name
        holder.txtFoodPrice.text = "Rs.${currentItem.costForOne}"
        holder.btnAddFood.setOnClickListener {
            holder.btnRemoveFood.visibility = View.VISIBLE
            holder.btnAddFood.visibility = View.GONE
            listener.onAddItemClick(currentItem)
        }

        holder.btnRemoveFood.setOnClickListener {
            holder.btnAddFood.visibility = View.VISIBLE
            holder.btnRemoveFood.visibility = View.GONE
            listener.onRemoveItemClick(currentItem)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount() = menuList.size
}
