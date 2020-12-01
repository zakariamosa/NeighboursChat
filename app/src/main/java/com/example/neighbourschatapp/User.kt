package com.example.neighbourschatapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
/*
Jag har lagt till experimental = true i build.gradle. Då kan jag här lägga till @Parcelize
och göra denna klass Parcelable. Detta gör att vi kan skicka hela detta object som putExtra vid Intent.
Detta använder vi senare i ChatLogActivity (alltså själva chatten). Där hjälper det oss att kunna importera hela användaren
från NewMessageActivity.
 */
@Parcelize
class User (val userId: String,
            var userName: String,
            val userEmail: String,
            var userImageUrl: String,
            val lastLocationLat: Double,
            val lastLocationLong: Double,
            var userAge: String,
            var userInterest: String,
            val token: String,
            var distanceFromMe: Int): Parcelable {
    constructor() : this ("","","","",0.0,
            0.0,"", "", "", 0)
}