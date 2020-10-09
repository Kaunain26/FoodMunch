package com.knesarCreation.foodmunch.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.util.ConnectionManager
import com.knesarCreation.foodmunch.util.Validations
import org.json.JSONException
import org.json.JSONObject


class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var imgClose: ImageView
    private lateinit var etMobileNumber: EditText
    private lateinit var etEmailAddress: EditText
    private lateinit var btnNext: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        /*initializing Views*/
        initViews()

        /*setting toolbar*/
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""  // setting toolbar title null

        /*closing activity*/
        imgClose.setOnClickListener {
            super.onBackPressed()
        }

        btnNext.setOnClickListener {
            /*sending json post request*/
            sendingJsonRequest()
        }
    }

    private fun initViews() {
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etEmailAddress = findViewById(R.id.etEmailAddress)
        btnNext = findViewById(R.id.btnNext)
        toolbar = findViewById(R.id.customToolbar)
        imgClose = findViewById(R.id.imgClose)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun sendingJsonRequest() {
        val isValidated = validationUserInput()  // validating user input
        if (isValidated) {
            progressBar.visibility = View.VISIBLE
            btnNext.visibility = View.GONE

            val queue = Volley.newRequestQueue(this)
            val url = "http://13.235.250.119/v2/forgot_password/fetch_result"

            val jsonParams = JSONObject()
            jsonParams.put("mobile_number", etMobileNumber.text.toString())
            jsonParams.put("email", etEmailAddress.text.toString())

            /*Checking internet connection*/
            if (ConnectionManager().isNetworkAvailable(this)) {
                val jsonObjectRequest =
                    object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                        try {
                            val data = it.getJSONObject("data")
                            val success = data.getBoolean("success")

                            if (success) {
                                /* Hiding progress bar and making  visible next button*/
                                progressBar.visibility = View.GONE
                                btnNext.visibility = View.VISIBLE
                                alertDialog()

                            } else {
                                val error = data.getString("errorMessage")
                                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()

                                /* Hiding progress bar and making  visible next button*/
                                progressBar.visibility = View.GONE
                                btnNext.visibility = View.VISIBLE
                            }
                        } catch (e: JSONException) {
                            /*Catching Error if any JSON exception occurred*/

                            /* Hiding progress bar and making  visible next button*/
                            progressBar.visibility = View.GONE
                            btnNext.visibility = View.VISIBLE
                            Toast.makeText(this, "$e", Toast.LENGTH_SHORT).show()
                        }

                    }, Response.ErrorListener {
                        /*Catching Error if any VolleyError occurred*/

                        /* Hiding progress bar and making  visible next button*/
                        progressBar.visibility = View.GONE
                        btnNext.visibility = View.VISIBLE
                        Toast.makeText(
                            this,
                            "Some unexpected error occurred!!",
                            Toast.LENGTH_SHORT
                        ).show()
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
                /* Hiding progress bar and making  visible next button if there is no internet*/

                progressBar.visibility = View.GONE
                btnNext.visibility = View.VISIBLE
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validationUserInput(): Boolean {
        return if (Validations.validatePhoneNo(etMobileNumber.text.toString())) {
            etMobileNumber.error = null
            if (Validations.validateEmail(etEmailAddress.text.toString())) {
                etEmailAddress.error = null
                true
            } else {
                etEmailAddress.error = "Invalid Email"
                false
            }
        } else {
            etMobileNumber.error = "Invalid Mobile Number"
            false
        }
    }

    fun alertDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_otp_received, null)

        val dialogBtnOK = dialogLayout.findViewById<TextView>(R.id.btnOk)
        val txtCounter = dialogLayout.findViewById<TextView>(R.id.txtCounter)
        var counter = 5
        builder.setView(dialogLayout)

        /* Enabling counter in dialog*/
        object : CountDownTimer(6000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                txtCounter.text = counter.toString()
                counter--
            }

            override fun onFinish() {
                dialogBtnOK.visibility = View.VISIBLE
                txtCounter.visibility = View.GONE
            }
        }.start()

        dialogBtnOK.setOnClickListener {
            val intent = Intent(this, OTPActivity::class.java)
            intent.putExtra(
                "mobile_number",
                etMobileNumber.text.toString()
            )
            dialogBtnOK.visibility = View.GONE
            startActivity(intent)
            finish()
        }

        builder.setCancelable(false)
        builder.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            super.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}