package com.gmail.guushamm.homio

import android.app.Activity
import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.gmail.guushamm.homio.service.MessageService
import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onClick
import java.util.*

/**
 * Created by guushamm on 30-5-17.
 */
class ActionAdapter(var context: Context, var actions: ArrayList<Action>) : RecyclerView.Adapter<ActionAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return actions.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val action: Action = actions[position]
        holder.title.text = action.title
        Picasso
                .with(context)
                .load(action.pictureUrl)
                .resize(450,350)
                .centerInside()
                .into(holder.thumbnail)
        holder.timestamp.text = action.timestamp.time.toString()

        holder.allow.onClick {
            val response: JsonObject = makeRequest(action)
            response.addProperty("allowed", true)
            doAsync {
                MessageService.publishCallback(response)
            }
            actions.remove(action)

            this.notifyDataSetChanged()

            val message = "Succesfully allowed request"
            Snackbar.make((context as Activity).findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                    .setAction("com.gmail.guushamm.homio.Action", null).show()
        }

        holder.deny.onClick {
            val response: JsonObject = makeRequest(action)
            response.addProperty("allowed", false)
            doAsync {
                MessageService.publishCallback(response)
            }
            actions.remove(action)
            this.notifyDataSetChanged()

            val message = "Succesfully denied request"
            Snackbar.make((context as Activity).findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                    .setAction("com.gmail.guushamm.homio.Action", null).show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.action_card, parent, false)

        return ViewHolder(itemView)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.title) as TextView
        var timestamp: TextView = view.findViewById(R.id.timestamp) as TextView
        var thumbnail: ImageView = view.findViewById(R.id.thumbnail) as ImageView
        var allow: Button = view.findViewById(R.id.allow) as Button
        val deny: Button = view.findViewById(R.id.deny) as Button
    }

    fun makeRequest(action: Action): JsonObject {
        val response: JsonObject = JsonObject()
        response.addProperty("origin", action.origin)
        response.addProperty("id", action.id)
        //TODO make this more dynamic
        response.addProperty("user", "GuusHamm")
        response.addProperty("timestamp", Calendar.getInstance().time.time)
        return response
    }
}