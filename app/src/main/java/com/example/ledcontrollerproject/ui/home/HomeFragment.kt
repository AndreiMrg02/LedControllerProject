package com.example.ledcontrollerproject.ui.home

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_VISIBLE_TO_INSTANT_APPS
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.registerReceiver
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ledcontrollerproject.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var btPermission = false
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val devicesList: MutableList<BluetoothDevice> = mutableListOf()
    private lateinit var devicesAdapter: ArrayAdapter<String>
    private var isFound = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        devicesAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            devicesList.map {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return binding.devicesListView
                }
                it.name ?: "Unknown Device"
            })
        val devicesListView: ListView = binding.devicesListView
        devicesListView.adapter = devicesAdapter

        val connectButton: Button = binding.connectToBluetooth
        connectButton.setOnClickListener {
            Log.d("Bluetooth", "Connect button clicked. Initiating Bluetooth scan...")
            scanBluetooth()

        }

        return root
    }

    private fun scanBluetooth() {
        Log.d("Bluetooth", "Initiating Bluetooth scan...")
        val bluetoothManager: BluetoothManager =
            requireContext().getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "Device doesn't support Bluetooth", Toast.LENGTH_LONG)
                .show()
            Log.e("Bluetooth", "Device doesn't support Bluetooth")
        } else {
            Log.d("Bluetooth", "Bluetooth adapter initialized. Requesting Bluetooth permission...")
            bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH)
        }
    }

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d("Bluetooth", "Bluetooth permission granted: $isGranted")
        if (isGranted) {
            btPermission = true
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                Log.d("Bluetooth", "Bluetooth is not enabled. Launching Bluetooth activation intent...")
                btActivityResultLauncher.launch(enableBtIntent)
            } else {
                btScan()
            }
        } else {
            btPermission = false
        }
    }

    private val btActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        Log.d("Bluetooth", "Bluetooth activation result: ${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            btScan()
        }
    }

    private fun btScan() {
        Log.d("Bluetooth", "Initiating Bluetooth scan after permissions and activation...")
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothAdapter.startDiscovery()
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(requireContext(), receiver, filter, RECEIVER_VISIBLE_TO_INSTANT_APPS)
            val discoverableIntent: Intent =
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                    putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                }
            Log.d("Bluetooth", "Starting Bluetooth discovery...")
            startActivity(discoverableIntent)
        } else {
            // Handle the case when Bluetooth scan permission is not granted
            Log.d("Bluetooth", "Bluetooth scan permission not granted.")
            showPermissionDeniedDialog()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.parcelable(BluetoothDevice.EXTRA_DEVICE)

                    device?.let {
                        val deviceName = if (ActivityCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        } else {

                        }
                        it.name ?: "Unknown Device"
                        val deviceHardwareAddress = it.address // MAC address
                        // Do something with deviceName and deviceHardwareAddress
                        // For example, add them to devicesList and update the adapter
                        devicesList.add(it)
                        devicesAdapter.notifyDataSetChanged()
                        Log.d(
                            "Bluetooth",
                            "Bluetooth device found: ${it.name ?: "Unknown Device"}"
                        )
                    }
                }
            }
        }

    }
    private fun checkBluetoothPermission(): Boolean {

        return if (SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {

            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED

        }

    }
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Denied")
            .setMessage("Bluetooth permission is required for scanning devices. Please grant the permission.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireActivity().unregisterReceiver(receiver)
    }
}
