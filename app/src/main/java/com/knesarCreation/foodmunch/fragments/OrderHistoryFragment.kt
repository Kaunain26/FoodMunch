package com.knesarCreation.foodmunch.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.adapter.OrderHistoryParentAdapter
import com.knesarCreation.foodmunch.model.OrderHistory
import com.knesarCreation.foodmunch.util.ConnectionManager
import com.knesarCreation.foodmunch.util.SessionManager
import kotlinx.android.synthetic.main.activity_dashboard.*
import org.json.JSONException

class OrderHistoryFragment : Fragment() {

    lateinit var orderHistParentRecyclerView: RecyclerView
    lateinit var orderHistoryParentAdapter: OrderHistoryParentAdapter
    val orderHistoryList = arrayListOf<OrderHistory>()
    lateinit var sessionManager: SessionManager
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var rlLoading: RelativeLayout
    lateinit var rlNoOrderHistory: RelativeLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_order_history, container, false)

        setHasOptionsMenu(true)

        /*initializing views*/
        initViews(view)

        /*setting recycler view*/
        buildRecyclerView(view)
        return view
    }

    private fun initViews(view: View) {
        rlLoading = view.findViewById(R.id.rlLoading)
        rlNoOrderHistory = view.findViewById(R.id.rlNoOrderHistory)
        sessionManager = SessionManager(activity as Context)
        sharedPreferences = sessionManager.getSharedPreferences
    }

    private fun buildRecyclerView(view: View) {
        orderHistParentRecyclerView = view.findViewById(R.id.orderHistoryParentRecycler)
        sendJsonRequest()  /*Sending json get request for order history*/
    }

    private fun sendJsonRequest() {
        rlLoading.visibility = View.VISIBLE  /*progress bar visibility visible*/

        val queue = Volley.newRequestQueue(activity as Context)
        val userId = sharedPreferences.getString("userId", null)
        val url = "http://13.235.250.119/v2/orders/fetch_result/$userId"

        /*Checking internet connection*/
        if (ConnectionManager().isNetworkAvailable(activity as Context)) {
            val jsonObjectRequest =
                object : JsonObjectRequest(Method.GET, url, null, Response.Listener {

                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")

                        if (success) {
                            /*if success is true fetching data from server*/
                            rlLoading.visibility = View.GONE
                            val jsonArray = data.getJSONArray("data")
                            Log.d("TAG", "data:$it")
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val id = jsonObject.getString("order_id")
                                val resName = jsonObject.getString("restaurant_name")
                                val totalCost = jsonObject.getString("total_cost")
                                val date = jsonObject.getString("order_placed_at")
                                val foodItems = jsonObject.getJSONArray("food_items")

                                orderHistoryList.add(
                                    OrderHistory(
                                        id.toInt(),
                                        resName,
                                        totalCost,
                                        date,
                                        foodItems
                                    )
                                )

                                val linearLayoutManager = LinearLayoutManager(activity as Context)
                                orderHistParentRecyclerView.layoutManager = linearLayoutManager

                                orderHistoryParentAdapter =
                                    OrderHistoryParentAdapter(activity as Context, orderHistoryList)
                                orderHistParentRecyclerView.adapter = orderHistoryParentAdapter
                            }

                            if (orderHistoryList.isEmpty()) {
                                rlNoOrderHistory.visibility = View.VISIBLE
                            } else {
                                rlNoOrderHistory.visibility = View.GONE
                            }

                        } else {
                            /* Hiding progress bar */
                            rlLoading.visibility = View.GONE
                            Toast.makeText(
                                activity as Context,
                                "Some Unexpected Error Occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        /*Catching Error if any JSON exception occurred*/

                        /* Hiding progress bar */
                        rlLoading.visibility = View.GONE
                        Toast.makeText(activity as Context, "$e", Toast.LENGTH_SHORT).show()
                    }
                }, Response.ErrorListener {
                    /*Catching Error if any Volley Error occurred*/

                    /* Hiding progress bar */
                    rlLoading.visibility = View.GONE
                    Toast.makeText(
                        activity as Context,
                        "Some Unexpected Error Occurred",
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
            Toast.makeText(activity as Context, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            (activity as AppCompatActivity).drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }
}