package com.example.eric_irene

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.annotation.NonNull
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.HashMap
import java.util.regex.Pattern

class DriverSettingActivity : AppCompatActivity() {
    private var mNameField: EditText? =
        null
    private  var mPhoneField:EditText? = null
    private  var mCarField:EditText? = null

    private var mBack: Button? = null
    private  var mConfirm:android.widget.Button? = null

    private var mAuth: FirebaseAuth? = null
    private var mDriverDatabase: DatabaseReference? = null
    private var awesomeValidation: AwesomeValidation? = null
    private val Name = Pattern.compile(
        "^" +
                "(?=.*[a-zA-Z])" +  //any letter
                ".{1,}" +  //at least 1 characters
                "$"
    )
    private val Phone = Pattern.compile(
        "^" +
                "(?=.*[0-9])" +  //at least 1 digit
                ".{10,}" +  //at least 10 characters
                "$"
    )
    private val Car = Pattern.compile(
        "^" +
                "(?=.*[0-9])" +  //at least 1 digit
                ".{6,}" +  //at least 1 characters
                "$"
    )
    private var userID: String? = null
    private var mName: String? = null
    private var mPhone: String? = null
    private var mCar: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_setting)
        awesomeValidation = AwesomeValidation(ValidationStyle.BASIC)
        mNameField = findViewById(R.id.name) as EditText
        awesomeValidation!!.addValidation(this, R.id.name, Name, R.string.error_field_required)
        mPhoneField = findViewById(R.id.phone) as EditText
        awesomeValidation!!.addValidation(this, R.id.phone, Phone, R.string.error_invalid_no)
        mCarField = findViewById(R.id.car) as EditText
        awesomeValidation!!.addValidation(this, R.id.car, Car, R.string.error_field_required)
        mBack = findViewById(R.id.back) as Button
        mConfirm = findViewById(R.id.confirm) as Button
        mAuth = FirebaseAuth.getInstance()
        userID = mAuth!!.currentUser!!.uid
        mDriverDatabase =
            FirebaseDatabase.getInstance().reference.child("Users").child("Drivers").child(
                userID!!
            )
        getUserInfo()
        mConfirm!!.setOnClickListener(View.OnClickListener {
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
        mDriverDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                    val map = dataSnapshot.value as Map<String, Any?>?
                    if (map!!["Name"] != null) {
                        mName = map["Name"].toString()
                        mNameField!!.setText(mName)
                    }
                    if (map["Phone"] != null) {
                        mPhone = map["Phone"].toString()
                        mPhoneField?.setText(mPhone)
                    }
                    if (map["Car"] != null) {
                        mCar = map["Car"].toString()
                        mCarField?.setText(mCar)
                    }
                }
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {}
        })
    }


    private fun saveUserInformation() {
        mName = mNameField!!.text.toString()
        mPhone = mPhoneField?.getText().toString()
        mCar = mCarField?.getText().toString()
        val userInfo: HashMap<String?, Any?> = HashMap<String?, Any?>()
        userInfo["Name"] = mName
        userInfo["Phone"] = mPhone
        userInfo["Car"] = mCar
        mDriverDatabase!!.updateChildren(userInfo)
        finish()
    }
}