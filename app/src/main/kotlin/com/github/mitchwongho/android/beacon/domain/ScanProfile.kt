package com.github.mitchwongho.android.beacon.domain

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 *
 */
open class ScanProfile(@PrimaryKey open var uuid: String = UUID.randomUUID().toString(),
                       open var testDuration: Int = 0,
                       open var scanOnPeriod: Int = 0,
                       open var scanOffPeriod: Int = 0,
                       open var radioRestartInterval: Int = 0,
                       open var rangingTimeout: Int = 0) : RealmObject() {}