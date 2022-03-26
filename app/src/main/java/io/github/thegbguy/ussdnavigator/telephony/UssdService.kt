/*
 * *
 *  * Created by Chiranjeevi Pandey on 26/3/22, 1:07 pm
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 26/3/22, 1:07 pm
 *
 */
package io.github.thegbguy.ussdnavigator.telephony

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import io.github.thegbguy.ussdnavigator.ui.MainActivity

/*
 * This class is used to intercept USSD response dialog using the
 * accessibility service.
 */
class UssdService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventString = String.format(
            "onAccessibilityEvent: [type] %s [class] %s [package] %s [time] %s [text] %s",
            event.eventType, event.className, event.packageName,
            event.eventTime, event.text
        )
//        eventString.showToast(this)
        Log.d(TAG, eventString)
        // doesn't intercept if USSD request was not sent by this app
        // and dialog is not a alertdialog
        if (MainActivity.isRequestOngoing
            && event.className.toString().contains("AlertDialog")
        ) {
            // parses the response
            var response: String = event.text[0].toString().trim()
            if(response.isBlank()){
                cancelDialog(event)
                return
            }else{
                response = "$response "
            }

            // cancels the dialog after successfully parsing response
            // and send the response to USSDController
            if (MainActivity.isRequestOngoing) {
                cancelDialog(event)
//                sendBroadcast(response)
                MainActivity.response.value = response
                MainActivity.isRequestOngoing = false
            }
        }
    }

    // perform programmatic click in USSD response dialog
    // by traversing the nodes and finding the appropriate button
    private fun cancelDialog(event: AccessibilityEvent) {
        val nodeInfos: List<AccessibilityNodeInfo> = getLeaves(event)
        for (leaf in nodeInfos) {
            if (leaf.className.toString().lowercase().contains("button")) {
                leaf.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return
            }
        }
    }

    // gets all the leaf nodes of the accessibility event
    private fun getLeaves(event: AccessibilityEvent): List<AccessibilityNodeInfo> {
        val leaves: MutableList<AccessibilityNodeInfo> = ArrayList()
        if (event.source != null) getLeaves(leaves, event.source)
        return leaves
    }

    // traverses the node based on whether it is a parent node
    private fun getLeaves(leaves: MutableList<AccessibilityNodeInfo>, node: AccessibilityNodeInfo) {
        if (node.childCount == 0) {
            leaves.add(node)
            return
        }
        for (i in 0 until node.childCount) {
            getLeaves(leaves, node.getChild(i))
        }
    }

    override fun onInterrupt() {
        "Service is interrupted. I don't know why in earth does this happened".showToast(this)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        "Service is connected. Now app should start working".showToast(this)
    }

    override fun onUnbind(intent: Intent): Boolean {
        "Service is unbinded. Sorry to see you go".showToast(this)
        return super.onUnbind(intent)
    }

    companion object {
        private val TAG = UssdService::class.java.simpleName
    }
}

// extension function to show toast directly using the String
fun String.showToast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}