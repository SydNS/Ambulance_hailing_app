package com.example.eric_irene

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.HashMap
import java.util.regex.Pattern

class CustomerSettingsActivity : AppCompatActivity() {
    private lateinit var mNameField: EditText
    private  lateinit var mPhoneField:EditText

    private lateinit var mBack: Button
    private  lateinit var mConfirm:Button
    private lateinit var awesomeValidation: AwesomeValidation
    private val Name = Pattern.compile(
        "^" +
                "(?=.*[a-zA-Z])" +  //any letter
                ".{1,}" +  //at least 1 characters
                "$"
    )
    private val Phone = Pattern.compile(
        ("^" +
                "(?=.*[0-9])" +  //at least 1 digit
                ".{10,}" +  //at least 1 characters
                "$")
    )
    private var mAuth: FirebaseAuth? = null
    private var mCustomerDatabase: DatabaseReference? = null

    private var userID: String? = null
    private var mName: String? = null
    private  var mPhone:kotlin.String? = null

    private val resultUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_settings)
        awesomeValidation = AwesomeValidation(ValidationStyle.BASIC)
        mNameField = findViewById(R.id.name) as EditText
        awesomeValidation!!.addValidation(this, R.id.name, Name, R.string.error_field_required)
        mPhoneField = findViewById(R.id.phone) as EditText
        awesomeValidation!!.addValidation(this, R.id.phone, Phone, R.string.error_invalid_no)
        mBack = findViewById(R.id.back) as Button
        mConfirm = findViewById(R.id.confirm) as Button
        mAuth = FirebaseAuth.getInstance()
        userID = mAuth!!.currentUser!!.uid
        mCustomerDatabase =
            FirebaseDatabase.getInstance().reference.child("Users").child("Customers").child(
                userID!!
            )
        getUserInfo()
        mConfirm.setOnClickListener(View.OnClickListener {
            if (awesomeValidation!!.validate()) {
                saveUserInformation()
            }
        })
        mBack!!.setOnClickListener(View.OnClickListener {
            finish()
            return@OnClickListener
        })
    }

    private fun getUserInfo() {
        mCustomerDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                    val map = dataSnapshot.value as Map<String, Any?>?
                    if (map!!["Name"] != null) {
                        mName = map["Name"].toString()
                        mNameField.setText(mName)
                    }
                    if (map["Phone"] != null) {
                        mPhone = map["Phone"].toString()
                        mPhoneField.setText(mPhone)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }


    private fun saveUserInformation() {
        mName = mNameField!!.text.toString()
        mPhone = mPhoneField.getText().toString()
        val userInfo: HashMap<String?, Any?> = HashMap<String?, Any?>()
        userInfo["Name"] = mName!!
        userInfo["Phone"] = mPhone!!
        mCustomerDatabase!!.updateChildren(userInfo)
        finish()
    }
}