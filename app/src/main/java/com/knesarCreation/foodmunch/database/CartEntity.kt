package com.knesarCreation.foodmunch.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey val resId: Int,
    val foodItems: String
)