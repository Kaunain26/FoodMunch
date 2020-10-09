package com.knesarCreation.foodmunch.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.adapter.ResMenuRecyclerAdapter
import com.knesarCreation.foodmunch.fragments.*
import com.knesarCreation.foodmunch.util.SessionManager


class DashboardActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    private lateinit var sessionManager: SessionManager
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        /*finding views by id*/
        toolbar = findViewById(R.id.dashboardToolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        /*Getting user data from shared Prefs*/
        sessionManager = SessionManager(this)
        sharedPreferences = sessionManager.getSharedPreferences

        /*setting up toolbar*/
        setSupportActionBar(toolbar)
        supportActionBar?.title = "All Restaurants"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /* Adding Hamburger icon to the toolbar */
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close,
        )
        toggle.syncState()  // syncing state of hamburger icon
        drawerLayout.addDrawerListener(toggle)

        /*display the AllRestaurantsFragment on activity by default*/
        openAllRestaurantsFragment()

        /* Setting click listener for menu items inside the navigation drawer*/
        navigationItemClickListener()

        /*Adding user details in nav header*/
        addingUserDetails()
    }

    @SuppressLint("SetTextI18n")
    private fun addingUserDetails() {
        val header = navigationView.getHeaderView(0)
        val username = header.findViewById<TextView>(R.id.txtUsername)
        val phoneNumber = header.findViewById<TextView>(R.id.txtUserPhoneNumber)

        username.text = sharedPreferences.getString("name", "N/A")
        phoneNumber.text = "+91-${sharedPreferences.getString("mobile_number", "N/A")}"
    }

    private fun navigationItemClickListener() {

        /* Here all clicks on navigation item will be handeled */
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.allRestaurants -> {
                    openAllRestaurantsFragment()
                }

                R.id.profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment()).commit()
                    supportActionBar?.title = "My Profile"
                }

                R.id.favourites -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FavRestaurantsFragment()).commit()
                    supportActionBar?.title = "Favourite Restaurants"
                }

                R.id.orderHistory -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, OrderHistoryFragment()).commit()
                    supportActionBar?.title = "Order History"
                }

                R.id.faq -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, FAQFragment()).commit()
                    supportActionBar?.title = "Frequently Asked Question"
                }

                R.id.logOut -> {
                    /*Logging out user session*/
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Confirmation")
                    builder.setMessage("Are you sure you want to log out!")
                    builder.setPositiveButton("OK") { _, _ ->
                        /* Clear all saved user data from shared prefs*/
                        sharedPreferences.edit().clear().apply()

                        startActivity(Intent(this, LoginActivity::class.java))
                        ActivityCompat.finishAffinity(this)
                        /**clear current Activity stack and launch a new Activity**/
                    }
                    builder.setNegativeButton("Cancel") { dialog, _ ->
                        openAllRestaurantsFragment()
                        dialog.dismiss()
                    }
                    builder.show()
                }
            }
            /*Closing Navigation drawer*/
            drawerLayout.closeDrawers()
            return@setNavigationItemSelectedListener true
        }

    }

    private fun openAllRestaurantsFragment() {
        val beginTransaction = supportFragmentManager.beginTransaction()
        val fragTransaction =
            beginTransaction.replace(R.id.fragment_container, AllRestaurantsFragment())
        fragTransaction.commit()
        supportActionBar?.title = "All Restaurants"
        navigationView.setCheckedItem(R.id.allRestaurants)
        toggle.isDrawerIndicatorEnabled = true
        toggle.syncState()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else {
            when (supportFragmentManager.findFragmentById(R.id.fragment_container)) {
                !is AllRestaurantsFragment -> {
                    /*Checking---  cart is empty or not
                    * -> if cart is not empty then it will ask a confirmation for reseting the CART items */

                    if (!ResMenuRecyclerAdapter.isCartEmpty) {
                        /*Confirmation Dialog*/
                        val dialog = AlertDialog.Builder(this)
                        dialog.setTitle("Confirmation")
                        dialog.setMessage("Going back will reset cart items. Do you still want to proceed")
                        dialog.setPositiveButton("OK") { _, _ ->

                            /*Deleting cart items*/
                            RestaurantMenuFragment.DBCartAsyncTask(
                                this,
                                RestaurantMenuFragment.restaurantId!!,
                                "null",
                                2
                            ).execute()
                            openAllRestaurantsFragment()
                            ResMenuRecyclerAdapter.isCartEmpty = true
                            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        }
                        dialog.setNegativeButton("Cancel") { dialog2, which ->
                            dialog2.dismiss()
                        }
                        dialog.show()
                    } else {

                        /*If cart is empty then it simply go back to AllRestaurantsFragment*/
                        openAllRestaurantsFragment()
                        /*setting LOCK_MODE_UNLOCKED of Drawer*/
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    }
                }
                else ->
                    super.onBackPressed()
            }
        }
    }
}