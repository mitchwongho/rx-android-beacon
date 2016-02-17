package com.github.mitchwongho.android.beacon.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.github.mitchwongho.android.beacon.R
import com.github.mitchwongho.android.beacon.domain.ProfileLayout
import com.github.mitchwongho.android.beacon.ext.Event
import com.jakewharton.rxbinding.view.RxView
import kotlinx.android.synthetic.main.layout_profile_format_card.view.*
import rx.Observable
import rx.lang.kotlin.BehaviourSubject

/**
 *
 */
class ProfileLayoutsRecyclerViewAdapter(val profiles: MutableList<ProfileLayout>, val context: Context) : RecyclerView.Adapter<ProfileLayoutsRecyclerViewAdapter.ViewHolder>() {

    val TAG = ProfileLayoutsRecyclerViewAdapter::class.java.simpleName

    private val subject = BehaviourSubject<ProfileItemSelected>()

    public inner class ProfileItemSelected(val profile: ProfileLayout, val view: CardView) : Event
    public inner class ProfileItemClicked(val profile: ProfileLayout, val view: CardView) : Event

    inner class ViewHolder(card: View) : RecyclerView.ViewHolder(card) {
        val card: CardView = card as CardView
        val name: EditText = card.edit_profile_name
        val layout: EditText = card.edit_profile_layout
    }

    override fun getItemCount(): Int = profiles.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profile = profiles[position]
        holder.name.setText(profile.name)
        holder.name.isEnabled = true
        holder.name.isFocusable = false
        holder.name.isClickable = true
        holder.name.isFocusableInTouchMode = false
        RxView.longClicks(holder.name).map { e -> ProfileItemSelected(profile, holder.card) }.subscribe(subject)
        RxView.clicks(holder.name).map { e -> ProfileItemSelected(profile, holder.card) }.subscribe(subject)
        holder.layout.setText(profile.layout)
        holder.layout.isEnabled = true
        holder.layout.isFocusable = false
        holder.layout.isClickable = true
        holder.layout.isFocusableInTouchMode = false
        RxView.longClicks(holder.layout).map { e -> ProfileItemSelected(profile, holder.card) }.subscribe(subject)
        RxView.clicks(holder.layout).map { e -> ProfileItemSelected(profile, holder.card) }.subscribe(subject)
        RxView.longClicks(holder.card).map { e -> ProfileItemSelected(profile, holder.card) }.subscribe(subject)
        RxView.clicks(holder.card).map { e -> ProfileItemSelected(profile, holder.card) }.subscribe(subject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        val card = LayoutInflater.from(context).inflate(R.layout.layout_profile_format_card, parent, false) as CardView
        card.isClickable = true
        card.isFocusable = true
        card.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
        return ViewHolder(card)
    }

    fun applyList(list: List<ProfileLayout>) {
        profiles.clear()
        profiles.addAll(list)
        notifyDataSetChanged()
    }

    fun getPosition(item: ProfileLayout): Int = profiles.indexOf(item)

    fun observeOnItemClick(): Observable<ProfileItemSelected> = subject
}