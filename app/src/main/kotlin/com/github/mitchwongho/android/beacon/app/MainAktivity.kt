package com.github.mitchwongho.android.beacon.app

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.github.mitchwongho.android.beacon.R
import com.github.mitchwongho.android.beacon.bluetooth.rx.LeScanResult
import com.github.mitchwongho.android.beacon.bluetooth.rx.advertise
import com.github.mitchwongho.android.beacon.ext.*
import com.github.mitchwongho.android.beacon.widget.LeScanResultRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import java.util.*
import java.util.concurrent.TimeUnit


/**
 *
 */
class MainAktivity : AppCompatActivity() {

    val TAG = MainAktivity::class.java.simpleName

    val REQUEST_ENABLE_BT: Int = 1

    lateinit var compositeSubscriptions: CompositeSubscription
    lateinit var serviceConnection: ServiceConnection
    var subScanResult: PublishSubject<List<LeScanResult>>?
    val adapter = LeScanResultRecyclerViewAdapter(ArrayList<LeScanResult>(), this)
    val state = HashMap<String, LeScanResult>()

    init {
        subScanResult = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "onCreate")
        setContentView(R.layout.activity_main)
        recyclerview.setHasFixedSize(true)
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = adapter

        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setLogo(R.mipmap.ic_launcher)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        when (btAdapter?.isEnabled) { true -> btAdapter.disable()
        }

    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        compositeSubscriptions = CompositeSubscription()

        // observe BLUETOOTH radio state
        val sub = receiverBluetoothState().
                subscribeOn(Schedulers.trampoline()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe({
                    // OnNext
                    state ->
                    when (state.state) {
                        BluetoothAdapter.STATE_ON -> {
                            Unit
                        }
                        BluetoothAdapter.STATE_OFF -> {
                            btAdapter.enable()
                        }
                    }
                }, {
                    //onError
                    throwable ->
                })

        compositeSubscriptions.add(sub)

        if (!checkBluetoothAdapter()) {
            return
        }

        val isLeReceiver = prefBeaconReceiver()
        Log.d(TAG, "onResume(){asLEReceiver=${isLeReceiver}}")

        if (isLeReceiver) {
            onResumeAsBeaconReceiver(btAdapter)
        } else {
            onResumeAsBeaconEmitter(btAdapter)
        }
    }

    /**
     * onResume for Beacon Emitter mode
     * @param btAdapter
     * @see BluetoothAdapter
     */
    private fun onResumeAsBeaconEmitter(btAdapter: BluetoothAdapter) {
        val sub = btAdapter.advertise().
                subscribeOn(Schedulers.trampoline()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe({
                    //OnNext
                    event ->
                    Log.d(TAG, "onLEBeaconAdvertiseStarted")
                }, {
                    //onError
                    throwable ->
                    Log.e(TAG, "Error starting LE Beacon Advertising: ${throwable.message}", throwable)
                }, {
                    //OnCompleted
                    Log.d(TAG, "LE Beacon Advertising completed")
                })
        compositeSubscriptions.add(sub)
    }

    /**
     * onResume for Beacon Receiver mode
     */
    private fun onResumeAsBeaconReceiver(btAdapter: BluetoothAdapter) {
        state.clear()
        adapter.applyList(emptyList(), FilterType.RSSI)

        val scanOn: Int = SettingsAktivity.Companion.translateScanOn(prefScanOnPeriod())
        val scanOff: Int = SettingsAktivity.Companion.translateScanOff(prefScanOffPeriod())
        val rangingTimeout: Int = SettingsAktivity.Companion.translateRangingTimeout(prefRangingTimeout())
        val radioRestartInterval: Int = SettingsAktivity.Companion.translateRadioRestart(prefRadioRestartInterval())

        subScanResult = PublishSubject.create()
        val sub0 = subScanResult?.
                subscribeOn(Schedulers.trampoline())?.
                observeOn(AndroidSchedulers.mainThread())?.
                subscribe ({ res ->
                    val mapped: Map<String, LeScanResult> = res.associateBy { it -> it.deviceAddress }
                    synchronized(state) {
                        mapped.forEach { e ->
                            e.value.oob = state.get(e.key)?.oob ?: 0
                        }
                        state.putAll(mapped)
                        val now = System.currentTimeMillis()
                        state.forEach { e ->
                            val oldAge = e.value.age
                            e.value.age = now - e.value.timestamp
                            if (oldAge < rangingTimeout && e.value.age >= rangingTimeout) e.value.oob += 1
                        }
                        //                    }
                        adapter.applyList(state.values.toList(), FilterType.RSSI)
                    }
                }, { throwable ->
                    Log.e(TAG, "ReplaySubject onError ${throwable.message}", throwable)
                })
        compositeSubscriptions.add(sub0)

        BeaconManager.setAndroidLScanningDisabled(true)
        BeaconManager.setUseTrackingCache(false)

        val sub1 = this.rxBindAltBeaconManager(scanOn.toLong(), scanOff.toLong()).
                flatMap { conn ->
                    Log.d(TAG, "onAltBeacon::Connected")
                    when (conn) {
                        is AltBeaconServiceConnectEvent -> {
                            conn.beaconManager.rxNotifyInRange()
                        }
                        else -> Observable.error<RangeBeaconsInRegion>(UnsupportedOperationException("Expected 'AltBeaconServiceConnectEvent'"))
                    }
                }.
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                map {
                    // transform the List<Beacon> into a List<LeScanResult>
                    it ->
                    it.beacons.map<Beacon, LeScanResult> { it ->
                        LeScanResult(it.bluetoothName, it.bluetoothAddress, it.rssi, emptyArray(), System.currentTimeMillis(), 0, 0)
                    }
                }.
                subscribe({
                    //onNext
                    results ->
                    Log.e(TAG, "onAltBeacon in range ${results.size}")
                    subScanResult?.onNext(results)
                }, {
                    //onError
                    throwable ->
                    Log.e(TAG, "onAltBeacon error ${throwable.message}", throwable)
                }, {
                    //onComplete
                    Log.e(TAG, "onAltBeacon completed")
                })

        compositeSubscriptions.add(sub1)

        val sub2 = Observable.interval(radioRestartInterval.toLong(), TimeUnit.MINUTES).
                subscribeOn(Schedulers.trampoline()).
                observeOn(Schedulers.io()).
                subscribe { aLong ->
                    btAdapter.disable()
                }

        compositeSubscriptions.add(sub2)
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause")
        compositeSubscriptions.unsubscribe()
        compositeSubscriptions.clear()
        subScanResult?.onCompleted()
        synchronized(state) {
            state.clear()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        getMenuInflater().inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.menu_item_settings -> {
                val intent = Intent(this, SettingsAktivity::class.java)
                startActivity(intent)
            }
            R.id.menu_item_clear -> {
                synchronized(state) {
                    state.clear()
                    adapter.applyList(emptyList(), FilterType.RSSI)
                }

            }
            R.id.menu_item_profiles -> {
                val intent = Intent(this, ProfileLayoutsAktivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    private fun checkBluetoothAdapter(): Boolean {
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter == null) {
            //not available
            return false
        } else if (btAdapter.isEnabled) {
            // radio is enabled...continue
        } else {
            // ask permission to enable BLUETOOTH radio
            //TODO create an Rx wrapper for this Snackbar
            val snackbar = Snackbar.make(activity_main, R.string.bluetooth_disabled, Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction(R.string.turn_on, {
                view ->
                val btIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(btIntent, REQUEST_ENABLE_BT)
            })
            snackbar.show()
            return false
        }
        return true
    }

}
