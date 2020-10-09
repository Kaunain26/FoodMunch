package com.knesarCreation.foodmunch.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val rating: String,
    val costForOne: String,
    val imageUrl: String
)