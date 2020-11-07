package com.example.neighbourschatapp

class ChatMessage (val id: String, val text: String, val fromId: String, val toId: String, var timeStamp: Long, var read: Boolean) {
    constructor(): this ("","","","",-1, false)
}