package com.github.mitchwongho.android.beacon.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager

/**
 *
 */
class HorizontalLinearLayoutManager(val context: Context) : LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false){

    var canScrollHorizontally = true

    override fun canScrollHorizontally(): Boolean {
        return canScrollHorizontally
    }

    fun canScrollHorizontally( can: Boolean ) {
        canScrollHorizontally = can
    }
}