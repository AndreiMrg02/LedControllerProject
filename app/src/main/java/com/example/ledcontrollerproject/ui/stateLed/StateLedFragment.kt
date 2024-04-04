package com.example.ledcontrollerproject.ui.stateLed

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.ledcontrollerproject.R
import com.example.ledcontrollerproject.ui.bluetooth.BluetoothFragment
import com.example.ledcontrollerproject.ui.theme.WoofTheme
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

class StateLedFragment : Fragment() {

    private var _binding: StateLedFragment? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ColorPicker()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    fun ColorPicker() {
        val controller = rememberColorPickerController()
        val colorOn = Color(android.graphics.Color.parseColor("#24ad37"))
        val colorOff = Color(android.graphics.Color.parseColor("#ad2430"))
        WoofTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 30.dp)
            ) {
                HsvColorPicker(modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .padding(10.dp),
                    controller = controller,
                    onColorChanged = {
                        val rgb = "${(it.color.red * 255).toInt()},${(it.color.green * 255).toInt()},${(it.color.blue * 255).toInt()}" + '\n'
                        Log.d("Color", rgb)
                        context?.let { it1 ->
                            BluetoothFragment.sendDataToBluetoothDevice(rgb,
                                it1
                            )
                        }

                    })
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(colorOn),
                        onClick = {
                            val rgb = "255, 255, 255"
                            context?.let { it1 ->
                                BluetoothFragment.sendDataToBluetoothDevice(rgb,
                                    it1
                                )
                            }
                            Log.d("ON", "The led is turn on")
                        }) {
                        Text("ON")
                    }
                    Button(modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(colorOff),
                        onClick = {
                            val rgb = "0, 0, 0"
                            context?.let { it1 ->
                                BluetoothFragment.sendDataToBluetoothDevice(rgb,
                                    it1
                                )
                            }
                        }) {
                        Text("OFF")
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
