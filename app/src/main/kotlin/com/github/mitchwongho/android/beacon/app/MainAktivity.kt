package com.github.mitchwongho.android.beacon.app

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.github.mitchwongho.android.beacon.R
import com.github.mitchwongho.android.beacon.bluetooth.rx.advertise
import com.github.mitchwongho.android.beacon.content.*
import com.github.mitchwongho.android.beacon.domain.ScanProfile
import com.github.mitchwongho.android.beacon.ext.Event
import com.github.mitchwongho.android.beacon.ext.RangeBeaconsInRegion
import com.github.mitchwongho.android.beacon.ext.rxNotifyInRange
import com.github.mitchwongho.android.beacon.widget.HorizontalLinearLayoutManager
import com.github.mitchwongho.android.beacon.widget.ScanProfileRecyclerViewAdapter
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent
import com.jakewharton.rxbinding.support.v7.widget.scrollEvents
import com.jakewharton.rxbinding.support.v7.widget.scrollStateChanges
import com.jakewharton.rxbinding.view.RxView
import kotlinx.android.synthetic.main.activity_main.*
import org.altbeacon.beacon.Beacon
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.TimeUnit


/**
 *
 */
class MainAktivity : AppCompatActivity() {

    val TAG = MainAktivity::class.java.simpleName

    val REQUEST_ENABLE_BT: Int = 1

    lateinit var compositeSubscriptions: CompositeSubscription
    lateinit var serviceConnection: ServiceConnection
    lateinit var reducer: PublishSubject<Event>

    val btAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    //    var subScanResult: PublishSubject<List<LeScanResult>>?
    //    val adapter = LeScanResultRecyclerViewAdapter(ArrayList<LeScanResult>(), this)
    val recyclerAdapter = ScanProfileRecyclerViewAdapter(mutableListOf(), this)

    val periodFormatter = PeriodFormatterBuilder().
            printZeroAlways().
            minimumPrintedDigits(1).
            appendHours().
            appendSeparator(":").
            minimumPrintedDigits(2).
            appendMinutes().
            appendSeparator(":").
            appendSeconds().
            toFormatter()

    data class RangedBeacon(val beacon: Beacon, val lastUpdate: DateTime = DateTime.now()) {
        override fun equals(other: Any?): Boolean {
            return other is RangedBeacon && other.beacon.bluetoothAddress!!.equals(this.beacon.bluetoothAddress)
        }

        override fun hashCode(): Int {
            return this.beacon.bluetoothAddress.hashCode() //perhaps not the best option
        }
    }

    class OnBeaconsInRange(val beacons: List<RangedBeacon>) : Event
    class OnScanProfilesFetched(val profiles: List<ScanProfile>) : Event
    class OnFabClicked(val state: Int) : Event
    class OnTimerIntervalUpdated(val startTime: DateTime = DateTime.now(), val interval: Long) : Event
    class OnScrollEvent(val state: Int, val dx: Int, val dy: Int) : Event
    class OnBluetoothStateChanged(val state: Int) : Event
    class State(val selectedProfile: ScanProfile? = null,
                val fabState: OnFabClicked? = null,
                val timerIntervalState: OnTimerIntervalUpdated? = null,
                val scrollEventState: OnScrollEvent? = null,
                val btState: OnBluetoothStateChanged? = null,
                val intervalSubscription: Subscription? = null,
                val beaconBindSubscription: Subscription? = null,
                val rangedBeacons: List<RangedBeacon> = emptyList()) : Event


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "onCreate")
        setContentView(R.layout.activity_main)
        recyclerview.setHasFixedSize(true)
        recyclerview.layoutManager = HorizontalLinearLayoutManager(this)
        recyclerview.layoutManager.isAutoMeasureEnabled = true
        recyclerview.adapter = recyclerAdapter

        fab.backgroundTintList = resources.getColorStateList(R.color.main_fab_ready_colour)
        fab.tag = 0

        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setLogo(R.mipmap.ic_launcher)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")
        reducer = PublishSubject.create()
        compositeSubscriptions = CompositeSubscription()

        btAdapter.disable()

        // observe BLUETOOTH radio state
//        val sub = receiverBluetoothState().
//                map { e -> OnBluetoothStateChanged(e.state) }.
//                subscribe(reducer)
//
//        compositeSubscriptions.add(sub)


        val sub1 = reducer.observeOn(AndroidSchedulers.mainThread()).
                startWith(  State() ). //initialise state
                scan { acc: Event?, event: Event ->
                    val state = if (acc is State) acc else State()
                    when (event) {
                        is OnBluetoothStateChanged -> {
                            when (event.state) {
                                BluetoothAdapter.STATE_ON -> {
                                    Unit
                                }
                                BluetoothAdapter.STATE_OFF -> {
                                    btAdapter.enable()
                                    reducer.onNext(OnFabClicked(1)) ///Mmmmm...this mights create an error in the state
                                }
                            }
                            State(selectedProfile = state?.selectedProfile,
                                    btState = event,
                                    fabState = state?.fabState,
                                    timerIntervalState = state?.timerIntervalState,
                                    scrollEventState = state?.scrollEventState)
                        }
                        is OnScrollEvent -> {

                            when (event.state) {
                                RecyclerView.SCROLL_STATE_SETTLING -> {
                                    val layoutManager = recyclerview.layoutManager as LinearLayoutManager
                                    val firstVisible = layoutManager.findFirstVisibleItemPosition()
                                    val firstCompletlyVisible = layoutManager.findFirstCompletelyVisibleItemPosition()
                                    val lastVisible = layoutManager.findLastVisibleItemPosition()
                                    val lastCompletlyVisible = layoutManager.findLastCompletelyVisibleItemPosition()
                                    //                            d("Scroll Event {first=${firstVisible},firstComplete=${firstCompletlyVisible}}")
                                    //                            d("Scroll Event {last=${lastVisible},lastComplete=${lastCompletlyVisible}}")
                                    if (event.dx > 0 && (firstVisible != lastVisible)) {
                                        // scroll right
                                        recyclerview.smoothScrollToPosition(lastVisible)
                                        val profile = recyclerAdapter.getItemAtPosition(lastVisible)
                                        val duration = SettingsAktivity.translatePositionTestDuration(profile.testDuration)
                                        val startTime = state.timerIntervalState?.startTime ?: DateTime.now()
                                        setClockText( startTime, duration )
                                        //
                                        State(selectedProfile = profile,
                                                btState = state?.btState,
                                                fabState = state?.fabState,
                                                timerIntervalState = state?.timerIntervalState,
                                                scrollEventState = event)
                                    } else if (event.dx < 0 && (firstVisible != lastVisible)) {
                                        // scroll left
                                        recyclerview.smoothScrollToPosition(firstVisible)
                                        val profile = recyclerAdapter.getItemAtPosition(firstVisible)
                                        val duration = SettingsAktivity.translatePositionTestDuration(profile.testDuration)
                                        val startTime = state.timerIntervalState?.startTime ?: DateTime.now()
                                        setClockText( startTime, duration )
                                        //
                                        State(selectedProfile = profile,
                                                btState = state?.btState,
                                                fabState = state?.fabState,
                                                timerIntervalState = state?.timerIntervalState,
                                                scrollEventState = event)
                                    } else {
                                        acc
                                    }
                                }
                                else -> acc
                            }
                        }
                        is OnFabClicked -> {
                            var subInterval = state.intervalSubscription
                            var subBeaconBind = state.beaconBindSubscription
                            var rangedBeacons = state.rangedBeacons
                            when (event.state) {
                                0 -> {
                                    // ready -> running
                                    btAdapter.enable()
                                    rangedBeacons = emptyList()
                                    fab.backgroundTintList = resources.getColorStateList(R.color.main_fab_running_colour)
                                    fab.setImageDrawable(resources.getDrawable(R.mipmap.ic_stop_white_36dp))
                                    (recyclerview.layoutManager as HorizontalLinearLayoutManager).canScrollHorizontally(false)
                                    // start Interval Timer
                                    subInterval = Observable.combineLatest(
                                            Observable.just(DateTime.now()),
                                            Observable.interval(1, TimeUnit.SECONDS).startWith(0L),
                                            {
                                                a, b ->
                                                OnTimerIntervalUpdated(a, b)
                                            }).
                                            observeOn(Schedulers.computation()).
                                            subscribe(reducer)

                                    compositeSubscriptions.add(subInterval)
                                    //
                                    // TODO start scanning
                                    val scanOn = SettingsAktivity.translatePositionScanOn(state.selectedProfile?.scanOnPeriod ?: 0)
                                    val scanOff = SettingsAktivity.translatePositionScanOff(state.selectedProfile?.scanOffPeriod ?: 0)
                                    subBeaconBind = this@MainAktivity.
                                            rxBindAltBeaconManager(scanOn.toLong(), scanOff.toLong()).
                                            flatMap { it ->
                                                when(it) {
                                                    is AltBeaconServiceConnectEvent -> it.beaconManager.rxNotifyInRange()
                                                    else -> Observable.error<RangeBeaconsInRegion>(UnsupportedOperationException("Expected 'AltBeaconServiceConnectEvent'"))
                                                }
                                            }.
                                            flatMap { it ->
                                                Observable.from(it.beacons)
                                            }.
                                            map { it -> RangedBeacon(it) }.
                                            buffer(1, TimeUnit.SECONDS).
//                                            filter { it -> it.isNotEmpty() }.
                                            map { it -> OnBeaconsInRange(it) }.
                                            subscribe(reducer)
                                    compositeSubscriptions.add(subBeaconBind)
                                    //
                                }
                                1 -> {
                                    // running -> stopped
                                    clock.setTextColor(resources.getColor(R.color.fab_stop))
                                    fab.backgroundTintList = resources.getColorStateList(R.color.main_fab_stopped_colour)
                                    fab.setImageDrawable(resources.getDrawable(R.mipmap.ic_replay_white_36dp))
                                    subInterval?.unsubscribe()
                                    compositeSubscriptions.remove(subInterval)
                                    subBeaconBind?.unsubscribe()
                                    compositeSubscriptions.remove(subBeaconBind)
                                }
                                2 -> {
                                    //stopped -> ready
                                    btAdapter.disable()
                                    clock.setTextColor(resources.getColor(android.R.color.primary_text_light))
                                    fab.backgroundTintList = resources.getColorStateList(R.color.main_fab_ready_colour)
                                    fab.setImageDrawable(resources.getDrawable(R.mipmap.ic_play_arrow_white_36dp))
                                    val duration = SettingsAktivity.translatePositionTestDuration(state.selectedProfile?.testDuration ?: 0)
                                    val startTime = state.timerIntervalState?.startTime ?: DateTime.now()
                                    setClockText(startTime, duration)
                                    (recyclerview.layoutManager as HorizontalLinearLayoutManager).canScrollHorizontally(true)
                                }
                            }
                            fab.tag = (event.state + 1) % 3
                            //
                            State(selectedProfile = state?.selectedProfile,
                                    btState = state?.btState,
                                    fabState = event,
                                    timerIntervalState = state?.timerIntervalState,
                                    scrollEventState = state?.scrollEventState,
                                    intervalSubscription = subInterval,
                                    beaconBindSubscription = subBeaconBind,
                                    rangedBeacons = rangedBeacons)
                        }
                        is OnTimerIntervalUpdated -> {
                            val start = event.startTime
                            val duration = SettingsAktivity.translatePositionTestDuration(state.selectedProfile?.testDuration ?: 0)
                            val end = start.plusMinutes(duration)
                            val now = DateTime.now()
                            val p = Period(now, end)
                            clock.text = p.toString(periodFormatter)
                            if (now.isEqual(end) || now.isAfter(end)) {
                              reducer.onNext(OnFabClicked(1))
                            }
                            //
                            State(selectedProfile = state?.selectedProfile,
                                    btState = state?.btState,
                                    fabState = state?.fabState,
                                    timerIntervalState = event,
                                    scrollEventState = state?.scrollEventState,
                                    intervalSubscription = state?.intervalSubscription,
                                    beaconBindSubscription = state?.beaconBindSubscription,
                                    rangedBeacons = state?.rangedBeacons)
                        }
                        is OnScanProfilesFetched -> {
                            val profiles = event.profiles
                            recyclerAdapter.updateDataSet(profiles)
                            if (profiles.isNotEmpty()) {
                                val selectedProfile = profiles.first()
                                val duration = SettingsAktivity.translatePositionTestDuration(selectedProfile.testDuration)
                                setClockText(DateTime.now(), duration)
                                State(selectedProfile = selectedProfile,
                                        btState = state?.btState,
                                        fabState = state?.fabState,
                                        timerIntervalState = state?.timerIntervalState,
                                        scrollEventState = state?.scrollEventState)
                            } else {
                                acc
                            }
                        }
                        is OnBeaconsInRange -> {
                            d("Ranged ${event.beacons.size}")
                            num_ranged_beacons.text = "${event.beacons.size}/${state.rangedBeacons.size}"
                            for (beacon: RangedBeacon in event.beacons) {
                                val contains = state?.rangedBeacons.contains(beacon)
                                d("Ranged {contains=${contains}}")
                                if (contains) {
                                    val rangingTimeout = SettingsAktivity.translatePositionRangingTimeout(state.selectedProfile?.rangingTimeout ?: 0)
                                    val lastSeen = state?.rangedBeacons[state?.rangedBeacons.indexOf(beacon)].lastUpdate
                                    val now = DateTime.now()
                                    val duration = Duration(lastSeen, now)
                                    d("Ranged beacon {beacon=${beacon.beacon.bluetoothAddress},last=${duration.millis},timeout=${rangingTimeout}}")
                                    if (duration.millis > rangingTimeout) {
                                        e("Ranged beacon '${beacon.beacon.bluetoothAddress}' exceeded timeout {last=${lastSeen}}")
                                    }
                                }
                            }
                            val listBeacons: MutableList<RangedBeacon> = mutableListOf()
                            listBeacons.addAll(event.beacons)
                            listBeacons.addAll(state.rangedBeacons)
                            State(selectedProfile = state?.selectedProfile,
                                    btState = state?.btState,
                                    fabState = state?.fabState,
                                    timerIntervalState = state?.timerIntervalState,
                                    scrollEventState = state?.scrollEventState,
                                    intervalSubscription = state?.intervalSubscription,
                                    beaconBindSubscription = state?.beaconBindSubscription,
                                    rangedBeacons = listBeacons.toSet().toList())
                        }
                        else -> acc
                    }
                }.
                subscribe({
                    //onNext
                    acc ->
                    d("reducer::onNext")
                }, {
                    //onError
                    throwable ->
                    e("ERROR handing event", throwable)
                }, {
                    //onCompleted
                    d("reducer::onCompleted")
                })
        compositeSubscriptions.add(sub1)

//        if (!checkBluetoothAdapter()) {
//            return
//        }

        val isLeReceiver = true

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
        //
        // Observe RecyclerView scroll events
        val sub = Observable.combineLatest<Int, RecyclerViewScrollEvent, OnScrollEvent>(
                recyclerview.scrollStateChanges(),
                recyclerview.scrollEvents(),
                { state, event ->
                    OnScrollEvent(state, event.dx(), event.dy())
                }).subscribe(reducer)

        compositeSubscriptions.add(sub)
        //
        // Observe FAB clicks
        val sub1 = RxView.clicks(fab).
                map { e -> OnFabClicked(fab.tag as Int) }.
                subscribe(reducer)
        compositeSubscriptions.add(sub1)
        //
        // Observe RecyclerView data
        val sub2 = fetchDO(ScanProfile::class.java).
                map { it -> OnScanProfilesFetched(it) }.
                subscribe(reducer)
        compositeSubscriptions.add(sub2)
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause")
        //        subScanResult?.onCompleted()
        reducer.onCompleted()
        compositeSubscriptions.unsubscribe()
        compositeSubscriptions.clear()
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
            R.id.menu_item_profiles -> {
                val intent = Intent(this, ProfileLayoutsAktivity::class.java)
                startActivity(intent)
            }
            R.id.menu_scan_profiles -> {
                val intent = Intent(this, ScanProfileAktivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    private fun checkBluetoothAdapter(): Boolean {
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

    private fun setClockText(start: DateTime, durationMinutes: Int) {
        val end = start.plusMinutes(durationMinutes)
        val p = Period(start, end)
        clock.text = p.toString(periodFormatter)
    }

}
