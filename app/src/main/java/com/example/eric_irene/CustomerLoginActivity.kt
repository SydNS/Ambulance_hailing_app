package com.example.eric_irene

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CustomerLoginActivity : AppCompatActivity() {

    private lateinit var mEmail: EditText
    private lateinit var mPassword:EditText
    private val TAG = "CustomerLoginActivity"

    private var mAuth: FirebaseAuth? = null
    private var firebaseAuthListener: AuthStateListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_login)
        mEmail = findViewById(R.id.email)
        mPassword = findViewById(R.id.password)
        val mLogin: Button = findViewById(R.id.login)
        val mRegistration: TextView = findViewById(R.id.textView)
        val mForgetPassword: TextView = findViewById(R.id.forgetPassword)
        mForgetPassword.setOnClickListener {
            val intent = Intent(this@CustomerLoginActivity, ResetPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
        mAuth = FirebaseAuth.getInstance()
        firebaseAuthListener = AuthStateListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && mAuth!!.currentUser!!.isEmailVerified) {
                val intent = Intent(this@CustomerLoginActivity, CustomerMapActivity::class.java)
                Toast.makeText(
                    this@CustomerLoginActivity,
                    "Welcome ",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(intent)
                finish()
            }
        }
        mRegistration.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@CustomerLoginActivity, CustomerSignup::class.java)
            Log.v(TAG, "First")
            startActivity(intent)
            return@OnClickListener
        })
        mLogin.setOnClickListener {
            val email = mEmail.text.toString()
            val password: String = mPassword.text.toString()
             if(email.isNotEmpty() || password.isNotEmpty()) {

                 mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                     this@CustomerLoginActivity
                 ) { task ->
                     if (!task.isSuccessful) {
                         Toast.makeText(
                             this@CustomerLoginActivity,
                             "Incorrect Email-id/Password.",
                             Toast.LENGTH_SHORT
                         ).show()
                     } else {
                         if (mAuth!!.currentUser!!.isEmailVerified) {
                             val user_id = mAuth!!.currentUser!!.uid
                             val current_user_db =
                                 FirebaseDatabase.getInstance().reference.child("Users")
                                     .child("Customers").child(user_id)
                             current_user_db.setValue(true)
                         } else {
                             Toast.makeText(
                                 this@CustomerLoginActivity,
                                 "Please, verify your email. by clicking the link sent to you",
                                 Toast.LENGTH_LONG
                             ).show()
                         }
                     }
                 }
             }else{
                 Toast.makeText(this, " Fill the blanks", Toast.LENGTH_SHORT).show()
             }
        }
    }

    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(firebaseAuthListener!!)
    }

    override fun onStop() {
        super.onStop()
        mAuth!!.removeAuthStateListener(firebaseAuthListener!!)
    }
}