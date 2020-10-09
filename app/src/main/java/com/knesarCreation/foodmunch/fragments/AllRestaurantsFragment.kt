package com.knesarCreation.foodmunch.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.adapter.AllRestaurantsRecyclerAdapter
import com.knesarCreation.foodmunch.model.Restaurants
import com.knesarCreation.foodmunch.model.Sorter
import com.knesarCreation.foodmunch.util.ConnectionManager
import kotlinx.android.synthetic.main.activity_dashboard.*
import org.json.JSONException
import java.util.*
import kotlin.collections.HashMap

class AllRestaurantsFragment : Fragment() {

    lateinit var recyclerRestaurants: RecyclerView
    lateinit var allRestaurantsRecyclerAdapter: AllRestaurantsRecyclerAdapter
    private val restaurantsList = arrayListOf<Restaurants>()
    lateinit var rlLoading: RelativeLayout
    lateinit var searchView: SearchView
    private var checkedItem = -1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_all_restaurants, container, false)

        setHasOptionsMenu(true)

        /*finding views by id*/
        recyclerRestaurants = view.findViewById(R.id.restaurantsRecyclerView)
        rlLoading = view.findViewById(R.id.rlLoading)
        searchView = view.findViewById(R.id.dashboardSearchView)

        /* Changing the color of Search Text and Search hint "By using EditText"
         *  THIS CONCEPT IS TAKEN FROM STACK_OVERFLOW
         * */
        val searchEditText: EditText =
            searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(resources.getColor(R.color.black))
        searchEditText.setHintTextColor(resources.getColor(R.color.grey))

        /*Sending a JSON request to Server*/
        sendingJsonRequest()

        /*searching restaurant*/
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                allRestaurantsRecyclerAdapter.filter.filter(newText)
                return false
            }
        })
        return view
    }

    private fun sendingJsonRequest() {
        /*making progress bar visible, Indicates that some process is Running*/
        rlLoading.visibility = View.VISIBLE

        /*Checking Internet connection*/
        if (ConnectionManager().isNetworkAvailable(activity as Context)) {
            val queue = Volley.newRequestQueue(activity as Context)
            val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

            /* Sending GET request through JSON */
            val jsonObjectRequest =
                object : JsonObjectRequest(Method.GET, url, null, Response.Listener {

                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            /*here making progress bar Invisible, Indicates that some process is completed*/
                            rlLoading.visibility = View.GONE

                            /*Getting JSONArray from JSONObject*/
                            val jsonArray = data.getJSONArray("data")
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)

                                /* Adding data to the List*/
                                restaurantsList.add(
                                    Restaurants(
                                        jsonObject.getString("id").toInt(),
                                        jsonObject.getString("name"),
                                        jsonObject.getString("rating"),
                                        "₹${jsonObject.getString("cost_for_one")}/person",
                                        jsonObject.getString("image_url")
                                    )
                                )

                                val linearLayoutManager = LinearLayoutManager(activity as Context)
                                allRestaurantsRecyclerAdapter =
                                    AllRestaurantsRecyclerAdapter(
                                        activity as Context,
                                        restaurantsList,
                                        activity!!.supportFragmentManager,
                                        1
                                    )

                                /* adding adapter to recycler view*/
                                recyclerRestaurants.adapter = allRestaurantsRecyclerAdapter

                                /*adding layoutManager  to recycler view*/
                                recyclerRestaurants.layoutManager = linearLayoutManager
                            }

                        } else {
                            /*here making progress bar Invisible, Indicates that some error occurred*/
                            rlLoading.visibility = View.GONE

                            val errorMessage = data.getString("errorMessage")
                            Toast.makeText(activity as Context, errorMessage, Toast.LENGTH_SHORT)
                                .show()
                        }

                    } catch (e: JSONException) {
                        /*here making progress bar Invisible, Indicates that some error occurred*/
                        rlLoading.visibility = View.GONE

                        Toast.makeText(activity as Context, "$e", Toast.LENGTH_SHORT).show()
                    }
                }, Response.ErrorListener {
                    /*making progress bar Invisible, Indicates that some error occurred*/
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
            /*here making progress bar Invisible, Indicates that some error occurred*/
            rlLoading.visibility = View.GONE

            /*Alerting user that there is no internet connection enabled*/
            val builder = AlertDialog.Builder(activity as Context)
            builder.setTitle("No Connection!!")
            builder.setMessage("Internet connection is not enable. Please connect to the internet")
            builder.setPositiveButton("OK") { dialog, which ->
                val openSetting =
                    /* using explicit intent for opening setting */
                    Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(openSetting)
                ActivityCompat.finishAffinity(activity!!.applicationContext as Activity)
            }
            builder.setCancelable(false)
            builder.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort -> {
                val builder = AlertDialog.Builder(activity as Context)
                builder.setTitle("Sort By?")
                val sortMethods = arrayOf("Cost(Low to High)", "Cost(High to Low)", "Rating")
                builder.setSingleChoiceItems(sortMethods, checkedItem) { _, isChecked ->
                    checkedItem = isChecked
                }
                builder.setPositiveButton("OK") { _, _ ->
                    when (checkedItem) {
                        0 -> {
                            /* Cost(Low to High) */
                            Collections.sort(restaurantsList, Sorter.costComparator)
                        }
                        1 -> {
                            /* Cost(High to Low) */
                            Collections.sort(restaurantsList, Sorter.costComparator)
                            restaurantsList.reverse()
                        }
                        else -> {
                            /* Rating */
                            Collections.sort(restaurantsList, Sorter.ratingComparator)
                            restaurantsList.reverse()
                        }
                    }
                    allRestaurantsRecyclerAdapter.notifyDataSetChanged()
                }
                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()
            }
            android.R.id.home -> (activity as AppCompatActivity).drawerLayout.openDrawer(
                GravityCompat.START

            )
        }
        return true
    }
}