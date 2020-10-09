package com.knesarCreation.foodmunch.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.util.SessionManager
import kotlinx.android.synthetic.main.activity_dashboard.*

class ProfileFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var txtUsername: TextView
    lateinit var txtUserMobileNo: TextView
    lateinit var txtUserEmailAddress: TextView
    lateinit var txtUserAddress: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        setHasOptionsMenu(true)

        /*Initializing Views*/
        txtUsername = view.findViewById(R.id.txtUsername)
        txtUserMobileNo = view.findViewById(R.id.txtMobileNo)
        txtUserEmailAddress = view.findViewById(R.id.txtUserEmail)
        txtUserAddress = view.findViewById(R.id.txtDeliveryAddress)

        /*Getting user data from shared Prefs*/
        sessionManager = SessionManager(activity as Context)
        sharedPreferences = sessionManager.getSharedPreferences

        txtUsername.text = sharedPreferences.getString("name", "N/A")
        txtUserEmailAddress.text = sharedPreferences.getString("email", "N/A")
        txtUserMobileNo.text = "+91-${sharedPreferences.getString("mobile_number", "N/A")}"
        txtUserAddress.text = sharedPreferences.getString("address", "N/A")

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            (activity as AppCompatActivity).drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }
}