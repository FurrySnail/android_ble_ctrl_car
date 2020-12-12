package com.example.ble_test

import android.bluetooth.*
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.ble_test.R.layout.control_layout
import kotlinx.android.synthetic.main.control_layout.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ControlActivity: AppCompatActivity(){

    private lateinit var device: BluetoothDevice
    private lateinit var gatt: BluetoothGatt
    private lateinit var characteristic: BluetoothGattCharacteristic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            ?: error("Missing BluetoothDevice from MainActivity!")
        setContentView(control_layout)

        control_led_on.setOnClickListener{sendCommand("l")}
        control_led_off.setOnClickListener{sendCommand("n")}

        control_ahead.setOnClickListener{sendCommand("w")}
        control_back.setOnClickListener{sendCommand("s")}
        control_left.setOnClickListener{sendCommand("a")}
        control_right.setOnClickListener{sendCommand("d")}
        control_stop.setOnClickListener{sendCommand("x")}
        gatt = device.connectGatt(this, true, Gattcallback);

    }



    private fun sendCommand(input: String){

        Log.w("sendCommand: ", "$input")
        Log.w("sendCommand: ", "characteristic: ${characteristic.uuid}")

        characteristic.setWriteType(WRITE_TYPE_DEFAULT)
        characteristic.setValue(input)

        //Send commands
        gatt.writeCharacteristic(characteristic)

    }

    private val Gattcallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    var bluetoothGatt = gatt
                    Handler(Looper.getMainLooper()).post {
                        bluetoothGatt?.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
                gatt.close()
            }
        }


        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {

            with(gatt) {

                if (services.isEmpty()) {
                    Log.i("Gattcallback", "No service and characteristic available, call discoverServices() first?")
                    return
                }

                services.forEach {
                    if (it.uuid.toString() == "0000ffe0-0000-1000-8000-00805f9b34fb") {
                        characteristic =
                            it.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"))
                        Log.i("Characteristic", "${characteristic.uuid} found")
                    }
                }
            }
        }
    }
}