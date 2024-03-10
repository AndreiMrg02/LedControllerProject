package com.example.ledcontrollerproject.ui.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.TextButton
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.ledcontrollerproject.ui.theme.WoofTheme
import com.example.ledcontrollerproject.util.BluetoothScanner
import com.example.ledcontrollerproject.util.CallbackBluetoothScan
import kotlinx.coroutines.launch

class BluetoothFragment : Fragment() {
    private var isScanning: Boolean = false
    private val ID_BLUETOOTH = 101
    private val ID_ENABLE_BLUETOOTH = 102

    private val INTERVAL_REFRESH_DATA_SCANNER_MS: Long = 60 * 1000 // minutes

    private lateinit var bluetoothScanner: BluetoothScanner

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        return ComposeView(context).apply {
            setContent {
                BluetoothScannerApp {
                    MainContent()
                }
            }
        }
    }

    @Composable
    fun BluetoothScannerApp(content: @Composable () -> Unit) {
        rememberScaffoldState()
        MaterialTheme {
            Surface {
                content()
            }
        }
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    fun MainContent() {
        val context = LocalContext.current
        val rec = remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
        val handler = Handler()

        // Start scanning on launch
        initScan(context, rec)
        DisposableEffect(Unit) {
            startInfinityRefreshScanner(handler, rec)
            onDispose {
                handler.removeCallbacksAndMessages(null)
            }
        }

        val coroutineScope = rememberCoroutineScope()
        WoofTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = {
                    coroutineScope.launch {
                        rec.value = emptyList() // Clear the list
                        bluetoothScanner.initRefreshData()
                    }
                }) {
                    Text("Start Scan")
                }

                Spacer(modifier = Modifier.height(16.dp))

                DeviceList(rec.value)
            }
        }
    }

    /*    @SuppressLint("MissingPermission")
        @Composable
        fun DeviceList(devices: List<BluetoothDevice>) {
            LazyColumn {
                items(devices) { device ->
                    if (device.name != null) {
                        DeviceListItem(device = device)
                    }
                }
            }
        }*/

    @Composable
    @SuppressLint("MissingPermission")
 /*   fun DeviceList(devices: List<BluetoothDevice>, onItemClick: (BluetoothDevice) -> Unit) {
        LazyColumn {
            items(devices.filter { it.name != null }) { device ->
                DeviceListItem(device = device) {
                    onItemClick(device)
                }
            }
        }
    }*/
    fun DeviceList(devices: List<BluetoothDevice>)
    {
        WoofTheme {
            LazyColumn {
                items(devices.filter { it.name != null }) { device ->
                    DeviceListItem(device = device) {
                    }
                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    @Composable
    fun DeviceListItem(device: BluetoothDevice, onItemClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClick() }
                .padding(8.dp),
            shape = CircleShape,
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = device.name ?: "Unknown Device",
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(onClick = {
                    initiatePairing(device)
                }) {
                    Text("Pair Device")
                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun initiatePairing(device: BluetoothDevice) {
        try {
            val isBonded = device.createBond()
            if (isBonded) {
                Toast.makeText(context, "Pairing successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Pairing failed!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Pairing failed! Exception: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


    private fun startInfinityRefreshScanner(
        handler: Handler,
        rec: MutableState<List<BluetoothDevice>>
    ) {
        if (!isScanning) {
            val runnable = object : Runnable {
                @RequiresApi(Build.VERSION_CODES.S)
                override fun run() {
                    rec.value = emptyList() // Clear the list
                    bluetoothScanner.initRefreshData()
                    handler.postDelayed(this, INTERVAL_REFRESH_DATA_SCANNER_MS)
                }
            }
            handler.post(runnable)
            isScanning = true
        }
    }

    private fun initScan(context: Context, rec: MutableState<List<BluetoothDevice>>) {
        if (isScanning) {
            bluetoothScanner.stopScan()
        }

        bluetoothScanner = BluetoothScanner(context, object : CallbackBluetoothScan {
            override fun permissionsNotGranted(permissions: Array<String>) {
                requestPermissions(permissions, ID_BLUETOOTH)
            }

            @SuppressLint("MissingPermission")
            override fun bluetoothNotEnable() {
                Toast.makeText(context, "Enable Bluetooth!", Toast.LENGTH_LONG).show()
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, ID_ENABLE_BLUETOOTH)
            }

            override fun deviceFound(device: BluetoothDevice) {
                if (!rec.value.contains(device)) {
                    rec.value = rec.value + listOf(device)
                }
            }

            override fun deviceNotSupportBluetooth() {
                Toast.makeText(context, "Device does not support Bluetooth!", Toast.LENGTH_LONG)
                    .show()
            }
        })

        isScanning = false
    }
}
