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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

        val simUtils = SIMUtils.getInstance(this)

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

        setContent {
            MainScreen(
                simList = simUtils?.getSimList(),
                textState = response,
                onTakePackClick = onPackNameClick
            )
        }
    }

    companion object {
        var isRequestOngoing = false
        val response = mutableStateOf("")
        val selectedSim: MutableState<Sim?> = mutableStateOf(null)
    }
}

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

fun getNumberFromString(str: String): String? {
    val pattern = Pattern.compile("(\\d+)", Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(str)
    return if (matcher.find()) matcher.group(1) else ""
}

fun getNewRequestFromOldRequest(oldRequest: String, position: String): String {
    return with(StringBuilder(oldRequest)) {
        insert(length - 1, position)
        toString()
    }
}

fun getOldRequestFromCurrentRequest(currentRequest: String): String {
    return with(StringBuilder(currentRequest)) {
        replace(lastIndexOf("*"), length - 1, "")
        toString()
    }
}

fun isNotBackMain(packName: String): Boolean {
    return !packName.contains("*")
}

fun isPackName(packName: String): Boolean {
    return packName.contains(Regex("\\d+\\)?\\s?>?"))
}

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
                        simList.forEach {
                            Text(text = "Sim \"${it.simName}\" inserted in slot ${it.simSlot} ")
                        }
                        Text(text = "Select one telecom operator below to take data packs")
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
                            Text(
                                text = "Pack Title : $packName",
                                style = Typography.h5,
                                fontWeight = FontWeight.Bold
                            )
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