package com.example.eric_irene.HistoryRecyclerView

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eric_irene.HistorySingleActivity
import com.example.eric_irene.R

class HistoryViewHolders(itemView: View) : RecyclerView.ViewHolder(itemView),
    View.OnClickListener {
    var rideId: TextView
    var time: TextView
    override fun onClick(v: View) {
        val intent = Intent(v.context, HistorySingleActivity::class.java)
        val b = Bundle()
        b.putString("rideId", rideId.text.toString())
        intent.putExtras(b)
        v.context.startActivity(intent)
    }

    init {
        itemView.setOnClickListener(this)
        rideId = itemView.findViewById<View>(R.id.rideId) as TextView
        time = itemView.findViewById<View>(R.id.time) as TextView
    }
}