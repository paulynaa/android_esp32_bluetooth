package com.example.bt_def.bluetooth

import android.bluetooth.BluetoothAdapter

class BluetoothController(private val adapter: BluetoothAdapter) {
    private var connectThread: ConnectThread? = null

    fun connect(mac: String, listener: Listener) {
        if (adapter.isEnabled && mac.isNotEmpty()) {
            val device = adapter.getRemoteDevice(mac)
            connectThread = ConnectThread(device, listener)
            connectThread?.start()
        }
    }
    fun sendMessage(message: String){
        connectThread?.sendMessage(message)
    }

    fun closeConnection(){
        connectThread?.closeConnection()
    }

    companion object{
        const val BLUETOOTH_CONNECTED = "Bluetooth Connected"
        const val BLUETOOTH_NO_CONNECTED = "Not Connected"

    }
    interface Listener{
        fun onReceive(message: String)
    }
}