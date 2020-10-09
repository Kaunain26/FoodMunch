package com.knesarCreation.foodmunch.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.knesarCreation.foodmunch.R

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val runnable = Runnable {
            val intent = Intent(this@SplashScreen, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        /*
        *  Handler() is deprecated
        *  So instead of that we will use , Handler(Looper.getMainLooper())
        */
        Handler(Looper.getMainLooper()).postDelayed(runnable, 1000)
    }
}