package com.knesarCreation.foodmunch.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class ConnectionManager {

    fun isNetworkAvailable(context: Context): Boolean {
        val connectionManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo: NetworkInfo? = connectionManager.activeNetworkInfo
        return activeNetworkInfo != null
    }
}