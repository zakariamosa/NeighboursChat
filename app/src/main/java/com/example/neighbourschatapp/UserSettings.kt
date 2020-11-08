package com.example.neighbourschatapp

data class UserSettings(val locationDistance:Int) {
    constructor():this(5)
}