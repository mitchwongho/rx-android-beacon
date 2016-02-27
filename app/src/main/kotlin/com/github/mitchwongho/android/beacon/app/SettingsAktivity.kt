package com.github.mitchwongho.android.beacon.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.github.mitchwongho.android.beacon.R
import com.github.mitchwongho.android.beacon.content.*
import com.github.mitchwongho.android.beacon.domain.ScanProfile
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.widget.changes
import kotlinx.android.synthetic.main.activity_settings.*
import org.altbeacon.beacon.BeaconManager
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.BehaviorSubject
import rx.subscriptions.CompositeSubscription

/**
 *
 */

fun SettingsAktivity.Companion.translatePositionScanOn(pos: Int): Int {
    val v: Float = (pos.toFloat() / 100) * (SCAN_ON_MAX - SCAN_ON_MIN)
    return ((SCAN_ON_MIN + v.toInt()) / SCAN_INTERVAL) * SCAN_INTERVAL
}

fun SettingsAktivity.Companion.translateValueScanOn(value: Int): Int {
    val p: Float = (value.toFloat() / (SCAN_ON_MAX - SCAN_ON_MIN)) * 100
    return p.toInt()
}

fun SettingsAktivity.Companion.translatePositionScanOff(pos: Int): Int {
    val v: Float = (pos.toFloat() / 100) * (SCAN_OFF_MAX - SCAN_OFF_MIN)
    return ((SCAN_OFF_MIN + v.toInt()) / SCAN_INTERVAL) * SCAN_INTERVAL
}

fun SettingsAktivity.Companion.translateValueScanOff(value: Int): Int {
    val p: Float = (value.toFloat() / (SCAN_OFF_MAX - SCAN_OFF_MIN)) * 100
    return p.toInt()
}

fun SettingsAktivity.Companion.translatePositionRadioRestart(pos: Int): Int {
    val v: Float = (pos.toFloat() / 100) * (RADIO_RESTART_MAX - RADIO_RESTART_MIN)
    return ((RADIO_RESTART_MIN + v.toInt()) / RADIO_RESTART_INTERVAL) * RADIO_RESTART_INTERVAL
}

fun SettingsAktivity.Companion.translateValueRadioRestart(value: Int): Int {
    val p: Float = (value.toFloat() / (RADIO_RESTART_MAX - RADIO_RESTART_MIN)) * 100
    return p.toInt()
}

fun SettingsAktivity.Companion.translatePositionRangingTimeout(pos: Int): Int {
    val v: Float = (pos.toFloat() / 100) * (RANGING_MAX - RANGING_MIN)
    return ((RANGING_MIN + v.toInt()) / RANGING_INTERVAL) * RANGING_INTERVAL
}

fun SettingsAktivity.Companion.translateValueRangingTimeout(value: Int): Int {
    val p: Float = (value.toFloat() / (RANGING_MAX - RANGING_MIN)) * 100
    return p.toInt()
}

class SettingsAktivity(var profile: ScanProfile = ScanProfile()) : AppCompatActivity() {

    val TAG = SettingsAktivity::class.java.simpleName

    lateinit var reducer: BehaviorSubject<UIAction>
    lateinit var subscriptions: CompositeSubscription

    companion object {
        val SCAN_ON_MIN = 1000
        val SCAN_ON_MAX = 60000
        val SCAN_INTERVAL = 100
        val SCAN_OFF_MIN = 0
        val SCAN_OFF_MAX = 60000
        val RADIO_RESTART_INTERVAL = 1
        val RADIO_RESTART_MIN = 1 //minutes
        val RADIO_RESTART_MAX = 100
        val RADIO_RESTART_DEFAULT = 30
        val RANGING_INTERVAL = 1000
        val RANGING_MIN = 1000 //milliseconds
        val RANGING_MAX = 120000
        val RANGING_DEFAULT = 60000
    }

    interface UIAction

    class BackMenuItemClicked : UIAction
    class DeleteMenuItemClicked : UIAction
    class FABClicked : UIAction
    class ScanOnPeriodChanged(val value: Int) : UIAction
    class ScanOffPeriodChanged(val value: Int) : UIAction
    class RadioRestartIntervalChanged(val value: Int) : UIAction
    class RangingTimeoutChanged(val value: Int) : UIAction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        seek_scanon_period.max = 100 // 0~100%
        seek_scanoff_period.max = 100 // 0~100%
        seek_radio_restart_period.max = 100
        seek_ranging_timeout.max = 100

        seek_scanon_period.progress = applyScanOnPosition(prefScanOnPeriod())
        seek_scanoff_period.progress = applyScanOffPosition(prefScanOffPeriod())
        seek_radio_restart_period.progress = applyRadioRestartPosition(prefRadioRestartInterval())
        seek_ranging_timeout.progress = applyRangingPosition(prefRangingTimeout())

        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setIcon(R.mipmap.ic_launcher)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
    }

    override fun onStart() {
        super.onStart()

        reducer = BehaviorSubject.create()
        subscriptions = CompositeSubscription()

        if (intent.hasExtra("ScanProfileId")) {
            val uuid = intent.getStringExtra("ScanProfileId")
            val sub8 = fetchDO(ScanProfile::class.java, uuid).
                    observeOn(AndroidSchedulers.mainThread()).
                    subscribe({
                        //onNExt
                        r ->
                        if (r.isNotEmpty()) {
                            profile = cloneRealmObject(r.first()) //NB: to get a Realm-detached Object
                            applyScanOnPosition(profile.scanOnPeriod)
                            seek_scanon_period.progress = profile.scanOnPeriod
                            applyScanOffPosition(profile.scanOffPeriod)
                            seek_scanoff_period.progress = profile.scanOffPeriod
                            applyRadioRestartPosition(profile.radioRestartInterval)
                            seek_radio_restart_period.progress = profile.radioRestartInterval
                            applyRangingPosition(profile.rangingTimeout)
                            seek_ranging_timeout.progress = profile.rangingTimeout
                        } else {
                            // TODO indicate empty list
                        }
                    }, {
                        //onError
                        throwable ->
                        e("Error fetching ScanProfile {uuid=${uuid}}", throwable)
                    }, {
                        //onCompleted
                    })
            subscriptions.add(sub8)
        } else {
            applyScanOnValue(BeaconManager.DEFAULT_FOREGROUND_SCAN_PERIOD.toInt())
            applyScanOffValue(BeaconManager.DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD.toInt())
            applyRadioRestartValue(RADIO_RESTART_DEFAULT)
            applyRangingValue(RANGING_DEFAULT)
        }

        // subscribe to `R.id.seek.scanon_period`
        val sub1 = seek_scanon_period.changes().
                subscribeOn(AndroidSchedulers.mainThread()).
                map { value -> ScanOnPeriodChanged(value) }.
                subscribe(reducer)

        subscriptions.add(sub1)

        // subscribe to `R.id.seek_scanoff_period`
        val sub2 = seek_scanoff_period.changes().
                subscribeOn(AndroidSchedulers.mainThread()).
                map { value -> ScanOffPeriodChanged(value) }.
                subscribe(reducer)

        subscriptions.add(sub2)

        // subscribe to `R.id.seek_radio_restart_period`
        val sub5 = seek_radio_restart_period.changes().
                subscribeOn(AndroidSchedulers.mainThread()).
                map { value -> RadioRestartIntervalChanged(value) }.
                subscribe(reducer)

        subscriptions.add(sub5)

        // subscribe to `R.id.seek_ranging_timeout`
        val sub6 = seek_ranging_timeout.changes().
                subscribeOn(AndroidSchedulers.mainThread()).
                map { value -> RangingTimeoutChanged(value) }.
                subscribe(reducer)

        subscriptions.add(sub6)

        val sub7 = fab.clicks().
                flatMap { u -> insertOrUpdate(profile) }.
                map { r -> FABClicked() }.
                subscribe(reducer)
        subscriptions.add(sub7)


    }

    override fun onResume() {
        super.onResume()
        d("onResume")
        val sub = reducer.
                observeOn(AndroidSchedulers.mainThread())?.
                subscribe({
                    //onNext
                    action ->
                    when (action) {
                        is BackMenuItemClicked -> {
                            finish()
                        }
                        is FABClicked -> {
                            finish()
                        }
                        is DeleteMenuItemClicked -> {
                            finish()
                        }
                        is ScanOnPeriodChanged -> {
                            profile.scanOnPeriod = action.value
                            applyScanOnPosition(action.value)
                        }
                        is ScanOffPeriodChanged -> {
                            profile.scanOffPeriod = action.value
                            applyScanOffPosition(action.value)
                        }
                        is RadioRestartIntervalChanged -> {
                            profile.radioRestartInterval = action.value
                            applyRadioRestartPosition(action.value)
                        }
                        is RangingTimeoutChanged -> {
                            profile.rangingTimeout = action.value
                            applyRangingPosition(action.value)
                        }
                        else -> Unit
                    }

                }, {
                    throwable ->
                    Log.e(TAG, "onResume::subject ${throwable.message}", throwable)
                }, {
                    //onComplete
                })

        subscriptions.add(sub)

    }

    override fun onStop() {
        super.onStop()
        reducer.onCompleted()
        subscriptions.clear()
        subscriptions.unsubscribe()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.scan_profile_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_trash).setVisible(intent.hasExtra("ScanProfileId"))
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                reducer.onNext(BackMenuItemClicked())
                return false
            }
            R.id.menu_trash -> {
                val sub = deleteRealmObject(ScanProfile::class.java, profile.uuid).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe({
                            //onNext
                            it ->
                            e("onNext: Successfully deleted RealmObject")
                        }, {
                            //onError
                            throwable ->
                            e("Error deleting RealmObject ${throwable.message}", throwable)
                        }, {
                            //onCompleted
                            e("onCompleted: Successfully deleted RealmObject")
                            reducer.onNext(DeleteMenuItemClicked())
                        })
                subscriptions.add(sub)
//                reducer.onNext(DeleteMenuItemClicked())
                return false
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun applyScanOnPosition(pos: Int): Int {
        val scanOn = translatePositionScanOn(pos)
        value_scanon_period.text = "${scanOn}ms"
        return pos
    }

    private fun applyScanOnValue(value: Int): Int {
        val pos = translateValueScanOn(value)
        seek_scanon_period.progress = pos
        return value
    }

    private fun applyScanOffPosition(pos: Int): Int {
        val scanOff = translatePositionScanOff(pos)
        value_scanoff_period.text = "${scanOff}ms"
        return pos
    }

    private fun applyScanOffValue(value: Int): Int {
        val pos = translateValueScanOff(value)
        seek_scanoff_period.progress = pos
        return value
    }

    private fun applyRadioRestartPosition(pos: Int): Int {
        val restart = translatePositionRadioRestart(pos)
        value_radio_restart_period.text = "${restart}min"
        return pos
    }

    private fun applyRadioRestartValue(value: Int): Int {
        val pos = translateValueRadioRestart(value)
        seek_radio_restart_period.progress = pos
        return value
    }

    private fun applyRangingPosition(pos: Int): Int {
        val timeout = translatePositionRangingTimeout(pos)
        value_ranging_timeout.text = "${timeout}ms"
        return pos
    }

    private fun applyRangingValue(value: Int): Int {
        val pos = translateValueRangingTimeout(value)
        seek_ranging_timeout.progress = pos
        return value
    }
}
