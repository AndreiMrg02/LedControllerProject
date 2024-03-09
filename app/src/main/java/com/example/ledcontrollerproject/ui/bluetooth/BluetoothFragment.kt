package com.example.ledcontrollerproject.ui.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ledcontrollerproject.R
import com.example.ledcontrollerproject.databinding.FragmentHomeBinding

class BluetoothFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var btPermission = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this).get(BluetoothViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val connectButton: Button = binding.connectToBluetooth
        connectButton.setOnClickListener {
            Log.d("Bluetooth", "Connect button clicked. Initiating Bluetooth scan...")
            checkBluetoothSupport()

        }

        return root
    }

    /**
     * Method that checks if Bluetooth is supported, if yes, it will ask the required permission in order to use Bluetooth support.
     */
    private fun checkBluetoothSupport() {
        Log.d("Bluetooth", "Initiating Bluetooth scan...")
        val bluetoothManager: BluetoothManager =
            requireContext().getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "Device doesn't support Bluetooth", Toast.LENGTH_LONG)
                .show()
            Log.e("Bluetooth", "Device doesn't support Bluetooth")
        } else {
            Log.d("Bluetooth", "Bluetooth adapter initialized. Requesting Bluetooth permission...")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH)
            }
        }
    }

    /**
     * Static value that has access to launch, a method which can ask for a certain permission if necessary.
     */
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
                Log.d("Bluetooth", "Bluetooth is not enabled. Launching Bluetooth activation intent...")
                btActivityResultLauncher.launch(enableBtIntent)
            } else {
                scanBluetoothDevices()
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
            scanBluetoothDevices()
        }
    }

//    private fun checkDevicePressed(adapterView: AdapterView<*>, view: View, position: Int, l: Long, ADAhere:SimpleAdapter, dialog:AlertDialog) {
//        val string = ADAhere.getItem(position) as HashMap<String, String>
//        val deviceName = string["A"]
//        binding.deviceName.text = deviceName
//        dialog.dismiss()
//    }

    @SuppressLint("MissingPermission")
    private fun scanBluetoothDevices() {
        val bluetoothManager: BluetoothManager =
            requireContext().getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter

        val builder = AlertDialog.Builder(this@BluetoothFragment.requireContext())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
