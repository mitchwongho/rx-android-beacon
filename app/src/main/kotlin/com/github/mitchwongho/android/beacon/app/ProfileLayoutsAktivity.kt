package com.github.mitchwongho.android.beacon.app

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.github.mitchwongho.android.beacon.R
import com.github.mitchwongho.android.beacon.app.rx.AlertDialogButtonClicked
import com.github.mitchwongho.android.beacon.content.fetchDO
import com.github.mitchwongho.android.beacon.content.insertOrUpdate
import com.github.mitchwongho.android.beacon.domain.ProfileLayout
import com.github.mitchwongho.android.beacon.ext.Event
import com.github.mitchwongho.android.beacon.ext.create
import com.github.mitchwongho.android.beacon.ext.getCustomView
import com.github.mitchwongho.android.beacon.ext.setCardBackgroundColorCompat
import com.github.mitchwongho.android.beacon.widget.ProfileLayoutsRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_main.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subscriptions.CompositeSubscription

/**

 */

class ProfileLayoutsAktivity : AppCompatActivity() {

    val TAG = ProfileLayoutsAktivity::class.java.simpleName

    val adapter = ProfileLayoutsRecyclerViewAdapter(mutableListOf(), this)
    var compositeSubscriptions: CompositeSubscription?
    lateinit var reducer: BehaviorSubject<Event>

    init {
        compositeSubscriptions = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as MainApplication).component.inject(this)

        setContentView(R.layout.layout_recyclerview)

        recyclerview.setHasFixedSize(true)
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = adapter

        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setIcon(R.mipmap.ic_launcher)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = getString(R.string.profile_layouts)
    }

    override fun onStart() {
        super.onStart()
    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")

        compositeSubscriptions = CompositeSubscription()
        reducer = BehaviorSubject.create()

        val sub = reducer.observeOn(AndroidSchedulers.mainThread()).
                subscribe({
                    //onNext
                    event ->
                    when (event) {
                        is AlertDialogButtonClicked -> {
                            if (event.button == AlertDialog.BUTTON_POSITIVE) {
                                //
                                val view = event.alertDialog.getCustomView()
                                val editName = view?.findViewById(R.id.edit_profile_name) as EditText
                                val editLayout = view?.findViewById(R.id.edit_profile_layout) as EditText
                                val model = ProfileLayout(editLayout.text.toString(), editName?.text.toString())
                                //TODO input validation
                                insertOrUpdate(model).
                                        subscribeOn(Schedulers.trampoline()).
                                        observeOn(AndroidSchedulers.mainThread()).
                                        subscribe({
                                            //onNext
                                            robj ->
                                            Log.d(TAG, "SUCCESS added Profile Layout")
                                        }, {
                                            //onError
                                            throwable ->
                                            Log.e(TAG, "ERROR adding Profile Layout ${throwable.message}")
                                        }, {
                                            //onCompleted
                                            Log.d(TAG, "DONE adding Profile Layout")
                                            event.alertDialog.dismiss()
                                        })
                            } else {
                                event.alertDialog.dismiss()
                            }
                        }
                        is ProfileLayoutsRecyclerViewAdapter.ProfileItemSelected -> {
                            event.view.isSelected = !event.view.isSelected
                            if (event.view.isSelected)
                                event.view.setCardBackgroundColorCompat(R.color.statusBad)
                            else
                                event.view.setCardBackgroundColorCompat(R.color.statusExcellent)
                        }
                        is ProfileLayoutsRecyclerViewAdapter.ProfileItemClicked -> {

                        }
                    }
                }, {
                    //onError
                }, {
                    //onCompleted
                })

        compositeSubscriptions?.add(sub)

        val sub1 = fetchDO(ProfileLayout::class.java).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe({
                    o ->
                    adapter.applyList(o)
                }, {
                    //onError
                    throwable ->
                    Log.e(TAG, "fetchDO(ProfileLayout): ERROR ${throwable.message}", throwable)
                }, {
                    //onCompleted
                })

        compositeSubscriptions?.add(sub1)

        val sub2 = adapter.observeOnItemClick().
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(reducer)
        compositeSubscriptions?.add(sub2)

    }

    override fun onPause() {
        super.onPause()
        compositeSubscriptions?.clear()
        compositeSubscriptions?.unsubscribe()
        reducer.onCompleted()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.profile_layouts_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Log.d(TAG, "onOptionsItemSelected")
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.menu_add -> {
                val ab = AlertDialog.Builder(this)
                ab.setView(R.layout.layout_profile_format_card)
                val sub = ab.create(resIdNegative = android.R.string.cancel, resIdPositive = R.string.save).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(reducer)
                compositeSubscriptions?.add(sub)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
