package com.knesarCreation.foodmunch.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    object PrefsConst {
        var PRIVATE_MODE = 0
        const val PREFS_NAME = "userRegister"
        const val IS_LOGGED_IN = "isLoggedIn"

    }
    var getSharedPreferences: SharedPreferences =
        context.getSharedPreferences(PrefsConst.PREFS_NAME, PrefsConst.PRIVATE_MODE)
    var editor: SharedPreferences.Editor = getSharedPreferences.edit()

    fun login(isLoggedIn: Boolean) {
        editor.putBoolean(PrefsConst.IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return getSharedPreferences.getBoolean(PrefsConst.IS_LOGGED_IN, false)
    }
}
