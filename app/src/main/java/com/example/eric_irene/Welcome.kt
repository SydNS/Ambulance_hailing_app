package com.example.eric_irene

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast

class Welcome : AppCompatActivity() {

    var rellay1: RelativeLayout? = null
    var rellay2:RelativeLayout? = null
    private val mdriver: Button? = null
    private  var mCustomer:android.widget.Button? = null

    var handler = Handler()
    var runnable = Runnable {
        rellay1!!.visibility = View.VISIBLE
        rellay2!!.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        rellay1 = findViewById(R.id.rellay1) as RelativeLayout
        rellay2 = findViewById(R.id.rellay2) as RelativeLayout
        startService(Intent(this, onAppKilled::class.java))
        handler.postDelayed(runnable, 1000) //2000 is the timeout for the splash
    }

    fun driver(v: View?) {
        Toast.makeText(this, "I am a driver", Toast.LENGTH_SHORT).show()
    }

    fun patient(v: View?) {
        Toast.makeText(this, "I am a Patient", Toast.LENGTH_SHORT).show()
    }

    fun sendMessage(view: View?) {
        val intent = Intent(this, CustomerLoginActivity::class.java)
        startActivity(intent)
    }

    fun sendMessage2(view: View?) {
        val intent = Intent(this, DriverLoginActivity::class.java)
        startActivity(intent)
    }
}