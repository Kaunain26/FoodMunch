package com.knesarCreation.foodmunch.util

import android.util.Patterns

object Validations {
    fun validatePhoneNo(phoneNo: String): Boolean {
        return phoneNo.length == 10
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 6
    }

    fun validateName(name: String): Boolean {
        return name.length >= 3
    }

    fun matchPassword(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    fun validateEmail(email: String): Boolean {
        return (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches())
    }

    fun validateOtp(otp: String): Boolean {
        return otp.length == 4
    }
}