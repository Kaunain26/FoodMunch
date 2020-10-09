package com.knesarCreation.foodmunch.fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.adapter.AllRestaurantsRecyclerAdapter
import com.knesarCreation.foodmunch.database.Database
import com.knesarCreation.foodmunch.database.RestaurantEntity
import com.knesarCreation.foodmunch.model.Restaurants
import kotlinx.android.synthetic.main.activity_dashboard.*

class FavRestaurantsFragment : Fragment() {

    lateinit var recyclerRestaurants: RecyclerView
    lateinit var allRestaurantsRecyclerAdapter: AllRestaurantsRecyclerAdapter
    private val favRestaurantsList = arrayListOf<Restaurants>()
    lateinit var rlNoFavouriteRestaurants: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_fav_restaurants, container, false)

        /*enable option menu from fragment*/
        setHasOptionsMenu(true)

        /*initializing views*/
        recyclerRestaurants = view.findViewById(R.id.favRestaurantsRecyclerView)
        rlNoFavouriteRestaurants = view.findViewById(R.id.rlNoFavoritesRestaurants)

        /* getting favourites data from database */
        val favouriteList = FavouritesAsyncTask(activity as Context).execute().get()
        if (favouriteList.isEmpty()) {
            rlNoFavouriteRestaurants.visibility = View.VISIBLE
        } else {
            rlNoFavouriteRestaurants.visibility = View.GONE
            for (i in favouriteList) {
                favRestaurantsList.add(
                    Restaurants(
                        i.id,
                        i.name,
                        i.rating,
                        i.costForOne,
                        i.imageUrl
                    )
                )
            }
        }
        /* here we are using adapter created for AllRestaurantsFragment*/
        val linearLayoutManager = LinearLayoutManager(activity as Context)
        allRestaurantsRecyclerAdapter =
            AllRestaurantsRecyclerAdapter(
                activity as Context,
                favRestaurantsList,
                activity!!.supportFragmentManager,
                2
            )

        /* adding adapter to recycler view*/
        recyclerRestaurants.adapter = allRestaurantsRecyclerAdapter

        /*adding layoutManager  to recycler view*/
        recyclerRestaurants.layoutManager = linearLayoutManager
        recyclerRestaurants.setHasFixedSize(true)
        return view
    }

    /*Async task class that is used to do database process on working thread*/
    class FavouritesAsyncTask(context: Context) : AsyncTask<Void, Void, List<RestaurantEntity>>() {

        private val db =
            Room.databaseBuilder(context, Database::class.java, "restaurants_db").build()

        override fun doInBackground(vararg params: Void?): List<RestaurantEntity> {
            return db.restaurantDao().getAllRestaurants()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            (activity as AppCompatActivity).drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }
}