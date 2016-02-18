package com.github.mitchwongho.android.beacon.domain

import io.realm.RealmObject

/**
 *
 */
open class ScanProfile(open var scanOnPeriod: Int = 0,
                       open var scanOffPeriod: Int = 0,
                       open var radioRestartInterval: Int = 0,
                       open var rangingTimeout: Int = 0) : RealmObject() {}