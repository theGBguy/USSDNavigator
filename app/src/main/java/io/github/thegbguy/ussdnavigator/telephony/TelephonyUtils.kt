/*
 * *
 *  * Created by Chiranjeevi Pandey on 26/3/22, 1:07 pm
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 26/3/22, 1:07 pm
 *
 */

package io.github.thegbguy.ussdnavigator.telephony

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telecom.TelecomManager
import android.text.TextUtils.SimpleStringSplitter
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.startActivity
import io.github.thegbguy.ussdnavigator.ui.MainActivity


// dials the number in the phone's default dialer
fun dial(context: Context, number: String?) {
    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(number)))
    dialIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(dialIntent)
}

// calls the given number using the appropriate sim card
fun call(context: Context, number: String, simSlotIndex: Int?) {
    simSlotIndex?.let {
        context.startActivity(
            getActionCallIntent(
                context, Uri.parse("tel:$number"),
                simSlotIndex
            )
        )
        MainActivity.isRequestOngoing = true
        Log.d("TelephonyUtils", "Called Number : $number")
    }
}

// generates appropriate call intent for any sim slot
@RequiresApi(Build.VERSION_CODES.M)
@SuppressLint("MissingPermission")
private fun getActionCallIntent(context: Context, uri: Uri, simSlotIndex: Int): Intent {
    // https://stackoverflow.com/questions/25524476/make-call-using-a-specified-sim-in-a-dual-sim-device
    val simSlotName = arrayOf(
        "extra_asus_dial_use_dualsim",
        "com.android.phone.extra.slot",
        "slot",
        "simslot",
        "sim_slot",
        "subscription",
        "Subscription",
        "phone",
        "com.android.phone.DialingMode",
        "simSlot",
        "slot_id",
        "simId",
        "simnum",
        "phone_type",
        "slotId",
        "slotIdx"
    )
    val intent = Intent(Intent.ACTION_CALL, uri)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.putExtra("com.android.phone.force.slot", true)
    intent.putExtra("Cdma_Supp", true)
    for (slotIndex in simSlotName) intent.putExtra(slotIndex, simSlotIndex)
    val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    if (isPermissionGranted(context, Manifest.permission.READ_PHONE_STATE)) {
        val phoneAccountHandleList = telecomManager.callCapablePhoneAccounts
        if (phoneAccountHandleList != null && phoneAccountHandleList.size > simSlotIndex) intent.putExtra(
            "android.telecom.extra.PHONE_ACCOUNT_HANDLE",
            phoneAccountHandleList[simSlotIndex]
        )
    } else {
        Toast.makeText(context, "Permission is not granted", Toast.LENGTH_LONG).show()
    }
    return intent
}

val permissions = arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE)

fun isPermissionsGranted(context: Context): Boolean {
    permissions.forEach { permission ->
        if (!isPermissionGranted(context = context, permission = permission)) {
            return false
        }
    }
    return isAccessibilityServiceEnabled(context = context)
}

fun isPermissionGranted(context: Context, permission: String): Boolean {
    return checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun requestPermission(activity: Activity, permission: String){
    ActivityCompat.requestPermissions(activity, arrayOf(permission), 1100)
}

fun requestPermissions(activity: Activity, permissions: Array<String>) {
    ActivityCompat.requestPermissions(activity, permissions, 1000)
    if (!isAccessibilityServiceEnabled(activity)) {
        startActivity(activity, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), null)
    }
}

// checks whether accessibility service is enabled
fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedComponentName = ComponentName(context, UssdService::class.java)
    val enabledServicesSetting = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    val colonSplitter = SimpleStringSplitter(':')
    colonSplitter.setString(enabledServicesSetting)
    while (colonSplitter.hasNext()) {
        val componentNameString = colonSplitter.next()
        val enabledComponentName = ComponentName.unflattenFromString(componentNameString)
        if (enabledComponentName != null && enabledComponentName == expectedComponentName)
            return true
    }
    return false
}





