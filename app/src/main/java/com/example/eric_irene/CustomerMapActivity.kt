package com.example.eric_irene

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.eric_irene.databinding.ActivityMapsBinding
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.ArrayList
import java.util.HashMap

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    var mLastLocation: Location? = null
    var mLocationRequest: LocationRequest? = null

    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private lateinit var mLogout: Button
    private  lateinit var mRequest:Button
    private lateinit var mSettings:Button
    private lateinit var mHistory:Button
    private var pickupLocation: LatLng? = null
    private var requestBol = false
    private var pickupMarker: Marker? = null

    private var mapFragment: SupportMapFragment? = null

    private lateinit var destination: String

    private var destinationLatlng: LatLng? = null

    private var mRatingBar: RatingBar? = null

    private var mDriverInfo: LinearLayout? = null

    private var mDriverName: TextView? = null
    private  var mDriverPhone:TextView? = null
    private  var mDriverCar:TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        destinationLatlng = LatLng(0.0, 0.0)
        mDriverInfo = findViewById(R.id.driverInfo)
        mDriverName = findViewById(R.id.driverName)
        mDriverPhone = findViewById(R.id.driverPhone)
        mDriverCar = findViewById(R.id.driverCar)
        mRequest = findViewById(R.id.request)
        mSettings = findViewById(R.id.settings)
        mRatingBar = findViewById(R.id.ratingBar)
        mHistory = findViewById<Button>(R.id.history)
        mLogout = findViewById<Button>(R.id.logout)
        mLogout!!.setOnClickListener(View.OnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, Welcome::class.java)
            startActivity(intent)
            finish()
            return@OnClickListener
        })
        mRequest.setOnClickListener(View.OnClickListener {
            if (requestBol) {
                endRide()
            } else {
            }
            requestBol = true
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            val ref = FirebaseDatabase.getInstance().getReference("customerRequest")
            val geoFire = GeoFire(ref)
            geoFire.setLocation(
                userId,
                GeoLocation(mLastLocation!!.latitude, mLastLocation!!.longitude)
            )
            pickupLocation = LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude)
            pickupMarker = mMap!!.addMarker(
                MarkerOptions().position(pickupLocation).title("Pickup Here")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.places_ic_search))
            )
            mRequest.text = "Getting Your Ambulance..."
            getClosestDriver()
        })
        mSettings.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, CustomerSettingsActivity::class.java)
            startActivity(intent)
            return@OnClickListener
        })
        mHistory.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("customerOrDriver", "Customers")
            startActivity(intent)
        })
        val autocompleteFragment =
            fragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                destination = place.name.toString()
                destinationLatlng = place.latLng
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
            }
        })
    }

    private var radius = 1
    private var driverFound = false
    private var driverFoundID: String? = null

    lateinit var geoQuery: GeoQuery


    private fun getClosestDriver() {
        val driverLocation = FirebaseDatabase.getInstance().reference.child("driversAvailable")
        val geoFire = GeoFire(driverLocation)
        geoQuery = geoFire.queryAtLocation(
            GeoLocation(
                pickupLocation!!.latitude,
                pickupLocation!!.longitude
            ), radius.toDouble()
        )
        geoQuery.removeAllListeners()
        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String, location: GeoLocation) {
                if (!driverFound && requestBol) {
                    val mCustomerDatabase =
                        FirebaseDatabase.getInstance().reference.child("Users").child("Drivers")
                            .child(key)
                    mCustomerDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                                // Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (driverFound) {
                                    return
                                }
                                driverFound = true
                                driverFoundID = dataSnapshot.key
                                val driverRef =
                                    FirebaseDatabase.getInstance().reference.child("Users")
                                        .child("Drivers").child(
                                            driverFoundID!!
                                        ).child("customerRequest")
                                val customerId = FirebaseAuth.getInstance().currentUser!!
                                    .uid
                                val map: HashMap<String, Any> = HashMap<String, Any>()
                                map["customerRideId"] = customerId
                                map["destination"] = destination
                                map["destinationLat"] = destinationLatlng!!.latitude
                                map["destinationLng"] = destinationLatlng!!.longitude
                                driverRef.updateChildren(map)
                                getDriverLocation()
                                getDriverInfo()
                                getHasRideEnded()
                                mRequest.setText("Looking for Driver's Location....")
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }
            }

            override fun onKeyExited(key: String) {}
            override fun onKeyMoved(key: String, location: GeoLocation) {}
            override fun onGeoQueryReady() {
                if (!driverFound) {
                    radius++
                    getClosestDriver()
                }
            }

            override fun onGeoQueryError(error: DatabaseError) {}
        })
    }

    private var mDriverMarker: Marker? = null
    private var driverLocationRef: DatabaseReference? = null
    private var driverLocationRefListener: ValueEventListener? = null
    private fun getDriverLocation() {
        driverLocationRef = FirebaseDatabase.getInstance().reference.child("driversWorking").child(
            driverFoundID!!
        ).child("l")
        driverLocationRefListener =
            driverLocationRef!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists() && requestBol) {
                        val map = dataSnapshot.value as List<Any?>?
                        var LocationLat = 0.0
                        var LocationLng = 0.0
                        // mRequest.setText("Ambulance Found");
                        if (map!![0] != null) {
                            LocationLat = map[0].toString().toDouble()
                        }
                        if (map[1] != null) {
                            LocationLng = map[1].toString().toDouble()
                        }
                        val driverLatLng = LatLng(LocationLat, LocationLng)
                        if (mDriverMarker != null) {
                            mDriverMarker!!.remove()
                        }
                        val loc1 = Location("")
                        loc1.latitude = pickupLocation!!.latitude
                        loc1.longitude = pickupLocation!!.longitude
                        val loc2 = Location("")
                        loc2.latitude = driverLatLng.latitude
                        loc2.longitude = driverLatLng.longitude
                        val distance = loc1.distanceTo(loc2)
                        if (distance < 100) {
                            mRequest.setText("Ambulance Arrived")
                        } else {
                            val dis = distance.toInt() / 1000
                            mRequest.setText("Ambulance Found: $dis Kms away...")
                        }
                        mDriverMarker = mMap!!.addMarker(
                            MarkerOptions().position(driverLatLng).title("Your Ambulance")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ambulance))
                        )
                    }
                }

                override fun onCancelled(@NonNull databaseError: DatabaseError) {}
            })
    }


    private fun getDriverInfo() {
        mDriverInfo!!.visibility = View.VISIBLE
        val mCustomerDatabase =
            FirebaseDatabase.getInstance().reference.child("Users").child("Drivers").child(
                driverFoundID!!
            )
        mCustomerDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                    if (dataSnapshot.child("Name") != null) {
                        mDriverName!!.text = dataSnapshot.child("Name").value.toString()
                    }
                    if (dataSnapshot.child("Phone") != null) {
                        mDriverPhone?.text = dataSnapshot.child("Phone").value.toString()
                    }
                    if (dataSnapshot.child("Car") != null) {
                        mDriverCar?.text = dataSnapshot.child("Car").value.toString()
                    }
                    var ratingSum = 0
                    var ratingsTotal = 0f
                    var ratingsAvg = 0f
                    for (child in dataSnapshot.child("rating").children) {
                        ratingSum += Integer.valueOf(child.value.toString())
                        ratingsTotal++
                    }
                    if (ratingsTotal != 0f) {
                        ratingsAvg = ratingSum / ratingsTotal
                        mRatingBar!!.rating = ratingsAvg
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 1000
        mLocationRequest!!.fastestInterval = 1000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) === PackageManager.PERMISSION_GRANTED
            ) {
            } else {
                checkLocationPermission()
            }
        }
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
        mMap!!.isMyLocationEnabled = true
    }

    var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                mLastLocation = location
                val latLng = LatLng(location.latitude, location.longitude)
                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))
                if (!getDriversAroundStarted) getDriversAround()
            }
        }
    }

    var getDriversAroundStarted = false
    var markers: MutableList<Marker> = ArrayList()
    private fun getDriversAround() {
        getDriversAroundStarted = true
        val driverLocation = FirebaseDatabase.getInstance().reference.child("driversAvailable")
        val geoFire = GeoFire(driverLocation)
        val geoQuery = geoFire.queryAtLocation(
            GeoLocation(
                mLastLocation!!.longitude,
                mLastLocation!!.latitude
            ), 999999999.0
        )
        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String, location: GeoLocation) {
                val driverLocation = LatLng(location.latitude, location.longitude)
                val mDriverMarker = mMap!!.addMarker(
                    MarkerOptions().position(driverLocation).title(key)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ambulance))
                )
                mDriverMarker.setTag(key)
                markers.add(mDriverMarker)
                for (markerIt in markers) {
                    if (markerIt.tag == key) return
                }
            }

            override fun onKeyExited(key: String) {
                for (markerIt in markers) {
                    if (markerIt.tag == key) {
                        markerIt.remove()
                    }
                }
            }

            override fun onKeyMoved(key: String, location: GeoLocation) {
                for (markerIt in markers) {
                    if (markerIt.tag == key) {
                        markerIt.position =
                            LatLng(location.latitude, location.longitude)
                    }
                }
            }

            override fun onGeoQueryReady() {}
            override fun onGeoQueryError(error: DatabaseError) {}
        })
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Please give permission...")
                    .setMessage("Please give permission...")
                    .setPositiveButton(
                        "OK"
                    ) { dialog, which ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            1
                        )
                    }
                    .create()
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) === PackageManager.PERMISSION_GRANTED
                    ) {
                        mFusedLocationClient!!.requestLocationUpdates(
                            mLocationRequest,
                            mLocationCallback,
                            Looper.myLooper()
                        )
                        mMap!!.isMyLocationEnabled = true
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Please provide the permission...",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private var driveHasEndedRef: DatabaseReference? = null
    private var driveHasEndedRefListener: ValueEventListener? = null
    private fun getHasRideEnded() {
        // String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        driveHasEndedRef =
            FirebaseDatabase.getInstance().reference.child("Users").child("Drivers").child(
                driverFoundID!!
            ).child("customerRequest").child("customerRideId")
        driveHasEndedRefListener =
            driveHasEndedRef!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                    } else {
                        endRide()
                    }
                }

                override fun onCancelled(@NonNull databaseError: DatabaseError) {}
            })
    }

    private fun endRide() {
        requestBol = false
        geoQuery.removeAllListeners()
        if (driverLocationRefListener != null && driveHasEndedRefListener != null) {
            driverLocationRef!!.removeEventListener(driverLocationRefListener!!)
            driveHasEndedRef!!.removeEventListener(driveHasEndedRefListener!!)
        }
        if (driverFoundID != null) {
            val driverRef =
                FirebaseDatabase.getInstance().reference.child("Users").child("Drivers").child(
                    driverFoundID!!
                ).child("customerRequest")
            driverRef.removeValue()
            driverFoundID = null
        }
        driverFound = false
        radius = 1
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().getReference("customerRequest")
        val geoFire = GeoFire(ref)
        geoFire.removeLocation(userId)
        if (pickupMarker != null) {
            pickupMarker!!.remove()
        }
        if (mDriverMarker != null) {
            mDriverMarker!!.remove()
        }
        mRequest.text = "Request An Ambulance"
        mDriverInfo!!.visibility = View.GONE
        mDriverName!!.text = ""
        mDriverPhone?.text = ""
    }


}