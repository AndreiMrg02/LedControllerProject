import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.IOException
import java.io.OutputStream
import java.util.Random
import java.util.UUID


private const val TAG = "BluetoothService"
private const val MESSAGE_TOAST = 1
private const val MESSAGE_WRITE = 2

class BluetoothConnect(private val handler: Handler) {
    lateinit var classicSocket: BluetoothSocket
    private var outputStream: OutputStream? = null
    private val mmBuffer: ByteArray = ByteArray(1024)
    var leGatt: BluetoothGatt? = null
    private var bluetoothServiceUUID: String? = "0000ffe0-0000-1000-8000-00805f9b34fb"
    var bluetoothGattService: BluetoothGattService? = null
    private lateinit var bluetoothGattCharacteristic: BluetoothGattCharacteristic
    public lateinit var message: ByteArray
    private var startedSending: Boolean = false

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                leGatt?.requestMtu(512);
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from ble BLE device")
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val gattServices: List<BluetoothGattService> = gatt.getServices()
                Log.e("onServicesDiscovered", "Services count: " + gattServices.size)

                for (gattService in gattServices) {
                    val serviceUUID = gattService.uuid.toString()
                    if (serviceUUID == bluetoothServiceUUID) {
                        bluetoothGattService = gattService
                        Log.e("onServicesDiscovered", "Service uuid $serviceUUID")
                        for (characteristic in gattService.characteristics) {
                            val characteristicUUID = characteristic.uuid.toString()
                            characteristic.writeType
                            val properties = characteristic.properties
                            if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                                if(!startedSending) {
                                    bluetoothGattCharacteristic = characteristic
                                    startedSending = true
                                    write(message)
                                    Log.e(
                                        "onFoundWriteService",
                                        "Found write uuid $serviceUUID $characteristicUUID"
                                    )
//                                write(mes.toByteArray())
                                }
                                break;

                            }
                            Log.e("onServicesDiscovered", "Characteristic uuid $characteristicUUID")
                        }
                    }
                }

            }

        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e("onMTU", "MTU Good")
                leGatt?.discoverServices()
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int
        ) {
            startedSending = false
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e("Write", "Write Success")
            } else {
                Log.e("Write", "No Good")
            }
        }
    }


    @SuppressLint("MissingPermission")
    fun connectDevice(device: BluetoothDevice, uuid: UUID, context: Context) {
        BluetoothAdapter.getDefaultAdapter()?.cancelDiscovery()
        leGatt = null;
        classicSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
        if (device.type == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
            classicSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)
            try {
                if (classicSocket.isConnected) {
                    classicSocket.close()
                }
                classicSocket.connect()
                outputStream = classicSocket.outputStream
                Log.d(TAG, classicSocket.isConnected.toString())
            } catch (e: IOException) {
                e.message?.let { Log.d(TAG, it) }
            }
        } else if (device.type == BluetoothDevice.DEVICE_TYPE_LE) {
            leGatt = device.connectGatt(context, false, gattCallback)
            Log.d(TAG, "The device is Bluetooth Low Energy (LE) type ")
            return
        } else {
            // Tip de dispozitiv necunoscut sau alt tip de dispozitiv Bluetooth
            Log.d(TAG, "Unknown device type or unknown bluetooth device type")
            return
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    fun write(bytes: ByteArray) {
        try {
            if (leGatt != null) {
                val writeType = when {
                    bluetoothGattCharacteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    bluetoothGattCharacteristic.isWritableWithoutResponse() -> {
                        BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                    }

                    else -> error("Characteristic ${bluetoothGattCharacteristic.uuid} cannot be written to")
                }

                leGatt?.let { gatt ->
                    bluetoothGattCharacteristic.writeType = writeType
                    bluetoothGattCharacteristic.value = bytes
                    gatt.writeCharacteristic(bluetoothGattCharacteristic)
                } ?: error("Not connected to a BLE device!")
            } else if (classicSocket != null) {
                outputStream?.write(bytes)
            }

            val writtenMsg = handler.obtainMessage(MESSAGE_WRITE, -1, -1, mmBuffer)
            writtenMsg.sendToTarget()
        } catch (e: IOException) {
            Log.e(TAG, "Error occurred when sending data", e)

            // Send a failure message back to the activity.
            val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
            val bundle = Bundle().apply {
                putString("toast", "Couldn't send data to the other device")
            }
            writeErrorMsg.data = bundle
            handler.sendMessage(writeErrorMsg)
        }
    }

    fun BluetoothGattCharacteristic.isReadable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    private fun BluetoothGattCharacteristic.isWritable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    private fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    private fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0
    }

    fun cancel() {
        try {
            classicSocket.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the connect socket", e)
        }
    }
}
