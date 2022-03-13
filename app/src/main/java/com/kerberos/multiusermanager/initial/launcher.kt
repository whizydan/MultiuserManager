package com.kerberos.multiusermanager.initial

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kerberos.multiusermanager.MainActivity
import com.kerberos.multiusermanager.R
import com.stericson.RootTools.RootTools
import kotlin.system.exitProcess

class launcher : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        val git = findViewById<TextView>(R.id.git)
        val chkroot = findViewById<Button>(R.id.chkroot)

        git.setOnClickListener {
            val openurl= Intent(Intent.ACTION_VIEW)
            openurl.data= Uri.parse("https://github.com/whizydan")
            startActivity(openurl)
        }


        chkroot.setOnClickListener {
            if(RootTools.isAccessGiven() == false){
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Required Feature")
                builder.setMessage("Root access is either unavailable or not installed.The app will now exit.")
                builder.setPositiveButton(
                    "Okay"
                ) { _, _ ->
                    exitProcess(1)
                }
                builder.create().show()
            }else if(RootTools.isAccessGiven() == true){
                val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
                var editor = sharedPreferences.edit()
                editor.putString("initial","false")
                editor.commit()
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}