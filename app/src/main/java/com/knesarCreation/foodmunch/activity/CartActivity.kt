package com.knesarCreation.foodmunch.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.adapter.CartRecyclerAdapter
import com.knesarCreation.foodmunch.adapter.ResMenuRecyclerAdapter
import com.knesarCreation.foodmunch.database.CartEntity
import com.knesarCreation.foodmunch.database.Database
import com.knesarCreation.foodmunch.model.CartItems
import com.knesarCreation.foodmunch.util.ConnectionManager
import com.knesarCreation.foodmunch.util.SessionManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class CartActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var recyclerCart: RecyclerView
    lateinit var cartRecyclerAdapter: CartRecyclerAdapter
    private lateinit var txtResName: TextView
    lateinit var btnPlaceOrder: Button
    lateinit var sharedPreferences: SharedPreferences
    lateinit var sessionManager: SessionManager
    lateinit var rlLoading: RelativeLayout
    private var resId: Int? = 0
    private var resName: String? = ""
    var totalCost = 0
    var orderList = arrayListOf<CartItems>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        /*Finding view by id*/
        initViews()

        /* Initializing session manager for saving user's data in SHARED PREFs */
        sessionManager = SessionManager(this)
        sharedPreferences = sessionManager.getSharedPreferences

        /*getting intent value*/
        resId = intent?.getIntExtra("res_id", 0)
        resName = intent?.getStringExtra("res_name")
        txtResName.text = resName

        /*Setting up toolbar*/
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*Getting data from database and adding into a list*/
        setUpCart()
        /*Placing order*/
        placeOrder()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.cartToolbar)
        txtResName = findViewById(R.id.txtResName)
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)
        recyclerCart = findViewById(R.id.cartRecycler)
        rlLoading = findViewById(R.id.rlLoading)
    }

    private fun placeOrder() {
        for (i in 0 until orderList.size) {
            totalCost += orderList[i].costForOne.toInt()
            Log.d("tag", "$orderList")
        }
        btnPlaceOrder.text = "Place Order(Total: Rs.$totalCost)"
        btnPlaceOrder.setOnClickListener {
            sendJsonRequest()  /* sending json post request*/
        }
    }

    private fun setUpCart() {
        val cartList = GetOrderListFromDBAsync(applicationContext).execute().get()

        for (order in cartList) {
            orderList.addAll(
                Gson().fromJson(order.foodItems, Array<CartItems>::class.java).asList()
            )
        }
        val linearLayout = LinearLayoutManager(this)
        recyclerCart.layoutManager = linearLayout
        cartRecyclerAdapter = CartRecyclerAdapter(this, orderList)
        recyclerCart.adapter = cartRecyclerAdapter
    }

    private fun sendJsonRequest() {
        /* Hiding btnPlaceOrder  and making visible rlLoading
            * Its indicating that processes are going on*/
        rlLoading.visibility = View.VISIBLE
        btnPlaceOrder.visibility = View.GONE

        val queue = Volley.newRequestQueue(this)
        val url = "http://13.235.250.119/v2/place_order/fetch_result/"

        val jsonParams = JSONObject()
        jsonParams.put("user_id", sharedPreferences.getString("userId", null))
        jsonParams.put("restaurant_id", resId.toString())
        jsonParams.put("total_cost", totalCost.toString())

        val foodArray = JSONArray()
        for (i in 0 until orderList.size) {
            val foodId = JSONObject()
            foodId.put("food_item_id", orderList[i].id)
            foodArray.put(i, foodId)
        }
        jsonParams.put("food", foodArray)

        /* Checking internet connection*/
        if (ConnectionManager().isNetworkAvailable(this)) {

            val jsonObjectRequest =
                object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")

                        /*if success is TRUE then clear the cart*/
                        if (success) {
                            val isClear = ClearCart(this, resId!!).execute().get()
                            ResMenuRecyclerAdapter.isCartEmpty = true

                            if (isClear) {
                                val dialog = Dialog(
                                    this,
                                    android.R.style.Theme_Black_NoTitleBar_Fullscreen
                                )
                                dialog.setContentView(R.layout.dilaog_order_confirmation)
                                dialog.show()
                                dialog.setCancelable(false)
                                val btnOk = dialog.findViewById<Button>(R.id.btnOk)
                                btnOk.setOnClickListener {
                                    dialog.dismiss()
                                    rlLoading.visibility = View.VISIBLE
                                    btnPlaceOrder.visibility = View.GONE

                                    /*After a successful order open a Dashboard Activity*/
                                    startActivity(
                                        Intent(
                                            this,
                                            DashboardActivity::class.java
                                        )
                                    )
                                    ActivityCompat.finishAffinity(this)
                                }
                            }
                        } else {
                            /*if success is FALSE then show some error msg*/
                            rlLoading.visibility = View.GONE
                            btnPlaceOrder.visibility = View.VISIBLE
                            val error = data.getString("errorMessage")
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: JSONException) {
                        /*Catching Error if any JSON exception occurred*/

                        /* Hiding rlLoading  and making btnPlaceOrder visible*/
                        rlLoading.visibility = View.GONE
                        btnPlaceOrder.visibility = View.VISIBLE
                        Toast.makeText(this, "$e", Toast.LENGTH_SHORT).show()
                    }
                }, Response.ErrorListener {
                    /*Catching Error if any VolleyError occurred*/

                    /* Hiding rlLoading  and making btnPlaceOrder visible*/
                    rlLoading.visibility = View.GONE
                    btnPlaceOrder.visibility = View.VISIBLE
                    Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()

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
            /* Hiding rlLoading  and making btnPlaceOrder visible
            if there is no internet connection  */
            rlLoading.visibility = View.GONE
            btnPlaceOrder.visibility = View.VISIBLE
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            super.onBackPressed()
        }
        return true
    }

    class GetOrderListFromDBAsync(context: Context) : AsyncTask<Void, Void, List<CartEntity>>() {

        val db = Room.databaseBuilder(context, Database::class.java, "restaurants_db").build()

        override fun doInBackground(vararg params: Void?): List<CartEntity> {
            return db.cartDao().getAllItems()
        }
    }

    class ClearCart(context: Context, private val resId: Int) :
        AsyncTask<Void, Void, Boolean>() {
        val db = Room.databaseBuilder(context, Database::class.java, "restaurants_db").build()
        override fun doInBackground(vararg params: Void?): Boolean {
            db.cartDao().deleteOrder(resId)
            db.close()
            return true
        }
    }
}