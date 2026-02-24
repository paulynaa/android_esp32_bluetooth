package com.example.esp32_valdymas

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bt_def.BluetoothConstants
import com.example.bt_def.bluetooth.BluetoothController
import com.example.esp32_valdymas.databinding.FragmentMainBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainFragment : Fragment() {
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var binding: FragmentMainBinding
    private lateinit var bluetoothController: BluetoothController
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBtAdapter()
        val pref = activity?.getSharedPreferences(BluetoothConstants.PREFERENCES, Context.MODE_PRIVATE)
        val mac = pref?.getString(BluetoothConstants.MAC, "")
        bluetoothController = BluetoothController(btAdapter)
        binding.bList.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_deviceListFragment)
        }
        binding.bConnect.setOnClickListener {
            bluetoothController.connect(mac ?: "")
        }

    }

    private fun initBtAdapter() {
        val bManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = bManager.adapter
    }
}