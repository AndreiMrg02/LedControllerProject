package com.example.ledcontrollerproject.util

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

interface CallbackBluetoothScan {
    fun permissionsNotGranted(permissions: Array<String>)
    fun bluetoothNotEnable()
    fun deviceFound(device: BluetoothDevice)
    fun deviceNotSupportBluetooth()

}

class BluetoothScanner(private val context: Context, private val callbackBluetoothScan: CallbackBluetoothScan) {
    @RequiresApi(Build.VERSION_CODES.S)
    private val needPermissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val broadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            when(p1!!.action!!){
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice = p1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    callbackBluetoothScan.deviceFound(device)
                }
            }
        }
    }

    private val intentFilter = IntentFilter()

    init {
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(BluetoothDevice.ACTION_NAME_CHANGED)
        context.registerReceiver(broadcastReceiver, intentFilter)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun initRefreshData(){
        if (bluetoothAdapter != null) {
            val permissions = preparePermissions()
            if (permissions) {
                val module = prepareBluetoothModule()
                if (module) {
                    refreshData()
                }
            }
        }else{
            callbackBluetoothScan.deviceNotSupportBluetooth()
        }
    }

    @SuppressLint("MissingPermission")
    private fun refreshData(){
        bluetoothAdapter!!.cancelDiscovery();
        val res = bluetoothAdapter.startDiscovery()
        Log.w("Scanning", res.toString())
    }

    private fun prepareBluetoothModule(): Boolean{
        if (bluetoothAdapter?.isEnabled == false){
            callbackBluetoothScan.bluetoothNotEnable()
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun preparePermissions(): Boolean{
        if (!checkPermissions()) {
            callbackBluetoothScan.permissionsNotGranted(getNotGrantedPermissions())
            return false
        }
        return true
    }

    private fun checkPermission(permission: String): Boolean{
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkPermissions(): Boolean{
        return needPermissions.all { checkPermission(it) }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getNotGrantedPermissions(): Array<String> {
        return needPermissions.filter { !checkPermission(it) }.toTypedArray()
    }

    @SuppressLint("MissingPermission")
    fun stopScan(){
        bluetoothAdapter?.cancelDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun onDestroy(){
        bluetoothAdapter?.cancelDiscovery()
    }

}