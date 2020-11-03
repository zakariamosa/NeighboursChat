package com.example.neighbourschatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

class ChatLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val username = intent.getStringExtra("username")
        supportActionBar?.title = username
        var rcvChatLog: RecyclerView = findViewById(R.id.recycler_view_chat_log)

        val adapter = GroupAdapter <ViewHolder>()

        adapter.add(ChatItemFrom())
        adapter.add(ChatItemTo())
        adapter.add(ChatItemFrom())
        adapter.add(ChatItemTo())
        adapter.add(ChatItemFrom())
        adapter.add(ChatItemTo())
        adapter.add(ChatItemFrom())
        adapter.add(ChatItemTo())
        adapter.add(ChatItemFrom())
        adapter.add(ChatItemTo())
        adapter.add(ChatItemFrom())
        adapter.add(ChatItemTo())


        rcvChatLog.adapter = adapter
    }
}