package com.github.mitchwongho.android.beacon.app

import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.github.mitchwongho.android.beacon.R
import com.github.mitchwongho.android.beacon.bluetooth.rx.LeScanResult
import com.github.mitchwongho.android.beacon.ext.ServiceConnectedEvent
import com.github.mitchwongho.android.beacon.ext.ServiceDisconnectedEvent
import com.github.mitchwongho.android.beacon.ext.rxBindService
import com.github.mitchwongho.android.beacon.service.BeaconService
import com.github.mitchwongho.android.beacon.service.BeaconServiceBinder
import com.github.mitchwongho.android.beacon.widget.SimpleAdapter
import kotlinx.android.synthetic.main.activity_main.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.ReplaySubject
import rx.subscriptions.CompositeSubscription
import java.util.*


/**
 *
 */
public class MainAktivity : AppCompatActivity() {

    val TAG = MainAktivity::class.java.simpleName

    var compositeSubscriptions: CompositeSubscription?
    var serviceConnection: ServiceConnection?
    var subScanResult: ReplaySubject<List<LeScanResult>>?
    val adapter = SimpleAdapter(ArrayList<LeScanResult>(), this)
    val state = HashMap<String, LeScanResult>()

    init {
        compositeSubscriptions = null
        serviceConnection = null
        subScanResult = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_recyclerview.setHasFixedSize(true)
        main_recyclerview.layoutManager = LinearLayoutManager(this)
        main_recyclerview.adapter = adapter

        supportActionBar.setDisplayUseLogoEnabled(true)
        supportActionBar.setLogo(R.mipmap.ic_launcher)
        supportActionBar.setDisplayShowHomeEnabled(true)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()

        compositeSubscriptions = CompositeSubscription()

        subScanResult = ReplaySubject.create()
        val sub0 = subScanResult?.
                subscribeOn(Schedulers.trampoline())?.
                observeOn(AndroidSchedulers.mainThread())?.
                subscribe ({ res ->
                    val mapped = res.toMapBy { it -> it.bluetoothDevice.address }
                    mapped.forEach { e ->
                        e.value.oob = state.get(e.key)?.oob ?: 0
                    }
                    state.putAll(mapped)
                    val now = System.currentTimeMillis()
                    state.forEach { e ->
                        val oldAge = e.value.age
                        e.value.age = now - e.value.timestamp
                        if (oldAge < 60000 && e.value.age >= 60000) e.value.oob += 1
                    }
                    adapter.applyList(state.values.toList())
                }, { throwable ->
                    Log.e(TAG, "ReplaySubject onError ${throwable.message}", throwable)
                })
        compositeSubscriptions?.add(sub0)

        val service = Intent(this, BeaconService::class.java)

        val sub = rxBindService(service, BIND_AUTO_CREATE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
            //onNext
            event ->
            (
                    when (event) {
                        is ServiceConnectedEvent -> {
                            Log.d(TAG, "onNext: Service ${event.name.toString()} connected")
                            serviceConnection = event.conn
                            val sub = (event.service as BeaconServiceBinder).startLeScan(interval = 8 * 1000)
                                    ?.observeOn(Schedulers.io())
                                    ?.subscribe({
                                        // onNext
                                        res ->
                                        Log.d(TAG, "onNext {devices=${res.size}}")
                                        subScanResult?.onNext(res)
                                    }, {
                                        //onError
                                        throwable ->
                                        Log.e(TAG, "onError ${throwable.message}")
                                    }, {
                                        //onComplete
                                        Log.d(TAG, "onComplete")
                                    });
                            compositeSubscriptions?.add(sub)
                        }
                        is ServiceDisconnectedEvent -> {
                            Log.w(TAG, "onNext: Service ${event.name} disconnected")
                        }
                    }
                    )
        }, {
            //onError
            throwable ->
            Log.e(TAG, "onError: BeaconService '${throwable.message}'", throwable)
        }, {
            //onComplete
            Log.d(TAG, "onComplete")
        })

        compositeSubscriptions?.add(sub)
    }

    override fun onPause() {
        super.onPause()
        compositeSubscriptions?.unsubscribe()
        compositeSubscriptions?.clear()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
