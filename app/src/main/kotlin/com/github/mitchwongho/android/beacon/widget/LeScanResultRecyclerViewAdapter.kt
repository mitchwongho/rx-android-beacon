package com.github.mitchwongho.android.beacon.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.mitchwongho.android.beacon.R
import com.github.mitchwongho.android.beacon.bluetooth.rx.LeScanResult
import com.github.mitchwongho.android.beacon.ext.FilterType
import com.github.mitchwongho.android.beacon.ext.setCardBackgroundColorCompat
import kotlinx.android.synthetic.main.layout_card.view.*
import java.util.*

/**
 *
 */
class LeScanResultRecyclerViewAdapter(val beacons: MutableList<LeScanResult>, val context: Context) : RecyclerView.Adapter<LeScanResultRecyclerViewAdapter.ViewHolder>() {

    val TAG = LeScanResultRecyclerViewAdapter::class.java.simpleName

    inner class ViewHolder(card: View) : RecyclerView.ViewHolder(card) {
        val card: CardView = card.card_view
        val name: TextView = card.device_name
        val deviceId: TextView = card.device_id
        val rssi: TextView = card.device_rssi
        val age: TextView = card.update_age
        val oob: TextView = card.oob
    }

    override fun getItemCount(): Int = beacons.size

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val beacon = beacons[position]
        holder?.name?.text = beacon.deviceName
        holder?.deviceId?.text = beacon.deviceAddress
        holder?.rssi?.text = beacon.rssi.toString()
        holder?.age?.text = beacon.age.toString()
        if (beacon.oob > 0) holder?.oob?.text = beacon.oob.toString()
        else holder?.oob?.text = ""
        // apply background
        when (beacon.age) {
            in 0..1999 -> holder?.card?.setCardBackgroundColorCompat(R.color.statusExcellent)
            in 2000..19999 -> holder?.card?.setCardBackgroundColorCompat(R.color.statusGood)
            in 20000..39999 -> holder?.card?.setCardBackgroundColorCompat(R.color.statusOkay)
            in 40000..59999 -> holder?.card?.setCardBackgroundColorCompat(R.color.statusPoor)
            else -> holder?.card?.setCardBackgroundColorCompat(R.color.statusBad)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        val card = LayoutInflater.from(parent?.context).inflate(R.layout.layout_card, parent, false)
        return ViewHolder(card)
    }

    fun applyList(list: List<LeScanResult>, filterType: FilterType) {
        beacons.clear()
        beacons.addAll(list.sortedWith(Comparator { a, b ->
            when (filterType) {
                FilterType.TIMEOUT -> {
                    if (a.timestamp > b.timestamp)
                        1
                    else if (b.timestamp > a.timestamp)
                        -1
                    else
                        0
                }
                FilterType.RSSI -> {
                    if (a.rssi > b.rssi)
                        1
                    else if (b.rssi > a.rssi)
                        -1
                    else
                        0
                }
                else -> 0
            }
        }))
        Log.d(TAG, "applyList {count=${beacons.size}}")
        notifyDataSetChanged()
    }
}