package com.example.eric_irene

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import java.util.regex.Pattern

class CustomerSignup : AppCompatActivity() {
    private lateinit var mEmail: EditText
    private  lateinit var mPassword:EditText
    private var mAuth: FirebaseAuth? = null
    private lateinit var firebaseAuthListener: AuthStateListener
    private lateinit var mLogin: TextView
    private lateinit var msignup:android.widget.Button
    private var awesomeValidation: AwesomeValidation? = null
    private val PASSWORD_PATTERN = Pattern.compile(
        "^" +
                "(?=.*[0-9])" +  //at least 1 digit
                "(?=.*[a-z])" +  //at least 1 lower case letter
                "(?=.*[A-Z])" +  //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +  //any letter
                "(?=.*[@#$%^&+=?])" +  //at least 1 special character
                "(?=\\S+$)" +  //no white spaces
                ".{8,}" +  //at least 8 characters
                "$"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_signup)
        awesomeValidation = AwesomeValidation(ValidationStyle.BASIC)
        mEmail = findViewById(R.id.email)
        mPassword = findViewById(R.id.password)
        mLogin = findViewById(R.id.textView)
        msignup = findViewById(R.id.signup)
        awesomeValidation!!.addValidation(
            this,
            R.id.email,
            Patterns.EMAIL_ADDRESS,
            R.string.error_invalid_email
        )
        awesomeValidation!!.addValidation(
            this,
            R.id.password,
            PASSWORD_PATTERN,
            R.string.error_incorrect_password
        )
        mAuth = FirebaseAuth.getInstance()
        firebaseAuthListener = AuthStateListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val intent = Intent(this@CustomerSignup, CustomerLoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        msignup.setOnClickListener(View.OnClickListener {
            val email = mEmail.getText().toString()
            val password: String = mPassword.getText().toString()
//            if (awesomeValidation!!.validate()) {
                mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                    this@CustomerSignup
                ) { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(this@CustomerSignup, "Sign Up Error", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        mAuth!!.currentUser!!.sendEmailVerification()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this@CustomerSignup,
                                        "Registered Successfully ! Please, check your Email for verification.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@CustomerSignup,
                                        task.exception.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        FirebaseAuth.getInstance().signOut()
                    }
                }
//            }
        })
        mLogin.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@CustomerSignup, CustomerLoginActivity::class.java)
            startActivity(intent)
            return@OnClickListener
        })
    }

    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener((firebaseAuthListener))
    }

    override fun onStop() {
        super.onStop()
        mAuth!!.removeAuthStateListener((firebaseAuthListener))
    }
}