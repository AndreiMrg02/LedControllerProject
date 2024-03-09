package com.example.ledcontrollerproject.ui.bluetoothDevices

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BluetoothDeviceFragment : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        layout.orientation = LinearLayout.VERTICAL

        val btList = ListView(this)
        btList.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        layout.addView(btList)

        val pairedDevices: Set<BluetoothDevice> = BluetoothAdapter.getDefaultAdapter().bondedDevices
        val data: MutableList<String> = pairedDevices.mapTo(mutableListOf()) { it.name + it.address }

        if (data.isNotEmpty()) {
            val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data)
            btList.adapter = adapter

            btList.setOnItemClickListener { _, _, position, _ ->
                val deviceName = adapter.getItem(position)

                val intent = Intent()
                intent.putExtra("deviceName", deviceName)
                setResult(RESULT_OK, intent)
                finish()
            }
        } else {
            val value = "No devices were found"
            finish()
        }

        setContentView(layout)
    }
}