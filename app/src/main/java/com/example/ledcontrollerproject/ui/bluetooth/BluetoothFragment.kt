package com.example.ledcontrollerproject.ui.bluetooth

import BluetoothConnect
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.example.ledcontrollerproject.R
import com.example.ledcontrollerproject.ui.theme.WoofTheme
import com.example.ledcontrollerproject.util.BluetoothScanner
import com.example.ledcontrollerproject.util.CallbackBluetoothScan
import kotlinx.coroutines.launch
import java.util.Random
import java.util.UUID

class BluetoothFragment : Fragment() {
    companion object {
        private var isScanning: Boolean = false
        private const val ID_BLUETOOTH = 101
        private const val ID_ENABLE_BLUETOOTH = 102
        private const val INTERVAL_REFRESH_DATA_SCANNER_MS: Long = 60 * 1000
        private lateinit var bluetoothService: BluetoothConnect
        private lateinit var bondedDevice: BluetoothDevice


        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        @SuppressLint("MissingPermission")
        fun sendDataToBluetoothDevice(rgb: String, context: Context) {
            bluetoothService = BluetoothConnect(Handler(Looper.getMainLooper()))
            val uuid = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

            if (bluetoothService != null && bondedDevice != null) {
                bluetoothService.connectDevice(bondedDevice, uuid, context)

                val rgbSpace = rgb.takeWhile { it != '\n' }
                bluetoothService.message = rgbSpace.toByteArray()
            } else {
                Toast.makeText(context, "Dispozitivul nu este conectat", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private lateinit var bluetoothScanner: BluetoothScanner

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    fun MainContent() {
        val context = LocalContext.current
        val rec = remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
        val handler = Handler()

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
                        rec.value = emptyList()
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    @SuppressLint("MissingPermission")
    fun DeviceList(devices: List<BluetoothDevice>) {
        WoofTheme {
            LazyColumn {
                items(devices.filter { it.name == "HMSoft"}) { device ->
                    DeviceListItem(device = device) {}
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
                    modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = device.name ?: "Unknown Device", fontWeight = FontWeight.Bold
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



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    private fun initiatePairing(device: BluetoothDevice) {
        try {
            val isBonded = device.createBond()
            if (isBonded) {
                bluetoothService =
                    context?.let { BluetoothConnect(Handler(Looper.getMainLooper())) }!!
                val uuid = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
                context?.let {
                    if (bluetoothService != null) {
                        bluetoothService.connectDevice(device, uuid, it)
                    }
                }
                bondedDevice = device
                val name = device.name
                val title = "Conexiune realizată cu $name"
                val content = "Conexiunea Bluetooth s-a realizat cu succes."
                context?.let { sendConnectionNotification(it, title, content) }
                val r = 255
                val g = 0
                val b = 0
                val message =
                    r.toString() + "," + g.toString() + "," + b.toString() + '\n'
                sendDataToBluetoothDevice(message, requireContext())
                Toast.makeText(context, "Pairing successful!", Toast.LENGTH_SHORT).show()
            } else {
                val name = device.name
                val title = name
                val content = "Conexiunea Bluetooth nu s-a realizat cu succes."
                context?.let { sendConnectionNotification(it, title, content) }
                Toast.makeText(context, "Pairing failed!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            val errorMessage = "Pairing failed! Exception: ${e.message}"
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            showCopyDialog(errorMessage, context)
        }
    }

    private fun showCopyDialog(errorMessage: String, context: Context?) {
        context?.let {
            val clipboard = it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Error Message", errorMessage)
            clipboard.setPrimaryClip(clip)

            AlertDialog.Builder(it).setTitle("Error").setMessage(errorMessage)
                .setPositiveButton("Copy") { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(it, "Error message copied to clipboard", Toast.LENGTH_SHORT)
                        .show()
                }.setNegativeButton("Dismiss") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }
    }

    private fun startInfinityRefreshScanner(
        handler: Handler, rec: MutableState<List<BluetoothDevice>>
    ) {
        if (!isScanning) {
            val runnable = object : Runnable {
                @RequiresApi(Build.VERSION_CODES.S)
                override fun run() {
                    rec.value = emptyList()
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

    private fun sendConnectionNotification(context: Context, title: String, content: String) {
        val channelId = "bluetooth_connection_channel"
        val notificationId = 101

        val notificationBuilder =
            NotificationCompat.Builder(context, channelId).setSmallIcon(R.drawable.img_2)
                .setContentTitle(title).setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Bluetooth Connection", NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    public fun setRGB(message: ByteArray){

    }
}
