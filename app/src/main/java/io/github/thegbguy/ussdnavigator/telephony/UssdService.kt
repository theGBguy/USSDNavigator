/*
 * Created by Chiranjeevi Pandey on 2/23/22, 9:41 AM
 * Copyright (c) 2022. Some rights reserved.
 * Last modified: 2022/02/23
 *
 * Licensed under GNU General Public License v3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.thegbguy.ussdnavigator.telephony

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import io.github.thegbguy.ussdnavigator.ui.MainActivity

/*
 * This class is used to intercept USSD response dialog using the
 * accessibility service. It is mainly used to improve user experience
 * but user can choose to turn it off, if they do not want any
 * intervention with the original USSD response dialog.
 */
class UssdService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventString = String.format(
            "onAccessibilityEvent: [type] %s [class] %s [package] %s [time] %s [text] %s",
            event.eventType, event.className, event.packageName,
            event.eventTime, event.text
        )
        Log.d(TAG, eventString)
        // doesn't intercept if USSD request was not sent by this app
        // and dialog is not a alertdialog
        if (MainActivity.isRequestOngoing
            && event.className.toString().contains("AlertDialog")
        ) {
            // parses the response
            val response: String = event.text[0].toString() + " "
            if (response.trim { it <= ' ' }.isEmpty()) {
                return
            }
//            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
//            clipboardManager?.setPrimaryClip(ClipData.newPlainText("text", eventString))

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

    // sends local broadcast
//    private fun sendBroadcast(response: String) {
//        val receiverIntent = Intent(EVENT_RESPONSE)
//        receiverIntent.putExtra(KEY_RESPONSE, response)
//        Handler(mainLooper).postDelayed({
//            LocalBroadcastManager.getInstance(this@UssdService).sendBroadcast(receiverIntent)
//        }, 500)
//    }

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
        Log.d(TAG, "onInterrupt fired")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected fired")
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "onUnbind fired")
        return super.onUnbind(intent)
    }

    companion object {
        private val TAG = UssdService::class.java.simpleName
    }
}