package com.example.ledcontrollerproject.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ledcontrollerproject.R
import com.example.ledcontrollerproject.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var btPermission = false
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


         val connectButton: Button = binding.connectToBluetooth

        connectButton.setOnClickListener {
            Log.d("Bluetooth", "Connect button clicked. Initiating Bluetooth scan...")
            scanBt()

        }

        return root
    }

    fun scanBt() {
        Log.d("Bluetooth", "Initiating Bluetooth scan...")
        val bluetoothManager: BluetoothManager =
            requireContext().getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "Device doesn't support Bluetooth", Toast.LENGTH_LONG)
                .show()
            Log.e("Bluetooth", "Device doesn't support Bluetooth")
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                Log.d(
                    "Bluetooth",
                    "Bluetooth adapter initialized. Requesting Bluetooth permission..."
                )
                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH)
            }
        }
    }

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d("Bluetooth", "Bluetooth permission granted: $isGranted")
        if (isGranted) {
            val bluetoothManager: BluetoothManager =
                requireContext().getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter = bluetoothManager.adapter
            btPermission = true
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                Log.d(
                    "Bluetooth",
                    "Bluetooth is not enabled. Launching Bluetooth activation intent..."
                )
                btActivityResultLauncher.launch(enableBtIntent)
            } else {
                scanBT()
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
            scanBT()
        }
    }

    @SuppressLint("MissingPermission")
    private fun scanBT() {
        val bluetoothManager: BluetoothManager =
            requireContext().getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter
        val builder = AlertDialog.Builder(this@HomeFragment.requireContext())
        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.scan_bt, null)
        builder.setCancelable(false)
        builder.setView(dialogView)
        val btList = dialogView.findViewById<ListView>(R.id.devicesList)
        val dialog = builder.create()
        val pairedDevices: Set<BluetoothDevice> =
            bluetoothAdapter?.bondedDevices as Set<BluetoothDevice>
        val ADAhere: SimpleAdapter
        var data: MutableList<Map<String?, Any?>?>? = null
        data = ArrayList()
        if (pairedDevices.isNotEmpty()) {
            val datanum1: MutableMap<String?, Any> = HashMap()
            datanum1["A"] = " "
            datanum1["B"] = " "
            data.add(datanum1)
            for (device in pairedDevices) {
                val datanum: MutableMap<String?, Any?> = HashMap()
                datanum["A"] = device.name
                datanum["B"] = device.address
                data.add(datanum)
            }
            val fromwhere = arrayOf("A")
            val viewswhere = intArrayOf(R.id.itemName)
            ADAhere  = SimpleAdapter(requireContext(),data,R.layout.item_list,fromwhere,viewswhere)
            btList.adapter = ADAhere
            ADAhere.notifyDataSetChanged()
            btList.onItemClickListener = AdapterView.OnItemClickListener { adapaterView, view, position, l ->
                val string = ADAhere.getItem(position) as HashMap<String,String>
                val deviceName = string["A"]
                binding.deviceName.text = deviceName
                dialog.dismiss()
            }
        } else {
            val value = "No devices were found"
            Toast.makeText(requireContext(), value, Toast.LENGTH_LONG).show()
            return
        }
        dialog.show()
    }


    /*    private fun scanBluetooth() {
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
    }*/
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        //    requireActivity().unregisterReceiver(receiver)
    }
}
