package com.knesarCreation.foodmunch.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.model.FoodItems
import com.knesarCreation.foodmunch.model.OrderHistory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OrderHistoryParentAdapter(
    val context: Context,
    private val orderHistoryList: ArrayList<OrderHistory>
) :
    RecyclerView.Adapter<OrderHistoryParentAdapter.OrderHistoryParentViewHolder>() {

    class OrderHistoryParentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        /*initializing view*/
        val txtResName: TextView = view.findViewById(R.id.txtResHistResName)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val recyclerResHistory: RecyclerView = view.findViewById(R.id.childOrderHistRecyclerview)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderHistoryParentViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.order_history_parent_custom_row, parent, false)
        return OrderHistoryParentViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderHistoryParentViewHolder, position: Int) {
        val orderHistoryObject = orderHistoryList[position]
        holder.txtResName.text = orderHistoryObject.resName
        holder.txtDate.text = formatDate(orderHistoryObject.orderDate)
        buildRecyclerView(holder.recyclerResHistory, orderHistoryObject)
    }

    override fun getItemCount(): Int {
        return orderHistoryList.size
    }

    private fun buildRecyclerView(
        recyclerResHistory: RecyclerView,
        orderHistoryList: OrderHistory
    ) {
        val foodItemsList = ArrayList<FoodItems>()
        for (i in 0 until orderHistoryList.foodItem.length()) {
            val foodJson = orderHistoryList.foodItem.getJSONObject(i)
            foodItemsList.add(
                FoodItems(
                    foodJson.getString("food_item_id"),
                    foodJson.getString("name"),
                    foodJson.getString("cost")
                )
            )
        }
        val cartItemAdapter = OrderHistoryChildAdapter(context, foodItemsList)
        val mLayoutManager = LinearLayoutManager(context)
        recyclerResHistory.layoutManager = mLayoutManager
        recyclerResHistory.itemAnimator = DefaultItemAnimator()
        recyclerResHistory.adapter = cartItemAdapter
    }


    private fun formatDate(dateTime: String): String? {
        val inputFormatter = SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.ENGLISH)
        val date: Date = inputFormatter.parse(dateTime) as Date


        val outputFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        return outputFormatter.format(date)
    }
}

