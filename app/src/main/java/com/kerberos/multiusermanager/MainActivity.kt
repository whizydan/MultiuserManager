package com.kerberos.multiusermanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.stericson.RootTools.RootTools
import java.io.FileFilter
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    private val m_Text = ""
    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.N)
    var isAllFabsVisible: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //copy userList to internal storage
        if (RootTools.isAccessGiven() == false){
            val badAbiDialog = MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage("Root permission denied")
                .setPositiveButton("Cancel", null).setOnDismissListener { exitProcess(1) }
            badAbiDialog.show()
        }
        Runtime.getRuntime().exec("su -c cp -R /data/system/users/* ${getExternalFilesDir(null)}")
        setContentView(R.layout.activity_main)
        val maxusers = findViewById<TextView>(R.id.maxusers)
        val fab = findViewById<FloatingActionButton>(R.id.add_button)
        val lv = findViewById<ListView>(R.id.listView)
        val appbar = findViewById<MaterialToolbar>(R.id.materialToolbar)
        val adduser = findViewById<FloatingActionButton>(R.id.adduser)
        val remuser = findViewById<FloatingActionButton>(R.id.remuser)

        fab.setOnClickListener() {
            if (isAllFabsVisible == false) {
                isAllFabsVisible = true
                adduser.visibility = FloatingActionButton.VISIBLE
                remuser.visibility = FloatingActionButton.VISIBLE
            } else {
                isAllFabsVisible = false
                adduser.visibility = FloatingActionButton.GONE
                remuser.visibility = FloatingActionButton.GONE

            }
        }


        appbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.Github -> {
                    val openurl= Intent(Intent.ACTION_VIEW)
                    openurl.data= Uri.parse("https://github.com/whizydan/MultiuserManager")
                    startActivity(openurl)
                    true
                }
                R.id.Share -> {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Switch easily between profiles with this amazing app." +
                                "get it at https://github.com/whizydan/MultiuserManager")
                        type = "text/plain"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                    true

                }
                R.id.About -> {
                    exitProcess(0)

                }
                else -> false
            }


        }
        //get maximum number of users
        val t = "Maximum supported users is: " + getSystemProperty("fw.max_users")
        maxusers.text = t
        if (getSystemProperty("fw.max_users").isNullOrEmpty()){

            val badAbiDialog = MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage("This feature is either diasbled or not supported")
                .setPositiveButton("Cancel", null).setOnDismissListener { exitProcess(1) }
            badAbiDialog.show()
        }

        var x = ""

        //find user folders
        var users = listOf<String>()
        if (!RootTools.isAccessGiven()){
            toaster("root access lost",applicationContext)}

       getExternalFilesDir(null)?.listFiles(FileFilter { it.isDirectory })?.forEach {
            // get path
            val v = getExternalFilesDir(null).toString() + "/"
            val xv = v.trim()
            val tft = it.toString().removePrefix(xv)
            x =   "$tft"
            users += x
            //return a folder names of user id's

        }


            //bind data to mutableList
        val list = ArrayList<HashMap<String, Any>>()
        for (i in users.indices) {
            val map = HashMap<String, Any>()
            map["user"] = "user ID: " + users[i]
            map["username"] = users[i]
            list.add(map)
        }
        val from = arrayOf("user", "username")
        val to = arrayOf(R.id.userid, R.id.username).toIntArray()


        //bind list to adapter
        val adapter = SimpleAdapter(this, list, R.layout.custom_list, from, to)
        lv.adapter = adapter
        lv.setOnItemClickListener { parent, _, position, _ ->
            val element = parent.getItemIdAtPosition(position)
            val user_id = users[element.toInt()]
            switchUser(user_id.toString())
        }

        adduser.setOnClickListener {
            val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Add User")

            // Set up the input
            val input = EditText(this)
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.hint = "Enter A Name"
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the buttons
            builder.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                // Here you get get input text from the Edittext
                val m_Text = input.text.toString()
                Runtime.getRuntime().exec("su -c pm create-user $m_Text")
            })
            builder.setNegativeButton(
                "Cancel",
                DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() })

            builder.show()

        }

        remuser.setOnClickListener {
            val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Remove User")

            // Set up the input
            val input = EditText(this)
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.hint = "Enter User Id"
            input.inputType = InputType.TYPE_CLASS_NUMBER
            builder.setView(input)

            // Set up the buttons
            builder.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                // Here you get get input text from the Edittext
                val m_Text: String = input.text.toString()
                Runtime.getRuntime().exec("su -c pm remove-user $m_Text")
                reload()
            })
            builder.setNegativeButton(
                "Cancel",
                DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() })

            builder.show()
        }

    }

    override fun onDestroy() {
        val x = getExternalFilesDir(null).toString() + "/"+"*"
        Runtime.getRuntime().exec("su -c rm -rR $x")
        super.onDestroy()
    }

    private fun switchUser(id: String) {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Switch user?")
        builder.setMessage("Are you sure you want to Switch?")
        builder.setPositiveButton("Yes",
            DialogInterface.OnClickListener { _, _ ->
                Runtime.getRuntime().exec("su -c am switch-user $id")
            })
        builder.setNegativeButton("No",
            DialogInterface.OnClickListener { _, _ ->
            reload()
            })
        builder.create().show()
    }

    @SuppressLint("PrivateApi")
    fun getSystemProperty(key: String?): String? {
        var value: String? = null
        try {
            value = Class.forName("android.os.SystemProperties")
                .getMethod("get", String::class.java).invoke(null, key) as String
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return value
    }

    private fun toaster(msg: String?, context: Context) {
        val toast = Toast.makeText(context, "$msg", Toast.LENGTH_LONG)
        toast.show()
    }

    private fun reload(){
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        val x = getExternalFilesDir(null).toString() + "/"+"*"
        Runtime.getRuntime().exec("su -c rm -rR $x")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


}