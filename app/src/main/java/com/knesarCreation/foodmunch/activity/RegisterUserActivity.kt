package com.knesarCreation.foodmunch.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
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

class RegisterUserActivity : AppCompatActivity() {

    private lateinit var imgClose: ImageView
    lateinit var etUsername: EditText
    lateinit var etEmailAddress: EditText
    private lateinit var etMobileNo: EditText
    lateinit var etDeliveryAddress: EditText
    lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    lateinit var btnRegister: Button
    lateinit var sessionManager: SessionManager
    lateinit var sharedPreferences: SharedPreferences
    lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)

        /*initializing views*/
        initViews()

        /*closing register activity*/
        imgClose.setOnClickListener {
            super.onBackPressed()
        }

        /* Initializing session manager for saving user's data in SHARED PREFs */
        sessionManager = SessionManager(this)
        sharedPreferences = sessionManager.getSharedPreferences

        btnRegister.setOnClickListener {
            /*sending a post request to the server*/
            sendJsonPostRequest()
        }
    }

    private fun initViews() {
        imgClose = findViewById(R.id.imgClose)
        etUsername = findViewById(R.id.etUsername)
        etEmailAddress = findViewById(R.id.etEmailAddress)
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress)
        etMobileNo = findViewById(R.id.etMobileNumber)
        etPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun validationUserInput(): Boolean {

        return if (Validations.validateName(etUsername.text.toString())) {
            etUsername.error = null

            if (Validations.validateEmail(etEmailAddress.text.toString())) {
                etEmailAddress.error = null

                if (Validations.validatePhoneNo(etMobileNo.text.toString())) {
                    etMobileNo.error = null

                    if (Validations.validatePassword(etPassword.text.toString())) {
                        etPassword.error = null

                        if (Validations.matchPassword(
                                etPassword.text.toString(),
                                etConfirmPassword.text.toString()
                            )
                        ) {
                            etPassword.error = null
                            etConfirmPassword.error = null
                            true  /* Return true if validation is successful*/

                        } else {
                            etPassword.error = "Password didn't match"
                            etConfirmPassword.error = "Password didn't match"
                            Toast.makeText(this, "Password didn't match", Toast.LENGTH_SHORT)
                                .show()
                            false  /* Return true if validation is unsuccessful*/
                        }

                    } else {
                        etPassword.error = "Password length should be greater or equal to 6"
                        Toast.makeText(
                            this,
                            "Password length should be greater or equal to 6",
                            Toast.LENGTH_SHORT
                        ).show()
                        false /* Return true if validation is unsuccessful*/
                    }
                } else {
                    etMobileNo.error = "Invalid mobile number"
                    Toast.makeText(this, "Invalid mobile number", Toast.LENGTH_SHORT).show()
                    false /* Return true if validation is unsuccessful*/
                }
            } else {
                etEmailAddress.error = "Invalid email address"
                Toast.makeText(this, "Invalid Email address", Toast.LENGTH_SHORT).show()
                false /* Return true if validation is unsuccessful*/
            }
        } else {
            etUsername.error = "Invalid name"
            Toast.makeText(this, "Invalid name", Toast.LENGTH_SHORT)
                .show()
            false /* Return true if validation is unsuccessful*/
        }
    }

    private fun sendJsonPostRequest() {

        val isValidated = validationUserInput()  /* Validating user input*/
        if (isValidated) {
            /* Hiding register button and making progress bar visible
            *  Its indicating that processes are in progress*/
            progressBar.visibility = View.VISIBLE
            btnRegister.visibility = View.GONE

            if (etDeliveryAddress.text.toString().isNotBlank()) {

                // Sending a Post request to Register user
                val queue = Volley.newRequestQueue(this@RegisterUserActivity)

                val jsonParams = JSONObject()
                jsonParams.put("name", etUsername.text.toString())
                jsonParams.put("mobile_number", etMobileNo.text.toString())
                jsonParams.put("password", etPassword.text.toString())
                jsonParams.put("address", etDeliveryAddress.text.toString())
                jsonParams.put("email", etEmailAddress.text.toString())

                /* Checking internet connection for sending json request*/
                if (ConnectionManager().isNetworkAvailable(this@RegisterUserActivity)) {
                    val jsonRequest =
                        object : JsonObjectRequest(
                            Method.POST,
                            "http://13.235.250.119/v2/register/fetch_result",
                            jsonParams,
                            Response.Listener {
                                try {
                                    val data = it.getJSONObject("data")
                                    val success = data.getBoolean("success")

                                    if (success) {
                                        /*if success is true we are getting user data from server*/
                                        val jsonObject = data.getJSONObject("data")

                                        /*saving users details in Shared prefs for further use*/
                                        sharedPreferences.edit()
                                            .putString("userId", jsonObject.getString("user_id"))
                                            .apply()

                                        sharedPreferences.edit()
                                            .putString("name", jsonObject.getString("name")).apply()

                                        sharedPreferences.edit()
                                            .putString("email", jsonObject.getString("email"))
                                            .apply()

                                        sharedPreferences.edit()
                                            .putString(
                                                "mobile_number",
                                                jsonObject.getString("mobile_number")
                                            )
                                            .apply()

                                        sharedPreferences.edit()
                                            .putString("address", jsonObject.getString("address"))
                                            .apply()

                                        sessionManager.login(true)

                                        startActivity(Intent(this, DashboardActivity::class.java))
                                        ActivityCompat.finishAffinity(this)

                                    } else {
                                        /* Hiding progress bar and making register button visible*/
                                        progressBar.visibility = View.GONE
                                        btnRegister.visibility = View.VISIBLE

                                        /*if success is false we display error message*/
                                        val errorMessage = data.getString("errorMessage")
                                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                                    }

                                } catch (e: JSONException) {
                                    /*Catching Error if any JSON exception occurred*/

                                    /* Hiding progress bar and making  register button visible*/
                                    progressBar.visibility = View.GONE
                                    btnRegister.visibility = View.VISIBLE

                                    Toast.makeText(this, "$e", Toast.LENGTH_LONG).show()
                                }
                            },
                            Response.ErrorListener {
                                /*Catching Error if any VolleyError occurred*/

                                /* Hiding progress bar and making register button visible*/
                                progressBar.visibility = View.GONE
                                btnRegister.visibility = View.VISIBLE

                                Toast.makeText(this, "VolleyError: $it", Toast.LENGTH_LONG).show()
                            }) {
                            override fun getHeaders(): MutableMap<String, String> {
                                val headers = HashMap<String, String>()
                                headers["Content-type"] = "application/json"
                                headers["token"] = "c28e4d98687310"
                                return headers
                            }
                        }

                    queue.add(jsonRequest)

                } else {
                    /* Hiding progress bar and making register button visible*/
                    progressBar.visibility = View.GONE
                    btnRegister.visibility = View.VISIBLE

                    Toast.makeText(
                        this,
                        "Please fill address",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {

                /* Hiding progress bar and making  visible register button  if there is no internet connection*/

                progressBar.visibility = View.GONE
                btnRegister.visibility = View.VISIBLE

                /*Alerting user that there is no internet connection enabled*/
                val builder = AlertDialog.Builder(this)
                builder.setTitle("No Connection!!")
                builder.setMessage("Internet connection is not enabled. Please connect to the internet")
                builder.setPositiveButton("OK") { dialog, which ->
                    val openSetting =
                        /* using explicit intent for opening setting */
                        Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
                    startActivity(openSetting)

                }
                builder.show()
            }
        }
    }
}