package com.example.bt_def.bluetooth

import android.bluetooth.BluetoothAdapter

class BluetoothController(private val adapter: BluetoothAdapter) {
    private var connectThread: ConnectThread? = null // turi viena gija kuri daro prijungima

    fun connect(mac: String, listener: Listener) { // pagal mac pasiima irengini ir paleidzia connect thread
        if (adapter.isEnabled && mac.isNotEmpty()) {
            val device = adapter.getRemoteDevice(mac)
            connectThread = ConnectThread(device, listener)
            connectThread?.start()
        }
    }
    fun sendMessage(message: String){
        connectThread?.sendMessage(message) // jei rysys aktyvus, tekstas eina i bluetooth socket
    }

    fun closeConnection(){
        connectThread?.closeConnection()
    }

    companion object{ // ui
        const val BLUETOOTH_CONNECTED = "Bluetooth Connected"
        const val BLUETOOTH_NO_CONNECTED = "Not Connected"

    }
    interface Listener{ // ui ausys
        fun onReceive(message: String)
    }
}