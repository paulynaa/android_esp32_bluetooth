package com.example.bt_def.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.util.UUID
// susikuria bluetooth socket, prisijungia, jei prisijunge, pradeda klausytis, leidzia siusti zinutes, leidzia uzdaryti rysi.
class ConnectThread(private val device: BluetoothDevice, val listener: BluetoothController.Listener) : Thread() {
    private val uuid = "00001101-0000-1000-8000-00805F9B34FB" // klasikinis Bluetooth SPP
    private var mSocket: BluetoothSocket? = null
    init {
        try {
            mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid))
        } catch (e: IOException) {

        } catch (se: SecurityException) {

        }
    }
        override fun run() {
            try {
                mSocket?.connect()
                listener.onReceive(BluetoothController.BLUETOOTH_CONNECTED)
                readMessage()
            } catch (e: IOException) {
                listener.onReceive(BluetoothController.BLUETOOTH_NO_CONNECTED)
            } catch (se: SecurityException) {

            }
        }

    private fun readMessage(){
        val buffer = ByteArray(256)
         while (true) { // klausosi visa laika
             try {
                val length = mSocket?.inputStream?.read(buffer) // laukia, perskaito, grazina ilgi
                 val message = String(buffer, 0, length ?: 0) // is baitu padaro teksta
                 listener.onReceive(message) // teksta siuncia i ui
             } catch (e: IOException) {
                 listener.onReceive(BluetoothController.BLUETOOTH_NO_CONNECTED)
                 break
             }

         }
    }

    fun sendMessage(message: String){
        try {
            mSocket?.outputStream?.write(message.toByteArray())
        } catch (e: IOException) {

        }

    }

    fun closeConnection() {
        try {
            mSocket?.close()
        } catch (e: IOException) {

        }
    }
}