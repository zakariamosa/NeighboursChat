package com.example.neighbourschatapp

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class General() {
    fun getblockedUsers():Boolean{

        var finishedPart:Int=0
        val userId = FirebaseAuth.getInstance().uid

        val db = FirebaseFirestore.getInstance()
        blocklistaMeAndThem.clear()
        if (userId != null) {
            //get the people i blocked
            val itemRef = db.collection("BlockList").document(userId).collection("UserBlockedList")
            itemRef.addSnapshotListener() { snapshot, e ->
                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        val settingblockuser = document.toObject(User::class.java)
                        if (settingblockuser != null) {
                            blocklistaMeAndThem.add(settingblockuser)
                        }
                    }
                }
                finishedPart++
            }
            //get the people they blocked me
            val itemRefTheyBlockedMe = db.collection("BlockList").document().collection("UserBlockedList")
            itemRefTheyBlockedMe.addSnapshotListener() { snapshot, e ->
                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        val settingblockuser = document.toObject(User::class.java)
                        if (settingblockuser != null) {
                            if (settingblockuser.userId==userId){
                                blocklistaMeAndThem.add(settingblockuser)
                                Log.d("!!", blocklistaMeAndThem.size.toString())
                            }
                        }
                    }
                }
                finishedPart++
            }
        }
        return finishedPart==2
    }
}