package com.example.esp32_valdymas

import android.animation.ObjectAnimator
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bt_def.BluetoothConstants
import com.example.bt_def.bluetooth.BluetoothController
import com.example.esp32_valdymas.databinding.FragmentMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainFragment : Fragment(), BluetoothController.Listener {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var bluetoothController: BluetoothController
    private var isConnected = false

    private lateinit var terminalFragment: TerminalFragment
    private lateinit var dashboardFragment: DashboardFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBtAdapter()
        setupViewPager()

        val pref = activity?.getSharedPreferences(BluetoothConstants.PREFERENCES, Context.MODE_PRIVATE)
        val mac = pref?.getString(BluetoothConstants.MAC, "")
        bluetoothController = BluetoothController(btAdapter)

        binding.bList.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_deviceListFragment)
        }

        binding.bConnect.setOnClickListener {
            if (isConnected) {
                bluetoothController.closeConnection()
                setDisconnected()
            } else {
                if (mac.isNullOrEmpty()) {
                    terminalFragment.appendLog("No device selected. Tap BT to pick a device.", TerminalFragment.LogType.ERROR)
                } else {
                    terminalFragment.appendLog("Connecting to $mac...", TerminalFragment.LogType.SYSTEM)
                    bluetoothController.connect(mac, this)
                }
            }
        }
    }

    private fun setupViewPager() {
        terminalFragment = TerminalFragment()
        dashboardFragment = DashboardFragment()

        val sendCommand: (String) -> Unit = { cmd ->
            if (isConnected) {
                bluetoothController.sendMessage(cmd)
                // log it if it came from dashboard (terminal logs its own)
            } else {
                terminalFragment.appendLog("Not connected.", TerminalFragment.LogType.ERROR)
            }
        }

        terminalFragment.onSendCommand = sendCommand
        dashboardFragment.onSendCommand = sendCommand

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int): Fragment =
                if (position == 0) terminalFragment else dashboardFragment
        }

        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = if (pos == 0) "TERMINAL" else "DASHBOARD"
        }.attach()
    }

    private fun initBtAdapter() {
        val bManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = bManager.adapter
    }

    private fun setConnected() {
        isConnected = true
        activity?.runOnUiThread {
            binding.bConnect.text = "DISCONNECT"
            binding.bConnect.backgroundTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#6B0000"))
            binding.tvConnectionStatus.text = "CONNECTED"
            binding.tvConnectionStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            binding.viewStatusDot.setBackgroundResource(R.drawable.status_dot_connected)
            pulseStatusDot()
        }
    }

    private fun setDisconnected() {
        isConnected = false
        activity?.runOnUiThread {
            binding.bConnect.text = "CONNECT"
            binding.bConnect.backgroundTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1A6B1A"))
            binding.tvConnectionStatus.text = "DISCONNECTED"
            binding.tvConnectionStatus.setTextColor(android.graphics.Color.parseColor("#666666"))
            binding.viewStatusDot.setBackgroundResource(R.drawable.status_dot)
        }
    }

    private fun pulseStatusDot() {
        val anim = ObjectAnimator.ofFloat(binding.viewStatusDot, "alpha", 1f, 0.2f, 1f)
        anim.duration = 1500
        anim.repeatCount = ObjectAnimator.INFINITE
        anim.start()
    }

    private fun parseAndDispatch(message: String) {
        val trimmed = message.trim()
        when {
            trimmed.startsWith("TEMP:") -> {
                val value = trimmed.removePrefix("TEMP:")
                dashboardFragment.updateSensorData("TEMP", value)
                terminalFragment.appendLog(trimmed, TerminalFragment.LogType.OK)
            }
            trimmed.startsWith("HUMID:") -> {
                val value = trimmed.removePrefix("HUMID:")
                dashboardFragment.updateSensorData("HUMID", value)
                terminalFragment.appendLog(trimmed, TerminalFragment.LogType.OK)
            }
            trimmed.startsWith("VALUE:") -> {
                val value = trimmed.removePrefix("VALUE:")
                dashboardFragment.updateSensorData("VALUE", value)
                terminalFragment.appendLog(trimmed, TerminalFragment.LogType.OK)
            }
            // NEW: potentiometer live value
            trimmed.startsWith("POT:") -> {
                val value = trimmed.removePrefix("POT:")
                dashboardFragment.updateSensorData("POT", value)
                // Don't log every pot update to terminal — it would spam it
            }
            // NEW: physical button pressed on ESP32
            trimmed == "BTN:PRESSED" -> {
                dashboardFragment.showResponse("Button pressed!", false)
                terminalFragment.appendLog("BTN:PRESSED — physical button triggered", TerminalFragment.LogType.OK)
            }
            trimmed == "OK" -> {
                dashboardFragment.showResponse("OK", false)
                terminalFragment.appendLog("OK", TerminalFragment.LogType.OK)
            }
            trimmed.startsWith("ERR:") -> {
                dashboardFragment.showResponse(trimmed, true)
                terminalFragment.appendLog(trimmed, TerminalFragment.LogType.ERROR)
            }
            trimmed.isNotEmpty() -> {
                terminalFragment.appendLog(trimmed, TerminalFragment.LogType.RECEIVED)
            }
        }
    }

    override fun onReceive(message: String) {
        when (message) {
            BluetoothController.BLUETOOTH_CONNECTED -> {
                setConnected()
                terminalFragment.appendLog("Connected!", TerminalFragment.LogType.OK)
            }
            BluetoothController.BLUETOOTH_NO_CONNECTED -> {
                setDisconnected()
                terminalFragment.appendLog("Connection lost.", TerminalFragment.LogType.ERROR)
            }
            else -> parseAndDispatch(message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}