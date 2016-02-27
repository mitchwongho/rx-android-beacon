package com.github.mitchwongho.android.beacon.app

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.github.mitchwongho.android.beacon.R
import com.github.mitchwongho.android.beacon.content.e
import com.github.mitchwongho.android.beacon.content.fetchDO
import com.github.mitchwongho.android.beacon.domain.ScanProfile
import com.github.mitchwongho.android.beacon.ext.Event
import com.github.mitchwongho.android.beacon.widget.ScanProfileRecyclerViewAdapter
import com.jakewharton.rxbinding.view.RxMenuItem
import kotlinx.android.synthetic.main.activity_main.*
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.BehaviorSubject
import rx.subscriptions.CompositeSubscription

/**
 *
 */

open class AddMenuItemClicked() : Event
open class BackMenuItemClicked() : Event
open class ScanProfileDataUpdated(val data: List<ScanProfile>)

class ScanProfileAktivity : AppCompatActivity() {


    lateinit var adapter: ScanProfileRecyclerViewAdapter

    lateinit var compositeSubscription: CompositeSubscription

    lateinit var reducer: BehaviorSubject<Event>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_recyclerview)
        adapter = ScanProfileRecyclerViewAdapter(mutableListOf<ScanProfile>(), this)
        recyclerview.setHasFixedSize(true)
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = adapter

        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setIcon(R.mipmap.ic_launcher)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = getString(R.string.scan_profiles)
    }

    override fun onStart() {
        super.onStart()
        e("onStart")
        compositeSubscription = CompositeSubscription()
        reducer = BehaviorSubject.create()

        val sub = reducer.subscribe({
            //onNext
            e ->
            when(e) {
                is AddMenuItemClicked -> {
                    val intent = Intent(this, SettingsAktivity::class.java)
                    startActivity(intent)
                }
                is BackMenuItemClicked -> {
                    finish()
                }
                is ScanProfileRecyclerViewAdapter.OnItemClicked -> {
                    Toast.makeText(this, "Item Clicked", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SettingsAktivity::class.java)
                    intent.putExtra("ScanProfileId", e.scanProfile.uuid)
                    startActivity(intent)
                }
                is ScanProfileRecyclerViewAdapter.OnItemLongPressed -> {
                    Toast.makeText(this, "Item Long Pressed", Toast.LENGTH_SHORT).show()
                }
            }
        }, {
            //onError
            throwable -> Unit
        }, {
            //onCompleted
            Unit
        })
        compositeSubscription.add(sub)
    }

    override fun onResume() {
        super.onResume()
        e("onResume")
        val sub = fetchDO(ScanProfile::class.java).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe({
                    //onNext
                    e ->
                    adapter.updateDataSet(e)
                }, {
                    //onError
                    throwable ->
                    this.e("Error fetching 'ScanProfile' dataset", throwable)
                }, {
                    //onCompleted
                    Unit
                })
        compositeSubscription.add(sub)

        val sub1 = adapter.observe().
                subscribe(reducer)
        compositeSubscription.add(sub1)

    }

    override fun onPause() {
        super.onPause()
        e("onPause")
        reducer.onCompleted()
        compositeSubscription.unsubscribe()
        compositeSubscription.clear()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        e("onCreateOptionsMenu")
        menuInflater.inflate(R.menu.profile_layouts_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                reducer.onNext(BackMenuItemClicked())
                return false
            }
            R.id.menu_add -> {
                reducer.onNext(AddMenuItemClicked())
                return false
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}