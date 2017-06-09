package com.gmail.guushamm.homio

import java.sql.Timestamp

/**
 * Created by guushamm on 30-5-17.
 */
data class Action(val origin: String, val id: String, val timestamp: Timestamp, val pictureUrl: String, val title: String, val type: String = "")