package com.example.eric_irene

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

class DriverLoginActivity : AppCompatActivity() {
    private lateinit var mEmail: EditText
    private lateinit var mPassword:EditText


    private var mAuth: FirebaseAuth? = null
    private var firebaseAuthListener: AuthStateListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        val mLogin: Button
        val mRegistration: Button
        val mForgetPassword: TextView
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_login)
        mAuth = FirebaseAuth.getInstance()
        firebaseAuthListener = AuthStateListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && mAuth!!.currentUser!!.isEmailVerified) {
                val intent = Intent(this@DriverLoginActivity, DriverMapActivity::class.java)
                Toast.makeText(
                    this@DriverLoginActivity,
                    "Welcome to Med Rescue",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(intent)
                finish()
            }
        }
        mForgetPassword = findViewById(R.id.forgetPassword)
        mForgetPassword.setOnClickListener {
            val intent = Intent(this@DriverLoginActivity, ResetPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
        mEmail = findViewById(R.id.email)
        mPassword = findViewById(R.id.password)
        mLogin = findViewById(R.id.login)
        mRegistration = findViewById(R.id.registration)
        mRegistration.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@DriverLoginActivity, DriverSignup::class.java)
            startActivity(intent)
            return@OnClickListener
        })
        mLogin.setOnClickListener {
            val email = mEmail.getText().toString()
            val password: String = mPassword.getText().toString()
            mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                this@DriverLoginActivity
            ) { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(
                        this@DriverLoginActivity,
                        "Driver Sign In Error",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (mAuth!!.currentUser!!.isEmailVerified) {
                        val user_id = mAuth!!.currentUser!!.uid
                        val current_user_db =
                            FirebaseDatabase.getInstance().reference.child("Users").child("Drivers")
                                .child(user_id)
                        current_user_db.setValue(true)
                    } else {
                        Toast.makeText(
                            this@DriverLoginActivity,
                            "Please, verify your email.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
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