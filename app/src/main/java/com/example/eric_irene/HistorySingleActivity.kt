package com.example.eric_irene

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.RatingBar
import android.widget.RatingBar.OnRatingBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import com.directions.route.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class HistorySingleActivity : AppCompatActivity(), OnMapReadyCallback, RoutingListener {
    private var rideId: String? =
        null
    private  lateinit var currentUserId: String
    private  lateinit var customerId: String
    private  lateinit var driverId: String
    private  var userDriverOrCustomer: String? = null

    private lateinit var rideLocation: TextView
    private lateinit var rideDistance: TextView
    private lateinit var rideDate: TextView
    private lateinit var userName: TextView
    private var userPhone: TextView? = null

    private var ridePrice: Double? = null

    private var mRatingBar: RatingBar? = null

    private var historyRideInfoDb: DatabaseReference? = null

    private var destinationLatLng: LatLng? =  null
    private  var pickupLatLng:com.google.android.gms.maps.model.LatLng? = null
    private var distance: String? = null

    private var mMap: GoogleMap? = null
    private var mMapFragment: SupportMapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_single)
        polylines = ArrayList()
        rideId = intent.extras!!.getString("rideId")
        mMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mMapFragment!!.getMapAsync(this)
        rideLocation = findViewById<TextView>(R.id.rideLocation)
        rideDistance = findViewById(R.id.rideDistance)
        rideDate = findViewById(R.id.rideDate)
        userName = findViewById(R.id.userName)
        userPhone = findViewById(R.id.userPhone)
        mRatingBar = findViewById(R.id.ratingBar)
        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        historyRideInfoDb =
            FirebaseDatabase.getInstance().reference.child("History").child(rideId!!)
        getRideInformation()
    }

    private fun getRideInformation() {
        historyRideInfoDb!!.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (child in dataSnapshot.children) {
                        if (child.key == "customer") {
                            customerId = child.value.toString()
                            if (customerId != currentUserId) {
                                userDriverOrCustomer = "Drivers"
                                getUserInformation("Customers", customerId)
                            }
                        }
                        if (child.key == "driver") {
                            driverId = child.value.toString()
                            if (driverId != currentUserId) {
                                userDriverOrCustomer = "Customers"
                                getUserInformation("Drivers", driverId)
                                displayCustomerRelatedObjects()
                            }
                        }
                        if (child.key == "timestamp") {
                            rideDate!!.text =
                                getDate(java.lang.Long.valueOf(child.value.toString()))
                        }
                        if (child.key == "rating") {
                            mRatingBar!!.rating = Integer.valueOf(child.value.toString()).toFloat()
                        }
                        if (child.key == "distance") {
                            distance = child.value.toString()
                            rideDistance!!.text =
                                distance!!.substring(0, Math.min(distance!!.length, 5)) + "km"
                            ridePrice = java.lang.Double.valueOf(distance) * 0.5
                        }
                        if (child.key == "destination") {
                            rideLocation!!.text = child.value.toString()
                        }
                        if (child.key == "location") {

                            //rideLocation.setText(getDate(Long.valueOf(child.getValue().toString())));
                            pickupLatLng = LatLng(
                                java.lang.Double.valueOf(
                                    child.child("from").child("lat").value.toString()
                                ),
                                java.lang.Double.valueOf(
                                    child.child("from").child("lng").value.toString()
                                )
                            )
                            destinationLatLng = LatLng(
                                java.lang.Double.valueOf(
                                    child.child("to").child("lat").value.toString()
                                ),
                                java.lang.Double.valueOf(
                                    child.child("to").child("lng").value.toString()
                                )
                            )
                            if (destinationLatLng !== LatLng(0.0, 0.0)) {
                                getRouteToMarker()
                            }
                        }
                    }
                }
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {}
        })
    }

    private fun displayCustomerRelatedObjects() {
        mRatingBar!!.visibility = View.VISIBLE
        mRatingBar!!.onRatingBarChangeListener =
            OnRatingBarChangeListener { ratingBar, rating, fromUser ->
                historyRideInfoDb!!.child("rating").setValue(rating)
                val mDriverRatingDb =
                    FirebaseDatabase.getInstance().reference.child("Users").child("Drivers")
                        .child(driverId).child("rating")
                mDriverRatingDb.child(rideId!!).setValue(rating)
            }
    }

    private fun getUserInformation(otherUserDriverOrCustomer: String, otherUserId: String) {
        val mOtherUSerDB =
            FirebaseDatabase.getInstance().reference.child("Users").child(otherUserDriverOrCustomer)
                .child(otherUserId)
        mOtherUSerDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val map = dataSnapshot.value as Map<String, Any?>?
                    if (map!!["Name"] != null) {
                        userName!!.text = map["Name"].toString()
                    }
                    if (map["Phone"] != null) {
                        userPhone!!.text = map["Phone"].toString()
                    }
                }
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {}
        })
    }

    private fun getDate(time: Long): String? {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.timeInMillis = time * 1000
        return DateFormat.format("MM-dd-yyyy hh:mm", cal).toString()
    }

    private fun getRouteToMarker() {
        val routing = Routing.Builder()
            .travelMode(AbstractRouting.TravelMode.DRIVING)
            .withListener(this)
            .alternativeRoutes(false)
            .waypoints(pickupLatLng, destinationLatLng)
            .build()
        routing.execute()
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
    }

    private var polylines: MutableList<Polyline>? = null
    private val COLORS = intArrayOf(R.color.primary_dark_material_light)

    override fun onRoutingFailure(e: RouteException?) {
        if (e != null) {
            Toast.makeText(this, "Error: " + e.message, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRoutingStart() {}

    override fun onRoutingSuccess(route: ArrayList<Route>, shortestRouteIndex: Int) {
        val builder = LatLngBounds.Builder()
        builder.include(pickupLatLng)
        builder.include(destinationLatLng)
        val bounds = builder.build()
        val width = resources.displayMetrics.widthPixels
        val padding = (width * 0.2).toInt()
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        mMap!!.animateCamera(cameraUpdate)

        // mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
        //mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("destination"));
        if (polylines!!.size > 0) {
            for (poly in polylines!!) {
                poly.remove()
            }
        }
        polylines = ArrayList()
        //add route(s) to the map.
        for (i in route.indices) {

            //In case of more than 5 alternative routes
            val colorIndex = i % COLORS.size
            val polyOptions = PolylineOptions()
            polyOptions.color(resources.getColor(COLORS[colorIndex]))
            polyOptions.width((10 + i * 3).toFloat())
            polyOptions.addAll(route[i].points)
            val polyline = mMap!!.addPolyline(polyOptions)
            (polylines as ArrayList<Polyline>).add(polyline)
            Toast.makeText(
                applicationContext,
                "Route " + (i + 1) + ": distance - " + route[i].distanceValue + ": duration - " + route[i].durationValue,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onRoutingCancelled() {}

    private fun erasePolylines() {
        for (line in polylines!!) {
            line.remove()
        }
        polylines!!.clear()
    }

}