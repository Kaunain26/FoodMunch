package com.knesarCreation.foodmunch.adapter

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.knesarCreation.foodmunch.R
import com.knesarCreation.foodmunch.database.Database
import com.knesarCreation.foodmunch.database.RestaurantEntity
import com.knesarCreation.foodmunch.fragments.RestaurantMenuFragment
import com.knesarCreation.foodmunch.model.Restaurants
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_fav_restaurants.*
import java.util.*
import kotlin.collections.ArrayList

class AllRestaurantsRecyclerAdapter(
    val context: Context,
    private val restaurantsList: ArrayList<Restaurants>,
    private val fragmentManager: FragmentManager,
    val mode: Int
) :
    RecyclerView.Adapter<AllRestaurantsRecyclerAdapter.RestaurantViewHolder>(), Filterable {

    private var restaurantFilterList = ArrayList<Restaurants>()

    init {
        restaurantFilterList = restaurantsList
    }

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /*initializing views*/
        val imgRestaurant: ImageView = itemView.findViewById(R.id.imgRestaurant)
        val txtRestaurantName: TextView = itemView.findViewById(R.id.txtRestaurantName)
        val txtFoodPrice: TextView = itemView.findViewById(R.id.txtCostForOne)
        val txtRating: TextView = itemView.findViewById(R.id.txtRestaurantRating)
        val imgFavIcon: ImageView = itemView.findViewById(R.id.imgFav)
        val linearLayoutContent: LinearLayout = itemView.findViewById(R.id.linearLayoutContent)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_all_restaurants_single_row, parent, false)
        return RestaurantViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {

        val currentRestaurants = restaurantFilterList[position]
        holder.txtRestaurantName.text = currentRestaurants.name
        holder.txtFoodPrice.text = currentRestaurants.costForOne.toString()
        holder.txtRating.text = currentRestaurants.rating

        /*using clipToOutline method for making curve edge of Restaurants Images*/
        holder.imgRestaurant.clipToOutline = true
        Picasso.get().load(currentRestaurants.imageUrl).error(R.drawable.error_img)
            .into(holder.imgRestaurant)

        holder.linearLayoutContent.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("res_id", currentRestaurants.id)
            bundle.putString("res_name", currentRestaurants.name)
            val restaurantMenuFragment = RestaurantMenuFragment()
            restaurantMenuFragment.arguments = bundle
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, restaurantMenuFragment).commit()

        }

        val restaurantEntity = RestaurantEntity(
            currentRestaurants.id,
            currentRestaurants.name,
            currentRestaurants.rating,
            currentRestaurants.costForOne,
            currentRestaurants.imageUrl
        )
        val isFav = DBAsyncTask(context, restaurantEntity, 1).execute().get()

        if (isFav) {
            holder.imgFavIcon.setImageResource(R.drawable.ic_favourite)
        } else {
            holder.imgFavIcon.setImageResource(R.drawable.ic_unfavourite)
        }
        holder.imgFavIcon.setOnClickListener {
            if (!DBAsyncTask(context, restaurantEntity, 1).execute().get()) {

                val addToFav = DBAsyncTask(context, restaurantEntity, 2).execute()
                val status = addToFav.get()

                if (status) {
                    Toast.makeText(
                        context,
                        "Restaurants added to favourites",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    holder.imgFavIcon.setImageResource(R.drawable.ic_favourite)

                } else {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            } else {
                val removeFav = DBAsyncTask(context, restaurantEntity, 3).execute()
                val status = removeFav.get()
                if (status) {
                    /* Mode 2 indicates fav fragment*/
                    if (mode == 2) {
                        /* If Favourite fragment is opened and if we try to remove fav items from that
                        *  so we have to  write this piece of code to remove fav items at a time*/

                        restaurantsList.remove(currentRestaurants)
                        notifyItemRemoved(position)  /* notify position that item is removed*/
                        if (restaurantsList.isEmpty()) {
                            (context as AppCompatActivity).rlNoFavoritesRestaurants.visibility =
                                View.VISIBLE
                        }
                    }
                    Toast.makeText(
                        context,
                        "Restaurants removed from favourites",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    holder.imgFavIcon.setImageResource(R.drawable.ic_unfavourite)

                } else {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount() = restaurantFilterList.size

    class DBAsyncTask(
        context: Context,
        private val restaurantEntity: RestaurantEntity,
        private val mode: Int
    ) :
        AsyncTask<Void, Void, Boolean>() {

        private val db =
            Room.databaseBuilder(context, Database::class.java, "restaurants_db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {

                /* Mode 1 -> Check DB if the restaurant is favourite or not*/
                1 -> {
                    val restaurant: RestaurantEntity? =
                        db.restaurantDao().getRestaurantsId(restaurantEntity.id)
                    db.close()
                    return restaurant != null
                }

                /* Mode 2 -> Save the restaurant into DB as favourite*/
                2 -> {
                    db.restaurantDao().insertRestaurants(restaurantEntity)
                    db.close()
                    return true
                }

                /*Mode 3 -> Remove the favourite restaurant*/
                3 -> {
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }

    /* Using getFilter method implementing from Filterable Interface for searching restaurants */
    override fun getFilter(): Filter {
        return object : Filter() {

            /*The performFiltering method checks if we have typed a text in the SearchView.*/
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()

                /*If there is not any text, will return all items.*/
                if (charSearch.isEmpty()) {
                    restaurantFilterList = restaurantsList
                } else {
                    /*If there is a text, then we check if the characters match the items from the list and return the results in a FilterResults type.*/
                    val resultList = ArrayList<Restaurants>()
                    for (row in restaurantsList) {
                        if (row.toString().toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }

                    restaurantFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = restaurantFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            /*The publishResults get these results, passes it to the countryFilterList array and updates the RecyclerView.*/
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                restaurantFilterList = results?.values as ArrayList<Restaurants>
                notifyDataSetChanged()
            }
        }
    }
}