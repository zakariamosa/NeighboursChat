package com.example.neighbourschatapp

class User (val userId: String,
            val userName: String,
            val userEmail: String,
            val userImageUrl: String) {
    constructor() : this ("","","","")
}