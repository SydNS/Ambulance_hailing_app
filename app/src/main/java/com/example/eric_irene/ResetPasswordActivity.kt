package com.example.eric_irene

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private var edtEmail: EditText? = null
    private var btnResetPassword: Button? = null
    private var btnBack: Button? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        edtEmail = findViewById(R.id.reset_email) as EditText
        btnResetPassword = findViewById(R.id.reset_password) as Button
        btnBack = findViewById(R.id.back) as Button
        mAuth = FirebaseAuth.getInstance()
        btnResetPassword!!.setOnClickListener(View.OnClickListener {
            val email = edtEmail!!.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(applicationContext, "Enter your email!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            mAuth!!.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Check email to reset your password!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Fail to send reset password email!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        })
        btnBack!!.setOnClickListener { finish() }
    }
}