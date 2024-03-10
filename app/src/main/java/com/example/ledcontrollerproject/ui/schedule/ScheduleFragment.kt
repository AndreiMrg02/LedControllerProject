package com.example.ledcontrollerproject.ui.schedule

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.example.ledcontrollerproject.ui.theme.WoofTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MyMenuScreen()
            }
        }
    }
}

@Composable
fun MyMenuScreen() {
    var menuItems by remember { mutableStateOf(listOf(1)) }
    WoofTheme {
        LazyColumn {
            items(menuItems) { index ->
                MyMenuContent(
                    index = index - 1,
                    onDeleteClick = {
                        menuItems = menuItems.filterIndexed { i, _ -> i != it }
                    }
                )
            }

            item {
                Button(
                    onClick = {
                        menuItems = menuItems + (menuItems.size + 1)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Adaugă")
                }
            }
        }
    }
}


@Composable
fun MyMenuContent(index: Int, onDeleteClick: (Int) -> Unit) {
    var label by remember { mutableStateOf("Eticheta") }
    var time by remember { mutableStateOf("12:00") }
    var daysSelected by remember { mutableStateOf(mutableSetOf<String>()) }
    var isTimePickerVisible by remember { mutableStateOf(false) }
    val backgroundColor = Color(0xFFCCADE0);
    WoofTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            TextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Eticheta") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            TimePickerField(
                time = time,
                onTimeSelected = { newTime ->
                    time = newTime
                    isTimePickerVisible = false
                },
                isTimePickerVisible = isTimePickerVisible,
                onTimePickerVisibleChange = { isVisible ->
                    isTimePickerVisible = isVisible
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                for (day in listOf("L", "M", "M", "J", "V", "S", "D")) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(day, fontSize = 16.sp)
                        Checkbox(
                            checked = daysSelected.contains(day),
                            onCheckedChange = {
                                if (it) {
                                    daysSelected.add(day)
                                } else {
                                    daysSelected.remove(day)
                                }
                            },
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            )
            {
                TextButton(
                    onClick = {
                        onDeleteClick(index)
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text("Stergere")
                }
            }
        }
        
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(Color.Gray)
        )
    }
}


@Composable
fun TimePickerField(
    time: String,
    onTimeSelected: (String) -> Unit,
    isTimePickerVisible: Boolean,
    onTimePickerVisibleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "Ora: $time",
            modifier = Modifier.clickable { onTimePickerVisibleChange(true) },
            fontSize = 30.sp // Aici am mărit dimensiunea textului
        )

        if (isTimePickerVisible) {
            ShowTimePicker(onTimeSelected, onTimePickerVisibleChange)
        }
    }
}

@Composable
fun ShowTimePicker(
    onTimeSelected: (String) -> Unit,
    onTimePickerVisibleChange: (Boolean) -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(context) {
        val calendar = Calendar.getInstance()

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val selectedTime = timeFormat.format(calendar.time)
                onTimeSelected(selectedTime)
                onTimePickerVisibleChange(false)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()

        onDispose { /* Cleanup, if needed */ }
    }
}