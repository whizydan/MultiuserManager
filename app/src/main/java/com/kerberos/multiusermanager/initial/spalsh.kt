package com.kerberos.multiusermanager.initial

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kerberos.multiusermanager.MainActivity

class spalsh : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val key = sharedPreferences.getString("initial","true")
        //if not first time install go to home
        if (key == "false"){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }else if(key == "true"){
            val intent = Intent(this,launcher::class.java)
            startActivity(intent)
            finish()
        }
    }
}