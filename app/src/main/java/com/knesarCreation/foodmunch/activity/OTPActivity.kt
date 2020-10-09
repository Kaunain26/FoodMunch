package com.knesarCreation.foodmunch.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.util.ConnectionManager
import com.knesarCreation.foodmunch.util.SessionManager
import com.knesarCreation.foodmunch.util.Validations
import org.json.JSONException
import org.json.JSONObject

class OTPActivity : AppCompatActivity() {

    private lateinit var btnVerify: Button
    private lateinit var etOtp: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var imgClose: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var sessionManager: SessionManager
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_o_t_p)

        /*Initializing views*/
        initView()

        /* Initializing session manager for saving user's data in SHARED PREFs */
        sessionManager = SessionManager(this)
        sharedPreferences = sessionManager.getSharedPreferences

        imgClose.setOnClickListener {
            super.onBackPressed()
        }

        btnVerify.setOnClickListener {
            /*sending json post request*/
            sendingJsonRequest()
        }

    }

    private fun initView() {
        btnVerify = findViewById(R.id.btnVerify)
        etOtp = findViewById(R.id.etOtp)
        etNewPassword = findViewById(R.id.etNewPassword)
        imgClose = findViewById(R.id.imgClose)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun sendingJsonRequest() {
        val isValidated = validationUserInput()  // validating user input
        if (isValidated) {
            /* Hiding verify button and making progress bar visible
              *Its indicating that processes are in progress*/
            progressBar.visibility = View.VISIBLE
            btnVerify.visibility = View.GONE

            /* Checking internet connection for sending json request*/
            if (ConnectionManager().isNetworkAvailable(this)) {
                val queue = Volley.newRequestQueue(this)
                val url = "http://13.235.250.119/v2/reset_password/fetch_result"

                /*getting mobile no from intent*/
                val mobileNumber = intent?.getStringExtra("mobile_number")

                val jsonParams = JSONObject()
                jsonParams.put("mobile_number", mobileNumber)
                jsonParams.put("password", etConfirmPassword.text.toString())
                jsonParams.put("otp", etOtp.text.toString())

                val jsonObjectRequest =
                    object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {

                        try {
                            val data = it.getJSONObject("data")
                            val success = data.getBoolean("success")
                            Log.d("TAG", "sendingJsonRequest:$it ")
                            if (success) {
                                /* If Success is TRUE Hiding progress bar and making verify button visible*/
                                progressBar.visibility = View.GONE
                                btnVerify.visibility = View.VISIBLE

                                /*Successfully  password changed AlertDialog*/
                                val builder = AlertDialog.Builder(this)
                                builder.setTitle("Success!!")
                                builder.setMessage("Your password is successfully changed")
                                builder.setIcon(R.drawable.ic_success)
                                builder.setPositiveButton("Got it!") { dialog, which ->
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    ActivityCompat.finishAffinity(this)
                                }
                                builder.setCancelable(false)
                                builder.show()
                            } else {
                                /* Hiding progress bar and making verify button visible*/
                                progressBar.visibility = View.GONE
                                btnVerify.visibility = View.VISIBLE
                                Toast.makeText(
                                    this,
                                    data.getString("errorMessage"),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        } catch (e: JSONException) {
                            /*Catching Error if any JSON exception occurred*/

                            /* Hiding progress bar and making verify button visible*/
                            progressBar.visibility = View.GONE
                            btnVerify.visibility = View.VISIBLE
                            Toast.makeText(this, "$e", Toast.LENGTH_SHORT).show()
                        }

                    }, Response.ErrorListener {
                        /*Catching Error if any VolleyError occurred*/

                        /* Hiding progress bar and making verify button visible*/
                        progressBar.visibility = View.GONE
                        btnVerify.visibility = View.VISIBLE
                        Toast.makeText(this, "Some unexpected error occurred", Toast.LENGTH_SHORT)
                            .show()
                    }) {
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            headers["Content-type"] = "application/json"
                            headers["token"] = "c28e4d98687310"
                            return headers
                        }
                    }
                queue.add(jsonObjectRequest)
            } else {
                /* Hiding progress bar and making verify button visible, if there is no internet connection*/
                progressBar.visibility = View.GONE
                btnVerify.visibility = View.VISIBLE
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*validating user input*/
    private fun validationUserInput(): Boolean {
        return if (Validations.validateOtp(etOtp.text.toString())) {
            etOtp.error = null

            if (Validations.validatePassword(etNewPassword.text.toString())) {
                etNewPassword.error = null

                if (Validations.matchPassword(
                        etNewPassword.text.toString(),
                        etConfirmPassword.text.toString()
                    )
                ) {
                    etConfirmPassword.error = null
                    true  /* Return true if validation is successful*/
                } else {
                    Toast.makeText(this, "Password did't match", Toast.LENGTH_SHORT).show()
                    etNewPassword.error = "Password did't match"
                    etConfirmPassword.error = "Password did't match"
                    false /* Return false if validation is successful*/
                }
            } else {
                etNewPassword.error = "Invalid Password"
                false  /* Return false if validation is successful*/
            }
        } else {
            etOtp.error = "Invalid OTP"
            false   /* Return true if validation is successful*/
        }
    }
}