/*
 * *
 *  * Created by Chiranjeevi Pandey on 26/3/22, 1:07 pm
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 26/3/22, 1:07 pm
 *
 */

package io.github.thegbguy.ussdnavigator.telephony

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.text.TextUtils

/*
 * This is a utility class that is used to invoke different
 * telephony related operations.
 */
class SIMUtils private constructor(private val context: Context) {
    private val telephonyManagers: MutableMap<Int, TelephonyManager>
    private val telephonyManager: TelephonyManager
    private val subscriptionManager: SubscriptionManager

    @get:SuppressLint("MissingPermission")
    private var subsInfoList: List<SubscriptionInfo> = ArrayList()
        get() {
            field = subscriptionManager.activeSubscriptionInfoList
            if (field == null) {
                field = ArrayList()
            }
            return field
        }

    // initializes telephony managers instances for both
    // single and dual sim enabled devices
    private fun initializeTelephonyManagers() {
        if (!telephonyManagers.isEmpty()) {
            telephonyManagers.clear()
        }
        for (subsInfo in subsInfoList) {
            telephonyManagers[subsInfo.simSlotIndex] =
                telephonyManager.createForSubscriptionId(subsInfo.subscriptionId)
        }
    }

    // returns the sim slot index of the given sim
    fun getSimSlotIndex(simName: String?): Int {
        for (subsInfo in subsInfoList) {
            if (TextUtils.equals(simName, subsInfo.carrierName))
                return subsInfo.simSlotIndex
        }
        return -1
    }

    // returns the list of sim card inserted in the device
    fun getSimList(): List<Sim> {
        return subsInfoList.map {
            Sim(it.carrierName.toString(), it.simSlotIndex).apply {
                when(it.carrierName){
                    "Namaste" -> this.dataPackUSSDCode = "*1415#"
                    "Ncell" -> this.dataPackUSSDCode = "*17123#"
                }
            }
        }
    }

    companion object {
        // singleton instance of this class
        @SuppressLint("StaticFieldLeak")
        var INSTANCE: SIMUtils? = null
        fun getInstance(context: Context): SIMUtils? {
            if (INSTANCE == null) {
                synchronized(SIMUtils::class.java) {
                    if (INSTANCE == null)
                        INSTANCE = SIMUtils(context.applicationContext)
                }
            }
            return INSTANCE
        }
    }

    init {
        telephonyManagers = HashMap()
        telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        subscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        initializeTelephonyManagers()
    }
}

data class Sim(
    val simName: String,
    val simSlot: Int,
    var dataPackUSSDCode: String = ""
)
