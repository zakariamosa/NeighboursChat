package com.example.neighbourschatapp

import android.location.Location
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.user_row.view.*
import java.lang.String
import java.util.*
import kotlin.math.roundToInt


class UserItem(val user: User) : Item<ViewHolder>() {

    var distance: Int = 0

    override fun getLayout(): Int {
        return R.layout.user_row
    }
    override fun bind(viewHolder: ViewHolder, position: Int) {
        getDistance(NewMessageActivity.currentuserlat, NewMessageActivity.currentuserlong, user.lastLocationLat, user.lastLocationLong)

        viewHolder.itemView.tv_user_name.text = "${user.userName}, ${user.userAge}"
        viewHolder.itemView.tv_user_about.text = "\"${user.userInterest}\""
        when (distance < 1 ) {
            true -> {viewHolder.itemView.tv_distance_from.text = "Less than 1 km away"}
            false -> {viewHolder.itemView.tv_distance_from.text = ("$distance km away")}
        }
        Picasso.get().load(user.userImageUrl).into(viewHolder.itemView.iv_user_photo_toolbar)
    }

    private fun getDistance(latOne: Double, longOne: Double, latTwo: Double, longTwo: Double) {
        val startLocation = Location("")
        startLocation.latitude = latOne
        startLocation.longitude = longOne

        val endLocation = Location("")
        endLocation.latitude = latTwo
        endLocation.longitude = longTwo
        val distanceInKm = startLocation.distanceTo(endLocation) / 1000

        distance = distanceInKm.roundToInt()
    }
}
