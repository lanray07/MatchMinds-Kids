package com.amazon.device.iap

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Host-rendering fixture only. Amazon's ART-targeted receiver lacks JVM stack-map frames.
 * No purchase broadcasts are processed in screenshot tests. This is never packaged in the app. */
class ResponseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Unit
}
