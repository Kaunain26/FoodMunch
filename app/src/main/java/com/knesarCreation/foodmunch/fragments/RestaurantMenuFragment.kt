package com.knesarCreation.foodmunch.fragments

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.activity.CartActivity
import com.knesarCreation.foodmunch.adapter.ResMenuRecyclerAdapter
import com.knesarCreation.foodmunch.database.CartEntity
import com.knesarCreation.foodmunch.database.Database
import com.knesarCreation.foodmunch.model.RestaurantMenu
import com.knesarCreation.foodmunch.util.ConnectionManager
import kotlinx.android.synthetic.main.activity_dashboard.*
import org.json.JSONException

class RestaurantMenuFragment : Fragment() {

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var resDetailsRecyclerView: RecyclerView
    lateinit var resMenuRecyclerAdapter: ResMenuRecyclerAdapter
    lateinit var rlLoading: RelativeLayout
    lateinit var btnProceedToCard: Button
    private var menuList = arrayListOf<RestaurantMenu>()
    private var cartList = arrayListOf<RestaurantMenu>()

    companion object {
        var restaurantId: Int? = 0
        var restaurantName: String? = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_restaurant_menu, container, false)

        /*getting restaurant id from AllRestaurantFragment using Bundle*/
        restaurantId = arguments?.getInt("res_id", 0)
        restaurantName = arguments?.getString("res_name")

        setHasOptionsMenu(true)

        /* finding views by id*/
        resDetailsRecyclerView = view.findViewById(R.id.resDetailsRecyclerView)
        rlLoading = view.findViewById(R.id.rlLoading)
        btnProceedToCard = view.findViewById(R.id.btnProceedToCart)

        /*Setting title to toolbar, Enabling home button and lock the drawer*/
        (activity as AppCompatActivity).supportActionBar?.title = restaurantName
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)


        /* Disable Hamburger icon in fragment */
        disableToggle()

        /*sending GET request using jsonObjectRequest*/
        sendingJsonRequest()

        /**Adding ordered food in Database using Gson class
         * Gson is nothing but its only converting list to a String type so that we can store a COMPLEX data in DATABASE*/
        proceedToCart()
        return view
    }

    private fun disableToggle() {
        toggle = ActionBarDrawerToggle(
            activity,
            (activity as AppCompatActivity).drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        /*replace hamburger icon to arrow icon*/
        toggle.isDrawerIndicatorEnabled = false
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun proceedToCart() {
        btnProceedToCard.setOnClickListener {
            val gson = Gson()
            val foodItems = gson.toJson(cartList)

            val addItemsToCart =
                DBCartAsyncTask(activity as Context, restaurantId!!, foodItems, 1).execute()
            val success = addItemsToCart.get()
            if (success) {
                val data = Intent(activity as Context, CartActivity::class.java)
                data.putExtra("res_id", restaurantId)
                data.putExtra("res_name", restaurantName)
                startActivity(data)
            } else {
                Toast.makeText(
                    activity as Context, "Some unexpected error occurred", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendingJsonRequest() {
        /*progress bar is set as visible , indicates that some process is running*/
        rlLoading.visibility = View.VISIBLE

        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

        /*checking internet connection*/
        if (ConnectionManager().isNetworkAvailable(activity as Context)) {
            val jsonObjectRequest =
                object : JsonObjectRequest(Method.GET, url + restaurantId, null, Response.Listener {

                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            /*progress bar is set as invisible , indicates that some process is completed*/
                            rlLoading.visibility = View.GONE

                            /*Getting JSONArray from JSONObject*/
                            val jsonArray = data.getJSONArray("data")
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)

                                val restaurantMenu = RestaurantMenu(
                                    jsonObject.getString("id").toInt(),
                                    jsonObject.getString("name"),
                                    jsonObject.getString("cost_for_one"),
                                    jsonObject.getString("restaurant_id").toInt()
                                )
                                menuList.add(restaurantMenu)

                                val linearLayoutManager = LinearLayoutManager(activity as Context)
                                resMenuRecyclerAdapter =
                                    ResMenuRecyclerAdapter(
                                        activity as Context,
                                        menuList,
                                        object : ResMenuRecyclerAdapter.OnItemClickListener {
                                            override fun onAddItemClick(
                                                orderItems: RestaurantMenu
                                            ) {
                                                /*hiding  btnProceedToCard visible */
                                                btnProceedToCard.visibility = View.VISIBLE
                                                cartList.add(orderItems) /*Adding food to cart list*/
                                                ResMenuRecyclerAdapter.isCartEmpty = false
                                            }

                                            override fun onRemoveItemClick(orderItems: RestaurantMenu) {
                                                cartList.remove(orderItems) /*Removing food from cart list*/
                                                if (cartList.isEmpty()) {
                                                    /*hiding  btnProceedToCard */
                                                    btnProceedToCard.visibility = View.GONE
                                                    ResMenuRecyclerAdapter.isCartEmpty = true
                                                }
                                            }

                                        })

                                /*setting layout manager*/
                                resDetailsRecyclerView.layoutManager = linearLayoutManager

                                /*setting adapter*/
                                resDetailsRecyclerView.adapter = resMenuRecyclerAdapter
                            }

                        } else {
                            /*progress bar is set as invisible , here it indicates error message using Toast*/
                            rlLoading.visibility = View.GONE
                            /*if success is FALSE the showing a error message*/
                            val error = data.getString("errorMessage")
                            Toast.makeText(activity as Context, error, Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: JSONException) {
                        /*Catching Error if any JSON exception occurred*/

                        /*progress bar is set as invisible , here it indicates error message using Toast*/
                        rlLoading.visibility = View.GONE
                        Toast.makeText(activity as Context, "$e", Toast.LENGTH_SHORT).show()
                    }


                }, Response.ErrorListener {
                    /*Catching Error if any VolleyError occurred*/

                    /*progress bar is set as invisible , here it indicates error message using Toast*/
                    rlLoading.visibility = View.GONE
                    Toast.makeText(activity as Context, "$it", Toast.LENGTH_SHORT).show()

                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val header = HashMap<String, String>()
                        header["Content-type"] = "application/json"
                        header["token"] = "c28e4d98687310"
                        return header
                    }
                }
            queue.add(jsonObjectRequest)
        } else {
            Toast.makeText(activity as Context, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAllRestaurantsFragment() {
        activity!!.supportFragmentManager.beginTransaction().replace(
            R.id.fragment_container,
            AllRestaurantsFragment()
        ).commit()

        (activity as AppCompatActivity).supportActionBar?.title = "All Restaurants"
        (activity as AppCompatActivity).navigationView.setCheckedItem(R.id.allRestaurants)
        (activity as AppCompatActivity).drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        /*Enable the hamburger icon*/
        toggle.isDrawerIndicatorEnabled = true
        toggle.syncState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {

            when (activity!!.supportFragmentManager.findFragmentById(R.id.fragment_container)) {
                !is AllRestaurantsFragment -> {
                    /*checking that cartList is empty or not*/
                    if (!ResMenuRecyclerAdapter.isCartEmpty) {
                        /* if cartLis is not empty then conformation dialog will appear*/
                        val dialog = AlertDialog.Builder(activity as Context)
                        dialog.setTitle("Confirmation")
                        dialog.setMessage("Going back will reset cart items. Do you still want to proceed")
                        dialog.setPositiveButton("OK") { _, _ ->

                            /*Deleting cart items*/
                            DBCartAsyncTask(
                                activity as Context,
                                restaurantId!!,
                                "null",
                                2
                            ).execute()
                            openAllRestaurantsFragment()
                            ResMenuRecyclerAdapter.isCartEmpty = true
                        }
                        dialog.setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        dialog.show()
                    } else {
                        /*if cartList is empty then it simply open  AllRestaurantsFragment*/
                        openAllRestaurantsFragment()
                    }
                }
            }
        }
        return true
    }

    class DBCartAsyncTask(
        context: Context,
        private val resId: Int,
        private val foodItems: String,
        private val mode: Int
    ) :
        AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, Database::class.java, "restaurants_db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    val cartEntity = CartEntity(resId, foodItems)
                    db.cartDao().insertOrder(cartEntity)
                    db.close()
                    return true
                }
                2 -> {
                    db.cartDao().deleteOrder(resId)
                    db.close()
                    return true
                }
            }
            return false
        }
    }
}