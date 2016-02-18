package com.github.mitchwongho.android.beacon.app

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.github.mitchwongho.android.beacon.R
import com.github.mitchwongho.android.beacon.content.*
import com.jakewharton.rxbinding.widget.changes
import com.jakewharton.rxbinding.widget.checkedChanges
import kotlinx.android.synthetic.main.activity_settings.*
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.BehaviorSubject
import rx.subscriptions.CompositeSubscription

/**
 *
 */
interface UIAction

data class ScanOnPeriodChanged(val value: Int) : UIAction
data class ScanOffPeriodChanged(val value: Int) : UIAction
data class AltBeaconEnabledChanged(val value: Boolean) : UIAction
data class BeaconReceiverEnabledChanged(val value: Boolean) : UIAction
data class RadioRestartIntervalChanged(val value: Int) : UIAction
data class RangingTimeoutChanged(val value: Int) : UIAction

fun SettingsAktivity.Companion.translateScanOn(pos: Int): Int {
    val v: Float = (pos.toFloat() / 100) * (SCAN_ON_MAX - SCAN_ON_MIN)
    return ((SCAN_ON_MIN + v.toInt()) / SCAN_INTERVAL) * SCAN_INTERVAL
}

fun SettingsAktivity.Companion.translateScanOff(pos: Int): Int {
    val v: Float = (pos.toFloat() / 100) * (SCAN_OFF_MAX - SCAN_OFF_MIN)
    return ((SCAN_OFF_MIN + v.toInt()) / SCAN_INTERVAL) * SCAN_INTERVAL
}

fun SettingsAktivity.Companion.translateRadioRestart(pos: Int): Int {
    val v: Float = (pos.toFloat() / 100) * (RADIO_RESTART_MAX - RADIO_RESTART_MIN)
    return ((RADIO_RESTART_MIN + v.toInt()) / RADIO_RESTART_INTERVAL) * RADIO_RESTART_INTERVAL
}

fun SettingsAktivity.Companion.translateRangingTimeout(pos: Int): Int {
    val v: Float = (pos.toFloat() / 100) * (RANGING_MAX - RANGING_MIN)
    return ((RANGING_MIN + v.toInt()) / RANGING_INTERVAL) * RANGING_INTERVAL
}

class SettingsAktivity : AppCompatActivity() {

    val TAG = SettingsAktivity::class.java.simpleName

    lateinit var subject: BehaviorSubject<UIAction>
    lateinit var subscriptions: CompositeSubscription

    companion object {
        val SCAN_ON_MIN = 1000
        val SCAN_ON_MAX = 60000
        val SCAN_INTERVAL = 100
        val SCAN_OFF_MIN = 1000
        val SCAN_OFF_MAX = 60000
        val RADIO_RESTART_INTERVAL = 1
        val RADIO_RESTART_MIN = 1
        val RADIO_RESTART_MAX = 100
        val RANGING_INTERVAL = 1000
        val RANGING_MIN = 1000
        val RANGING_MAX = 120000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        seek_scanon_period.max = 100 // 0~100%
        seek_scanoff_period.max = 100 // 0~100%

        switch_library.isChecked = applyAltBeaconSwitch(prefAltBeaconEnabled())
        seek_scanon_period.progress = applyScanOn(prefScanOnPeriod())
        seek_scanoff_period.progress = applyScanOff(prefScanOffPeriod())
        seek_radio_restart_period.progress = applyRadioRestart(prefRadioRestartInterval())
        seek_ranging_timeout.progress = applyRanging(prefRangingTimeout())

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            viewgroup_setting_mode.visibility = View.GONE
            switch_mode_is_receiver.isChecked = true
        } else {
            viewgroup_setting_mode.visibility = View.VISIBLE
            switch_mode_is_receiver.isChecked = applyBeaconReceiverModeEnabledSwitch(prefBeaconReceiver())
        }

        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setIcon(R.mipmap.ic_launcher)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
    }

    override fun onStart() {
        super.onStart()

        subject = BehaviorSubject.create()
        subscriptions = CompositeSubscription()

        // subscribe to `R.id.seek.scanon_period`
        val sub1 = seek_scanon_period.changes().
                subscribeOn(AndroidSchedulers.mainThread()).
                map { value -> ScanOnPeriodChanged(value) }.
                subscribe(subject)

        subscriptions.add(sub1)

        // subscribe to `R.id.seek_scanoff_period`
        val sub2 = seek_scanoff_period.changes().
                subscribeOn(AndroidSchedulers.mainThread()).
                map { value -> ScanOffPeriodChanged(value) }.
                subscribe(subject)

        subscriptions.add(sub2)

        // subscribe to `R.id.switch_library`
        val sub3 = switch_library.checkedChanges().
                subscribeOn(AndroidSchedulers.mainThread()).
                map { value -> AltBeaconEnabledChanged(value) }.
                subscribe(subject)

        subscriptions.add(sub3)

        // subscribe to `R.id.switch_mode_is_receiver`
        val sub4 = switch_mode_is_receiver.checkedChanges().
                subscribeOn(AndroidSchedulers.mainThread()).
                map { value -> BeaconReceiverEnabledChanged(value) }.
                subscribe(subject)

        subscriptions.add(sub4)

        // subscribe to `R.id.seek_radio_restart_period`
        val sub5 = seek_radio_restart_period.changes().
                subscribeOn(AndroidSchedulers.mainThread()).
                map { value -> RadioRestartIntervalChanged(value) }.
                subscribe(subject)

        subscriptions.add(sub5)

        // subscribe to `R.id.seek_ranging_timeout`
        val sub6 = seek_ranging_timeout.changes().
                subscribeOn(AndroidSchedulers.mainThread()).
                map { value -> RangingTimeoutChanged(value) }.
                subscribe(subject)

        subscriptions.add(sub6)
    }

    override fun onResume() {
        super.onResume()

        val sub = subject?.
                observeOn(AndroidSchedulers.mainThread())?.
                subscribe({
                    //onNext
                    action ->
                    when (action) {
                        is ScanOnPeriodChanged -> {
                            prefScanOnPeriod(action.value)
                            applyScanOn(action.value)
                        }
                        is ScanOffPeriodChanged -> {
                            prefScanOffPeriod(action.value)
                            applyScanOff(action.value)
                        }
                        is AltBeaconEnabledChanged -> {
                            prefAltBeaconEnabled(action.value)
                            applyAltBeaconSwitch(action.value)
                        }
                        is BeaconReceiverEnabledChanged -> {
                            prefBeaconReceiver(action.value)
                            applyBeaconReceiverModeEnabledSwitch(action.value)
                        }
                        is RadioRestartIntervalChanged -> {
                            prefRadioRestartInterval(action.value)
                            applyRadioRestart(action.value)
                        }
                        is RangingTimeoutChanged -> {
                            prefRangingTimeout(action.value)
                            applyRanging(action.value)
                        }
                        else -> Unit
                    }

                }, {
                    throwable ->
                    Log.e(TAG, "onResume::subject ${throwable.message}", throwable)
                }, {
                    //onComplete
                })

        subscriptions?.add(sub)

    }

    override fun onStop() {
        super.onStop()
        subscriptions.clear()
        subscriptions.unsubscribe()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun applyAltBeaconSwitch(value: Boolean): Boolean {
        setting_library_label.text = when (value) {
            true -> getString(R.string.alt_ble_library)
            else -> getString(R.string.std_ble_library)
        }
        return value
    }

    private fun applyBeaconReceiverModeEnabledSwitch(value: Boolean): Boolean {
        setting_mode_label.text = when (value) {
            true -> getString(R.string.ibeacon_receiver)
            else -> getString(R.string.ibeacon_emitter)
        }
        return value
    }

    private fun applyScanOn(pos: Int): Int {
        val scanOn = translateScanOn(pos)
        value_scanon_period.text = "${scanOn}ms"
        return pos
    }

    private fun applyScanOff(pos: Int): Int {
        val scanOff = translateScanOff(pos)
        value_scanoff_period.text = "${scanOff}ms"
        return pos
    }

    private fun applyRadioRestart(pos: Int): Int {
        val restart = translateRadioRestart(pos)
        value_radio_restart_period.text = "${restart}min"
        return pos
    }

    private fun applyRanging(pos: Int): Int {
        val timeout = translateRangingTimeout(pos)
        value_ranging_timeout.text = "${timeout}ms"
        return pos
    }
}
