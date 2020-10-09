package com.knesarCreation.foodmunch.model

import org.json.JSONArray

data class OrderHistory(
    val orderId: Int,
    val resName: String,
    val totalCost: String,
    val orderDate: String,
    val foodItem: JSONArray
)