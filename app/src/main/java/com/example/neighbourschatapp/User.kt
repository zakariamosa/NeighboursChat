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
            val userName: String,
            val userEmail: String,
            val userImageUrl: String,
            val lastLocationLat: Double,
            val lastLocationLong: Double,
            val token:String): Parcelable {
    constructor() : this ("","","","",0.0,0.0,"")
}