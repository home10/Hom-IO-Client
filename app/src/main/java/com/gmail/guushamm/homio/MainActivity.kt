package com.gmail.guushamm.homio

import android.app.Notification
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.gmail.guushamm.homio.service.MessageService
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.uiThread
import java.sql.Timestamp




class MainActivity : AppCompatActivity() {

    val gson: Gson by lazy { Gson() }
    val actions: ArrayList<Action> by lazy { ArrayList<Action>() }
    val adapter: ActionAdapter by lazy { ActionAdapter(this@MainActivity, actions) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Message sent", Snackbar.LENGTH_LONG)
                    .setAction("com.gmail.guushamm.homio.Action", null).show()
            doAsync {
                val message: JsonObject = JsonObject()
                message.addProperty("origin", "DoorBell_1")
                message.addProperty("id", "d17ec4e5-98aa-4cc8-a58b-217bed4e45f3")
                message.addProperty("timestamp", 1493815778.830151)
                message.addProperty("picture_url", "https://rickrongen.nl/ims/?id=4")
                MessageService.publishCallback(message)
            }
        }

        //Staggered grid view
        val recyclerView: RecyclerView = this@MainActivity.findViewById(R.id.recycler_view) as RecyclerView
        recyclerView.setHasFixedSize(false)
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.itemAnimator = DefaultItemAnimator()

        //Set the empty view
        recyclerView.adapter = adapter


        MessageService.subscribe(object : Handler() {
            override fun handleMessage(msg: Message?) {
                val message: String = msg?.data?.getString("msg") ?: ""
                val data: JsonObject = gson.fromJson(message, JsonObject::class.java)
                Log.d("HomIO", message)

                var type = ""
                if (data.has("type")) {
                    type = data.get("type").asString
                }

                when (type) {
                    "doorbell-cancel" -> removeAction(data.get("id").asString, actions)
                    else -> addAction(data, actions)
                }

                val title: String

                when (data.get("origin").asString) {
                    "DoorBell_1" -> {
                        title = "Someone is at your front door open it?"
                    }
                    else -> {
                        title = "Someone just made a request"
                    }
                }


                doAsync {
                    val picture_url = data.get("picture_url").asString
                    val image: Bitmap = Picasso.with(applicationContext)
                            .load(picture_url)
                            .get()

                    uiThread {
                        val notification = Notification.Builder(this@MainActivity)
                                .setContentTitle("Homio Request Received")
                                .setContentText(title)
                                .setSmallIcon(R.drawable.ic_video_label_black_24dp)
                                .setLargeIcon(image)
                                .setStyle(Notification.BigPictureStyle()
                                        .bigPicture(image))
                                .build()
                        notificationManager.notify(1, notification)
                    }

                }
            }

        })

    }

    fun addAction(data: JsonObject, actions: ArrayList<Action>) {
        val title: String
        when (data.get("origin").asString) {
            "DoorBell_1" -> {
                title = "Someone is at your front door open it?"
            }
            else -> {
                title = "Someone just made a request"
            }
        }

        var type = ""
        if (data.has("type")) {
            type = data.get("type").asString
        }

        val action: Action = Action(
                id = data.get("id").asString,
                origin = data.get("origin").asString,
                title = title,
                pictureUrl = data.get("picture_url").asString,
                type = type,
                timestamp = Timestamp(data.get("timestamp").asLong))

        actions.add(action)
        adapter.notifyDataSetChanged()
    }

    fun removeAction(id: String, actions: ArrayList<Action>) {
        val action: Action? = actions.filter { it.id == id }.firstOrNull()

        if (action != null) {
            actions.remove(action)
            adapter.notifyDataSetChanged()
            val message = "A request has been cancelled"
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                    .setAction("com.gmail.guushamm.homio.Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        when (item.itemId) {
            R.id.action_settings -> return true
            R.id.test_message -> doAsync { MessageService.publishTestMessage() }
            R.id.test_cancel_message -> doAsync { MessageService.publishCancelMessage(actions.map { it.id }) }
        }

        return super.onOptionsItemSelected(item)
    }


}