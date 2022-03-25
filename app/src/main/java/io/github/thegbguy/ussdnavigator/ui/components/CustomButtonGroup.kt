package io.github.thegbguy.ussdnavigator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.thegbguy.ussdnavigator.telephony.Sim
import io.github.thegbguy.ussdnavigator.ui.MainActivity

@Composable
fun CustomRadioGroup(
    selectedSim: MutableState<Sim?>,
    simList: List<Sim> = listOf()
) {
    var selectedOption by remember {
        selectedSim
    }
    val onSelectionChange = { sim: Sim ->
        selectedOption = sim
        MainActivity.response.value = ""
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        simList.forEach { sim ->
            Row(
                modifier = Modifier
                    .width(150.dp)
                    .padding(all = 8.dp),
            ) {
                Text(
                    text = sim.simName,
                    style = typography.body1.merge(),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            shape = RoundedCornerShape(
                                size = 8.dp,
                            ),
                        )
                        .clickable {
                            onSelectionChange(sim)
                        }
                        .background(
                            if (sim.simName == selectedOption?.simName) {
                                colors.primary
                            } else {
                                colors.primary.copy(alpha = .3f)
                            }
                        )
                        .padding(
                            vertical = 12.dp,
                            horizontal = 16.dp,
                        ),
                )
            }
        }
    }
}