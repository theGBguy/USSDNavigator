/*
 * *
 *  * Created by Chiranjeevi Pandey on 26/3/22, 1:07 pm
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 26/3/22, 1:07 pm
 *
 */

package io.github.thegbguy.ussdnavigator.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.thegbguy.ussdnavigator.telephony.*
import io.github.thegbguy.ussdnavigator.ui.components.CustomRadioGroup
import io.github.thegbguy.ussdnavigator.ui.theme.Typography
import io.github.thegbguy.ussdnavigator.ui.theme.USSDNavigatorTheme
import java.util.regex.Pattern

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize the simutils instance
        val simUtils = SIMUtils.getInstance(this)

        // check for READ_PHONE_STATE permissions immediately
        // as we are in hurry to get started
        if (!isPermissionGranted(this, permissions[0])) {
            requestPermission(this, permissions[0])
        }

        // handle clicking of the pack name
        val onPackNameClick = fun(ussdRequest: String) {
            if (isPermissionsGranted(context = this)) {
                call(
                    context = this,
                    number = Uri.encode(ussdRequest).toString(),
                    simSlotIndex = getCurrentlySelectedSimSlot()
                )
            } else {
                requestPermissions(activity = this, permissions = permissions)
            }
        }

        // set the content of this activity
        setContent {
            MainScreen(
                simList = simUtils?.getSimList(),
                textState = response,
                onTakePackClick = onPackNameClick
            )
        }
    }

    companion object {
        // variable to check whether the request is initiated
        // from our app because we cannot close the manual
        // USSD code that was run by user from the default phone app
        var isRequestOngoing = false

        // response of the USSD request
        val response = mutableStateOf("")

        // currently selected sim card
        val selectedSim: MutableState<Sim?> = mutableStateOf(null)
    }
}

// returns pack name as a string array from the raw response
fun getArrayFromResponse(response: String): Array<String> {
    return with(TextUtils.SimpleStringSplitter('\n')) {
        setString(response)
        val packs = mutableListOf("")
        while (hasNext()) {
            packs.add(next())
        }
        packs.toTypedArray()
    }
}

// returns the position of the package in a pack list
fun getNumberFromString(str: String): String? {
    val pattern = Pattern.compile("(\\d+)", Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(str)
    return if (matcher.find()) matcher.group(1) else ""
}

// returns new request from the old request
// adding *position in the right place (wink)
fun getNewRequestFromOldRequest(oldRequest: String, position: String): String {
    return with(StringBuilder(oldRequest)) {
        insert(length - 1, position)
        toString()
    }
}

// return old request from the new request
// idk why the heck I was writing the above comment
// because the function name is totally readable
// strip *position character subsequence
fun getOldRequestFromCurrentRequest(currentRequest: String): String {
    return with(StringBuilder(currentRequest)) {
        replace(lastIndexOf("*"), length - 1, "")
        toString()
    }
}

// returns if the string is not a text containing
// info to go back or go to main main
fun isNotBackMain(packName: String): Boolean {
    return !packName.contains("*")
}

// returns if the parsed name is a pack name
// string format : 1)Pack Name for Namaste
// 1 > Pack Name for Ncell
fun isPackName(packName: String): Boolean {
    return packName.contains(Regex("\\d+\\)?\\s?>?"))
}

fun isSuccessfullMessage(response: String): Boolean {
    return response.contains("succ")
}

fun isFailedMessage(response: String): Boolean {
    return response.contains("prob")
}

// return currently selected sim card's slot
// so we can automate the calling process
fun getCurrentlySelectedSimSlot(): Int? {
    return MainActivity.selectedSim.value?.simSlot
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun MainScreen(
    simList: List<Sim>? = listOf(),
    textState: MutableState<String>,
    onTakePackClick: (String) -> Unit,
) {
    val response by remember { textState }
    var currentRequest = remember { "" }

    USSDNavigatorTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = simList?.size!! > 1) {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colors.primary.copy(alpha = .3f),
                                        MaterialTheme.colors.secondary.copy(alpha = .3f)
                                    )
                                )
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // displays sim info
                        simList.forEach {
                            Text(text = "Sim \"${it.simName}\" inserted in slot ${it.simSlot} ")
                        }
                        Text(text = "Select one telecom operator below to take data packs")
                        // allows user to select sim
                        CustomRadioGroup(
                            simList = simList,
                            selectedSim = MainActivity.selectedSim
                        )
                    }
                }
                val context = LocalContext.current
                Button(
                    modifier = Modifier.wrapContentSize(),
                    onClick = {
                        if(!isPermissionsGranted(context)){

                        }
                        // checks if there is no currently selected sim
                        if (MainActivity.selectedSim.value == null) {
                            Toast.makeText(
                                context,
                                "Please select a SIM card first",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            return@Button
                        }
                        currentRequest = MainActivity.selectedSim.value!!.dataPackUSSDCode
                        onTakePackClick(currentRequest)
                    }
                ) {
                    Text(text = "Go to main menu")
                }
                // create the buttons dynamically with the help of
                // array of pack names parsed from raw response
                val packNameArray = getArrayFromResponse(response)
                packNameArray.forEach {
                    val packName = it.trim()
                    if (isPackName(packName = packName)) {
                        OutlinedButton(onClick = {
                            getNumberFromString(packName)?.let { position ->
                                currentRequest =
                                    getNewRequestFromOldRequest(currentRequest, "*$position")
                                onTakePackClick(currentRequest)
                            }
                        }) {
                            Text(text = packName)
                        }
                    } else {
                        if (packName.isNotBlank() && isNotBackMain(packName = packName)) {
                            if (isSuccessfullMessage(packName)) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Successful",
                                        tint = Color.Green.copy(alpha = .7f)
                                    )
                                    Text(text = packName, fontWeight = FontWeight.Bold)
                                }
                            } else if (isFailedMessage(packName)) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Failed",
                                        tint = Color.Red.copy(alpha = .7f)
                                    )
                                    Text(text = packName, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(
                                    text = "Pack Title : $packName",
                                    style = Typography.h5,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Button(onClick = {
                    if (MainActivity.selectedSim.value == null
                        || packNameArray.isEmpty()
                    ) {
                        Toast.makeText(
                            context,
                            "Please select a SIM card or click another button to load pack details first",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return@Button
                    }
                    if (currentRequest == MainActivity.selectedSim.value!!.dataPackUSSDCode) {
                        Toast.makeText(
                            context,
                            "Can't go back from here",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return@Button
                    }
                    currentRequest =
                        getOldRequestFromCurrentRequest(currentRequest = currentRequest)
                    onTakePackClick(currentRequest)
                }) {
                    Text(text = "Go back")
                }
            }
        }
    }
}