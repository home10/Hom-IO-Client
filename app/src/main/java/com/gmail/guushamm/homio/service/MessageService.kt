package com.gmail.guushamm.homio.service

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.QueueingConsumer
import java.util.*
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque


/**
 * Created by guushamm on 5-4-17.
 */
object MessageService {
    val factory: ConnectionFactory by lazy {
        ConnectionFactory().apply {
            username = "dmtuwgyf"
            password = "-uO2kShACltC63RsmWE3eQMwQ0vbr-Pq"
            host = "orangutan.rmq.cloudamqp.com"
            virtualHost = "dmtuwgyf"
            port = 5672
            isAutomaticRecoveryEnabled = false
        }
    }
    val channel: Channel by lazy { factory.newConnection().createChannel() }
    val queue: BlockingDeque<String> by lazy { LinkedBlockingDeque<String>() }
    val gson: Gson by lazy { Gson() }
    val random: Random by lazy { Random()}

    fun publishCallback(message: JsonObject) {
        val origin: String = message["origin"].asString
        channel.exchangeDeclare("callback", "direct", true)
        channel.basicPublish("callback", origin, null, message.toString().toByteArray())
    }

    fun publishTestMessage() {
        val message: JsonObject = JsonObject()
        message.addProperty("origin", "DoorBell_1")
        message.addProperty("id", UUID.randomUUID().toString())
        message.addProperty("timestamp", Calendar.getInstance().time.time)
        message.addProperty("picture_url", "https://rickrongen.nl/ims/?id=4")
        channel.basicPublish("chat", "", null, message.toString().toByteArray())
    }

    fun publishCancelMessage(ids: List<String>) {
        val id = ids[random.nextInt(ids.size)]

        val message: JsonObject = JsonObject()
        message.addProperty("origin", "DoorBell_1")
        message.addProperty("type", "doorbell-cancel")
        message.addProperty("id", id)
        message.addProperty("timestamp", Calendar.getInstance().time.time)
        message.addProperty("allowed", true)
        message.addProperty("user", "GuusHamm")
        channel.basicPublish("chat", "", null, message.toString().toByteArray())
    }

    fun subscribe(handler: Handler) {
        object : Thread() {
            override fun run() {
                while (true) {
                    try {
                        val connection: Connection = factory.newConnection()
                        val channel = connection.createChannel()
                        channel.basicQos(1)
                        val q = channel.queueDeclare()
                        channel.queueBind(q.queue, "chat", "")
                        val consumer = QueueingConsumer(channel)


                        val response: String = channel.basicConsume(q.queue, false, consumer)

                        while (true) {
                            val delivery: QueueingConsumer.Delivery = consumer.nextDelivery()
                            val message: String = String(delivery.body)
                            val msg: Message = handler.obtainMessage()
                            val bundle: Bundle = Bundle()
                            bundle.putString("msg", message)
                            msg.data = bundle
                            handler.sendMessage(msg)

                            val deliveryTag: Long = delivery.envelope.deliveryTag
                            channel.basicAck(deliveryTag, false)
                        }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        Log.d("HomIO", e.javaClass.name)
                        e.printStackTrace()
                        try {
                            Thread.sleep(5000)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }.start()
    }
}