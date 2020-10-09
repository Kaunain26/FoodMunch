package com.knesarCreation.foodmunch.database

import androidx.room.*

@Dao
interface CartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrder(cartEntity: CartEntity)

    @Query("DELETE  FROM cart WHERE resId =:id ")
    fun deleteOrder(id: Int)

    @Query("SELECT * FROM cart")
    fun getAllItems(): List<CartEntity>

}