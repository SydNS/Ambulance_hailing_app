package com.example.eric_irene.HistoryRecyclerView

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eric_irene.R

class HistoryAdapter: RecyclerView.Adapter<HistoryViewHolders>() {
    private var itemList: List<HistoryObject>? = null
    private var context: Context? = null

    fun HistoryAdapter(itemList: List<HistoryObject>?, context: Context?) {
        this.itemList = itemList
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolders {
        val layoutView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.itemview_history, null, false)
        val lp: RecyclerView.LayoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ) as RecyclerView.LayoutParams
        layoutView.layoutParams = lp
        return HistoryViewHolders(layoutView)
    }

    override fun onBindViewHolder(holder: HistoryViewHolders, position: Int) {
        holder.rideId.text = itemList!![position].rideId
        holder.time.text = itemList!![position].time
    }

    override fun getItemCount(): Int {
        return itemList!!.size
    }


}