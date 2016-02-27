package com.github.mitchwongho.android.beacon.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.mitchwongho.android.beacon.R
import com.github.mitchwongho.android.beacon.app.*
import com.github.mitchwongho.android.beacon.domain.ScanProfile
import com.github.mitchwongho.android.beacon.ext.Event
import com.jakewharton.rxbinding.view.RxView
import kotlinx.android.synthetic.main.layout_scan_profile_card.view.*
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

/**
 *
 */
class ScanProfileRecyclerViewAdapter(val profiles: MutableList<ScanProfile>, val context: Context) : RecyclerView.Adapter<ScanProfileRecyclerViewAdapter.ViewHolder>() {

    val subject: PublishSubject<UIEvent> = PublishSubject.create()

    interface UIEvent : Event
    class OnItemClicked(val scanProfile: ScanProfile, val view: View) : UIEvent
    class OnItemLongPressed(val scanProfile: ScanProfile, val view: View) : UIEvent

    inner class ViewHolder(card: View) : RecyclerView.ViewHolder(card) {
        val scanOnValue: TextView = card.scanon_period as TextView
        val scanOffValue: TextView = card.scanoff_period as TextView
        val radioResetValue: TextView = card.radio_reset_interval as TextView
        val rangingTimeoutValue: TextView = card.ranging_timeout as TextView
    }

    override fun getItemCount(): Int {
        return profiles.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, pos: Int) {
        val profile = profiles[pos]
        viewHolder.scanOnValue.text = "${SettingsAktivity.translatePositionScanOn(profile.scanOnPeriod)}ms"
        viewHolder.scanOffValue.text = "${SettingsAktivity.translatePositionScanOff(profile.scanOffPeriod)}ms"
        viewHolder.radioResetValue.text = "${SettingsAktivity.translatePositionRadioRestart(profile.radioRestartInterval)}ms"
        viewHolder.rangingTimeoutValue.text = "${SettingsAktivity.translatePositionRangingTimeout(profile.rangingTimeout)}ms"
        RxView.clicks(viewHolder.itemView).map { it -> OnItemClicked(profile,viewHolder.itemView) }.subscribe(subject)
        RxView.longClicks(viewHolder.itemView).map { it -> OnItemLongPressed(profile,viewHolder.itemView) }.subscribe(subject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): ViewHolder? {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_scan_profile_card, parent, false)
        return ViewHolder(view)
    }

    fun updateDataSet(list: List<ScanProfile>) {
        profiles.clear()
        profiles.addAll(list)
        notifyDataSetChanged()
    }

    fun observe(): Observable<UIEvent> {
        return subject.asObservable()
    }


}